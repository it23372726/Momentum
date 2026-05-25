package com.example.projectpbd.presentation.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.RepeatConfiguration
import com.example.projectpbd.domain.repository.ExpenseRepository
import com.example.projectpbd.domain.repository.IncomeRepository
import com.example.projectpbd.presentation.recurring.state.RecurringEvent
import com.example.projectpbd.presentation.recurring.state.RecurringUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

    init {
        loadSchedules()
    }

    private fun loadSchedules() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                expenseRepository.observeExpenses(),
                incomeRepository.observeIncomes()
            ) { expensesRes, incomesRes ->
                val expenses = if (expensesRes is Resource.Success) {
                    expensesRes.data.filter { it.repeatConfig.isEnabled }
                } else emptyList()

                val incomes = if (incomesRes is Resource.Success) {
                    incomesRes.data.filter { it.repeatConfig.isEnabled }
                } else emptyList()

                RecurringUiState(
                    expenses = expenses,
                    incomes = incomes,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onEvent(event: RecurringEvent) {
        when (event) {
            is RecurringEvent.ToggleExpenseActive -> toggleExpenseActive(event.expenseId, event.isActive)
            is RecurringEvent.ToggleIncomeActive -> toggleIncomeActive(event.incomeId, event.isActive)
            is RecurringEvent.DeleteExpenseSchedule -> deleteExpenseSchedule(event.expenseId)
            is RecurringEvent.DeleteIncomeSchedule -> deleteIncomeSchedule(event.incomeId)
            RecurringEvent.Refresh -> loadSchedules()
        }
    }

    private fun toggleExpenseActive(id: String, active: Boolean) {
        viewModelScope.launch {
            val expenseRes = expenseRepository.getExpense(id)
            if (expenseRes is Resource.Success) {
                val updated = expenseRes.data.copy(
                    repeatConfig = expenseRes.data.repeatConfig.copy(isActive = active)
                )
                expenseRepository.updateExpense(updated)
            }
        }
    }

    private fun toggleIncomeActive(id: String, active: Boolean) {
        viewModelScope.launch {
            val incomeRes = incomeRepository.getIncome(id)
            if (incomeRes is Resource.Success) {
                val updated = incomeRes.data.copy(
                    repeatConfig = incomeRes.data.repeatConfig.copy(isActive = active)
                )
                incomeRepository.updateIncome(updated)
            }
        }
    }

    private fun deleteExpenseSchedule(id: String) {
        viewModelScope.launch {
            val expenseRes = expenseRepository.getExpense(id)
            if (expenseRes is Resource.Success) {
                val updated = expenseRes.data.copy(
                    repeatConfig = RepeatConfiguration()
                )
                expenseRepository.updateExpense(updated)
            }
        }
    }

    private fun deleteIncomeSchedule(id: String) {
        viewModelScope.launch {
            val incomeRes = incomeRepository.getIncome(id)
            if (incomeRes is Resource.Success) {
                val updated = incomeRes.data.copy(
                    repeatConfig = RepeatConfiguration()
                )
                incomeRepository.updateIncome(updated)
            }
        }
    }
}
