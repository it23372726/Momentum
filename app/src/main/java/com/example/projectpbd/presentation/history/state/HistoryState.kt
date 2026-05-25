package com.example.projectpbd.presentation.history.state

import com.example.projectpbd.presentation.dashboard.state.ActivityItemUi

data class HistoryUiState(
    val isLoading: Boolean = false,
    val activities: List<ActivityItemUi> = emptyList(),
    val baseCurrency: String = "LKR",
    val wallets: List<String> = emptyList(),
    val currencies: List<String> = emptyList(),
    val selectedWallet: String? = null,
    val selectedCurrency: String? = null,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

sealed class HistoryEvent {
    data object Refresh : HistoryEvent()
    data class WalletFilterChanged(val wallet: String?) : HistoryEvent()
    data class CurrencyFilterChanged(val currency: String?) : HistoryEvent()
    data class SearchQueryChanged(val query: String) : HistoryEvent()
}
