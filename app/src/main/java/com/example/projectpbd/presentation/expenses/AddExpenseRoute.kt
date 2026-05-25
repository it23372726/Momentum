package com.example.projectpbd.presentation.expenses

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projectpbd.presentation.expenses.state.ExpenseEvent

@Composable
fun AddExpenseRoute(
    expenseId: String? = null,
    onBack: () -> Unit,
    onExpenseSaved: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(expenseId) {
        if (expenseId != null && uiState.expenseId != expenseId) {
            viewModel.onEvent(ExpenseEvent.LoadExpense(expenseId))
        }
    }

    AddExpenseScreen(
        state = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onSaved = onExpenseSaved
    )
}

