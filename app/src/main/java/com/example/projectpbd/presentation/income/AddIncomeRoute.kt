package com.example.projectpbd.presentation.income

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projectpbd.presentation.income.state.IncomeEvent

@Composable
fun AddIncomeRoute(
    incomeId: String? = null,
    onBack: () -> Unit,
    onIncomeSaved: () -> Unit,
    viewModel: IncomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(incomeId) {
        if (incomeId != null && uiState.incomeId != incomeId) {
            viewModel.onEvent(IncomeEvent.LoadIncome(incomeId))
        }
    }

    AddIncomeScreen(
        state = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onSaved = onIncomeSaved
    )
}

