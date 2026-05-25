package com.example.projectpbd.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.data.repository.AuthRepository
import com.example.projectpbd.domain.model.AppTheme
import com.example.projectpbd.domain.repository.ExchangeRateRepository
import com.example.projectpbd.domain.repository.SettingsRepository
import com.example.projectpbd.domain.repository.WalletRepository
import com.example.projectpbd.presentation.settings.state.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val walletRepository: WalletRepository,
    private val exchangeRateRepository: ExchangeRateRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeSettings()
        observeWallets()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.update { it.copy(
                    baseCurrency = settings.baseCurrency,
                    defaultWalletId = settings.defaultWalletId,
                    lastExchangeUpdate = settings.lastExchangeRateUpdate,
                    theme = settings.theme
                ) }
            }
        }
    }

    private fun observeWallets() {
        viewModelScope.launch {
            walletRepository.observeWallets().collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(wallets = resource.data) }
                }
            }
        }
    }

    fun updateBaseCurrency(currencyCode: String) {
        viewModelScope.launch {
            settingsRepository.updateBaseCurrency(currencyCode)
        }
    }

    fun updateDefaultWallet(walletId: String?) {
        viewModelScope.launch {
            settingsRepository.updateDefaultWallet(walletId)
        }
    }

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.updateTheme(theme)
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.logout()
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                exchangeRateRepository.clearCache()
                _uiState.update { it.copy(isLoading = false, successMessage = "Cache cleared successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to clear cache: ${e.message}") }
            }
        }
    }

    fun refreshExchangeRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                exchangeRateRepository.refreshRates()
                _uiState.update { it.copy(isLoading = false, successMessage = "Exchange rates updated.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to update rates: ${e.message}") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
