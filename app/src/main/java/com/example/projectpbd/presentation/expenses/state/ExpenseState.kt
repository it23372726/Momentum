package com.example.projectpbd.presentation.expenses.state

import com.example.projectpbd.domain.model.Category
import com.example.projectpbd.domain.model.PaymentMethod
import com.example.projectpbd.domain.model.RepeatConfiguration
import com.example.projectpbd.domain.model.Wallet

sealed class ExpenseEvent {
    data class LoadExpense(val id: String) : ExpenseEvent()
    data class AmountChanged(val value: String) : ExpenseEvent()
    data class CategorySelected(val categoryId: String) : ExpenseEvent()
    data class WalletSelected(val walletId: String) : ExpenseEvent()
    data class PaymentSelected(val method: PaymentMethod) : ExpenseEvent()
    data class NotesChanged(val value: String) : ExpenseEvent()
    data class DateChanged(val dateMillis: Long) : ExpenseEvent()
    data class RepeatConfigChanged(val config: RepeatConfiguration) : ExpenseEvent()
    data class DiscretionaryToggled(val enabled: Boolean) : ExpenseEvent()
    data object SaveClicked : ExpenseEvent()
    data object DeleteClicked : ExpenseEvent()
    data object MessageShown : ExpenseEvent()
}

data class ExpenseFormState(
    val amount: String = "",
    val currency: String = "LKR",
    val categoryId: String = "",
    val walletId: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CARD,
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val repeatConfig: RepeatConfiguration = RepeatConfiguration(),
    val isDiscretionary: Boolean = true
)

data class ExpenseUiState(
    val expenseId: String? = null,
    val form: ExpenseFormState = ExpenseFormState(),
    val categories: List<Category> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null
) {
    val isEditing: Boolean get() = expenseId != null
}

