package com.example.projectpbd.presentation.income.state

import com.example.projectpbd.domain.model.Category
import com.example.projectpbd.domain.model.RepeatConfiguration
import com.example.projectpbd.domain.model.Wallet

sealed class IncomeEvent {
    data class LoadIncome(val id: String) : IncomeEvent()
    data class AmountChanged(val value: String) : IncomeEvent()
    data class CurrencySelected(val currency: String) : IncomeEvent()
    data class CategorySelected(val categoryId: String) : IncomeEvent()
    data class WalletSelected(val walletId: String) : IncomeEvent()
    data class DateChanged(val dateMillis: Long) : IncomeEvent()
    data class NotesChanged(val value: String) : IncomeEvent()
    data class RepeatConfigChanged(val config: RepeatConfiguration) : IncomeEvent()
    data class ExchangeRateChanged(val value: String) : IncomeEvent()
    data class ConvertedAmountChanged(val value: String) : IncomeEvent()
    data object SaveClicked : IncomeEvent()
    data object DeleteClicked : IncomeEvent()
    data object MessageShown : IncomeEvent()
}

data class IncomeFormState(
    val amount: String = "",
    val currency: String = "LKR",
    val categoryId: String = "",
    val walletId: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val repeatConfig: RepeatConfiguration = RepeatConfiguration(),
    val exchangeRate: String = "",
    val convertedAmountLkr: String = ""
)

data class IncomeUiState(
    val incomeId: String? = null,
    val form: IncomeFormState = IncomeFormState(),
    val categories: List<Category> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val amountError: String? = null
) {
    val isEditing: Boolean get() = incomeId != null
}

