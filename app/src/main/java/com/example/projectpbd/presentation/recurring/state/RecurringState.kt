package com.example.projectpbd.presentation.recurring.state

import com.example.projectpbd.domain.model.Expense
import com.example.projectpbd.domain.model.Income

data class RecurringUiState(
    val expenses: List<Expense> = emptyList(),
    val incomes: List<Income> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class RecurringEvent {
    data class ToggleExpenseActive(val expenseId: String, val isActive: Boolean) : RecurringEvent()
    data class ToggleIncomeActive(val incomeId: String, val isActive: Boolean) : RecurringEvent()
    data class DeleteExpenseSchedule(val expenseId: String) : RecurringEvent()
    data class DeleteIncomeSchedule(val incomeId: String) : RecurringEvent()
    data object Refresh : RecurringEvent()
}
