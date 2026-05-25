package com.example.projectpbd.presentation.wallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.CurrencyRegistry
import com.example.projectpbd.domain.model.Wallet
import com.example.projectpbd.domain.repository.ExchangeRateRepository
import com.example.projectpbd.domain.repository.SettingsRepository
import com.example.projectpbd.domain.repository.WalletRepository
import com.example.projectpbd.presentation.wallets.state.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: WalletRepository,
    private val settingsRepository: SettingsRepository,
    private val exchangeRateRepository: ExchangeRateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        observeWallets()
        refreshRates()
    }

    private fun refreshRates() {
        viewModelScope.launch {
            try {
                exchangeRateRepository.refreshRates()
            } catch (e: Exception) {
                // Ignore, use cache
            }
        }
    }

    private fun observeWallets() {
        viewModelScope.launch {
            combine(
                repository.observeWallets(),
                settingsRepository.getSettings()
            ) { resource, settings ->
                when (resource) {
                    is Resource.Success -> {
                        val wallets = resource.data
                        val baseCurrency = settings.baseCurrency
                        var totalBase = 0.0
                        val items = wallets.map { wallet ->
                            val rate = exchangeRateRepository.getRate(wallet.currency, baseCurrency)
                            val balanceBase = wallet.currentBalance * rate
                            totalBase += balanceBase
                            
                            val currencyInfo = CurrencyRegistry.getByCode(wallet.currency)
                            WalletItemUi(
                                id = wallet.id,
                                name = wallet.name,
                                type = wallet.type,
                                originalBalance = wallet.currentBalance,
                                convertedBalanceLkr = balanceBase,
                                currencyCode = wallet.currency,
                                currencySymbol = currencyInfo.symbol,
                                flagEmoji = currencyInfo.flag
                            )
                        }
                        _uiState.update { it.copy(wallets = items, totalBalance = totalBase, baseCurrency = baseCurrency, isLoading = false) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(errorMessage = resource.message, isLoading = false) }
                    }
                    Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }.collect()
        }
    }

    fun onEvent(event: WalletEvent) {
        when (event) {
            is WalletEvent.LoadWallet -> loadWallet(event.id)
            is WalletEvent.NameChanged -> _uiState.update { it.copy(form = it.form.copy(name = event.value)) }
            is WalletEvent.TypeSelected -> _uiState.update { it.copy(form = it.form.copy(type = event.type)) }
            is WalletEvent.BalanceChanged -> _uiState.update { it.copy(form = it.form.copy(initialBalance = event.value)) }
            is WalletEvent.CurrencySelected -> _uiState.update { it.copy(form = it.form.copy(currency = event.code)) }
            WalletEvent.SaveClicked -> saveWallet()
            WalletEvent.DeleteClicked -> deleteWallet()
            WalletEvent.MessageShown -> _uiState.update { it.copy(errorMessage = null, successMessage = null) }
        }
    }

    private fun loadWallet(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedWalletId = id) }
            when (val res = repository.getWallet(id)) {
                is Resource.Success -> {
                    val wallet = res.data
                    _uiState.update { it.copy(
                        isLoading = false,
                        form = WalletFormState(
                            name = wallet.name,
                            type = wallet.type,
                            initialBalance = wallet.currentBalance.toString(),
                            currency = wallet.currency
                        )
                    ) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun saveWallet() {
        val currentState = _uiState.value
        val form = currentState.form
        if (form.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Wallet name cannot be empty.") }
            return
        }

        val balance = form.initialBalance.toDoubleOrNull() ?: 0.0
        val wallet = Wallet(
            id = currentState.selectedWalletId ?: "",
            name = form.name,
            type = form.type,
            currentBalance = balance,
            currency = form.currency,
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = if (currentState.isEditing) {
                repository.updateWallet(wallet)
            } else {
                repository.addWallet(wallet)
            }

            when (res) {
                is Resource.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        successMessage = if (currentState.isEditing) "Wallet updated." else "Wallet created.",
                        form = WalletFormState(),
                        selectedWalletId = null
                    ) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun deleteWallet() {
        val id = _uiState.value.selectedWalletId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val res = repository.deleteWallet(id)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        successMessage = "Wallet deleted.",
                        form = WalletFormState(),
                        selectedWalletId = null
                    ) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }
}
