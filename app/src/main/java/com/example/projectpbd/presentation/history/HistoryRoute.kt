package com.example.projectpbd.presentation.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.projectpbd.presentation.recurring.RecurringViewModel

@Composable
fun HistoryRoute(
    onBack: () -> Unit,
    onEditActivity: (String, Boolean) -> Unit,
    onEditRecurringExpense: (String) -> Unit,
    onEditRecurringIncome: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
    recurringViewModel: RecurringViewModel = hiltViewModel()
) {
    val historyState by viewModel.uiState.collectAsStateWithLifecycle()
    val recurringState by recurringViewModel.uiState.collectAsStateWithLifecycle()

    HistoryHubScreen(
        historyState = historyState,
        recurringState = recurringState,
        onHistoryEvent = viewModel::onEvent,
        onRecurringEvent = recurringViewModel::onEvent,
        onBack = onBack,
        onEditTransaction = onEditActivity,
        onEditRecurringExpense = onEditRecurringExpense,
        onEditRecurringIncome = onEditRecurringIncome
    )
}
