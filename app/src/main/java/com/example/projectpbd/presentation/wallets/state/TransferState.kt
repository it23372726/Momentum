package com.example.projectpbd.presentation.wallets.state

import com.example.projectpbd.domain.model.Wallet

data class TransferUiState(
    val wallets: List<Wallet> = emptyList(),
    val sourceWalletId: String? = null,
    val destinationWalletId: String? = null,
    val amount: String = "",
    val notes: String = "",
    val baseCurrency: String = "LKR",
    
    // Live Calculation Fields
    val convertedAmount: Double = 0.0,
    val exchangeRate: Double = 1.0,
    val sourceResultingBalance: Double = 0.0,
    val destResultingBalance: Double = 0.0,
    val isInsufficientFunds: Boolean = false,
    
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val showConfirmation: Boolean = false
)

sealed class TransferEvent {
    data class SourceSelected(val id: String) : TransferEvent()
    data class DestinationSelected(val id: String) : TransferEvent()
    data class AmountChanged(val value: String) : TransferEvent()
    data class NotesChanged(val value: String) : TransferEvent()
    data object SwapWallets : TransferEvent()
    data object RequestConfirmation : TransferEvent()
    data object DismissConfirmation : TransferEvent()
    data object Submit : TransferEvent()
    data object MessageShown : TransferEvent()
    data object ResetSuccess : TransferEvent()
}
