package com.example.projectpbd.presentation.wallets

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.manager.CurrencyConversionManager
import com.example.projectpbd.domain.model.Transfer
import com.example.projectpbd.domain.model.Wallet
import com.example.projectpbd.domain.repository.SettingsRepository
import com.example.projectpbd.domain.repository.WalletRepository
import com.example.projectpbd.presentation.wallets.state.TransferEvent
import com.example.projectpbd.presentation.wallets.state.TransferUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val settingsRepository: SettingsRepository,
    private val conversionManager: CurrencyConversionManager
) : ViewModel() {

    private val TRANSFER_DEBUG = "TRANSFER_DEBUG"

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                walletRepository.observeWallets(),
                settingsRepository.getSettings(),
                _uiState.map { it.sourceWalletId }.distinctUntilChanged(),
                _uiState.map { it.destinationWalletId }.distinctUntilChanged(),
                _uiState.map { it.amount }.distinctUntilChanged()
            ) { walletsRes, settings, sourceId, destId, amountStr ->
                val wallets = (walletsRes as? Resource.Success)?.data.orEmpty()
                val baseCurrency = settings.baseCurrency
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                
                val sourceWallet = wallets.find { it.id == sourceId }
                val destWallet = wallets.find { it.id == destId }
                
                var exchangeRate = 1.0
                var convertedAmount = amount
                var isInsufficient = false
                var sourceResult = 0.0
                var destResult = 0.0

                if (sourceWallet != null && destWallet != null) {
                    exchangeRate = conversionManager.getRate(sourceWallet.currency, destWallet.currency)
                    convertedAmount = conversionManager.roundAmount(amount * exchangeRate)
                    sourceResult = sourceWallet.currentBalance - amount
                    destResult = destWallet.currentBalance + convertedAmount
                    isInsufficient = sourceResult < 0
                    
                    Log.d(TRANSFER_DEBUG, "Calc: $amount ${sourceWallet.currency} -> $convertedAmount ${destWallet.currency} (Rate: $exchangeRate)")
                }

                _uiState.update { it.copy(
                    wallets = wallets,
                    baseCurrency = baseCurrency,
                    exchangeRate = exchangeRate,
                    convertedAmount = convertedAmount,
                    sourceResultingBalance = sourceResult,
                    destResultingBalance = destResult,
                    isInsufficientFunds = isInsufficient
                ) }
            }.collect()
        }
    }

    fun onEvent(event: TransferEvent) {
        when (event) {
            is TransferEvent.SourceSelected -> {
                if (event.id == _uiState.value.destinationWalletId) {
                    swapWallets()
                } else {
                    _uiState.update { it.copy(sourceWalletId = event.id) }
                }
            }
            is TransferEvent.DestinationSelected -> {
                if (event.id == _uiState.value.sourceWalletId) {
                    swapWallets()
                } else {
                    _uiState.update { it.copy(destinationWalletId = event.id) }
                }
            }
            is TransferEvent.AmountChanged -> _uiState.update { it.copy(amount = event.value) }
            is TransferEvent.NotesChanged -> _uiState.update { it.copy(notes = event.value) }
            TransferEvent.SwapWallets -> swapWallets()
            TransferEvent.RequestConfirmation -> _uiState.update { it.copy(showConfirmation = true) }
            TransferEvent.DismissConfirmation -> _uiState.update { it.copy(showConfirmation = false) }
            TransferEvent.Submit -> submitTransfer()
            TransferEvent.MessageShown -> _uiState.update { it.copy(errorMessage = null) }
            TransferEvent.ResetSuccess -> _uiState.update { it.copy(success = false, amount = "", notes = "") }
        }
    }

    private fun swapWallets() {
        val currentSource = _uiState.value.sourceWalletId
        val currentDest = _uiState.value.destinationWalletId
        _uiState.update { it.copy(sourceWalletId = currentDest, destinationWalletId = currentSource) }
    }

    private fun submitTransfer() {
        val state = _uiState.value
        val source = state.sourceWalletId
        val dest = state.destinationWalletId
        val amount = state.amount.toDoubleOrNull()

        if (source == null || dest == null || amount == null || amount <= 0 || source == dest || state.isInsufficientFunds) {
            _uiState.update { it.copy(errorMessage = "Invalid transfer details.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmation = false) }
            val transfer = Transfer(
                id = "",
                sourceWalletId = source,
                destinationWalletId = dest,
                amount = amount,
                notes = state.notes,
                date = System.currentTimeMillis()
            )
            
            when (val res = walletRepository.transferFunds(transfer)) {
                is Resource.Success -> {
                    Log.d(TRANSFER_DEBUG, "Transfer successful: $amount from $source to $dest")
                    _uiState.update { it.copy(isLoading = false, success = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }
}
