package com.example.projectpbd.presentation.settings.state

import com.example.projectpbd.domain.model.AppTheme
import com.example.projectpbd.domain.model.Wallet

data class SettingsUiState(
    val baseCurrency: String = "LKR",
    val defaultWalletId: String? = null,
    val lastExchangeUpdate: Long = 0L,
    val theme: AppTheme = AppTheme.SYSTEM,
    val wallets: List<Wallet> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
