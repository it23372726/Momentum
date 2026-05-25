package com.example.projectpbd.presentation.wallets.state

import com.example.projectpbd.domain.model.Wallet
import com.example.projectpbd.domain.model.WalletType

data class WalletItemUi(
    val id: String,
    val name: String,
    val type: WalletType,
    val originalBalance: Double,
    val convertedBalanceLkr: Double,
    val currencyCode: String,
    val currencySymbol: String,
    val flagEmoji: String
)

data class WalletUiState(
    val wallets: List<WalletItemUi> = emptyList(),
    val selectedWalletId: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val totalBalance: Double = 0.0,
    val baseCurrency: String = "LKR",
    val form: WalletFormState = WalletFormState()
) {
    val isEditing: Boolean get() = selectedWalletId != null
}

data class WalletFormState(
    val name: String = "",
    val type: WalletType = WalletType.CASH,
    val initialBalance: String = "0",
    val currency: String = "LKR"
)

sealed class WalletEvent {
    data class LoadWallet(val id: String) : WalletEvent()
    data class NameChanged(val value: String) : WalletEvent()
    data class TypeSelected(val type: WalletType) : WalletEvent()
    data class BalanceChanged(val value: String) : WalletEvent()
    data class CurrencySelected(val code: String) : WalletEvent()
    data object SaveClicked : WalletEvent()
    data object DeleteClicked : WalletEvent()
    data object MessageShown : WalletEvent()
}
