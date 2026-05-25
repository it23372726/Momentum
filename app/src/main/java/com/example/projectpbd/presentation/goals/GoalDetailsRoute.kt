package com.example.projectpbd.presentation.goals

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projectpbd.presentation.goals.state.GoalUiEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GoalHomeRoute(
    onAddGoal: () -> Unit,
    onGoalClick: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    GoalsHomeScreen(
        state = uiState,
        onEvent = viewModel::onEvent,
        onAddGoal = onAddGoal,
        onGoalClick = onGoalClick,
        onNavigateToHistory = onNavigateToHistory
    )
}

@Composable
fun GoalDetailsRoute(
    goalId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is GoalUiEvent.NavigateBack -> onBack()
                is GoalUiEvent.GoalCreatedSuccess -> { /* Handled in AddGoalRoute */ }
                is GoalUiEvent.GoalUpdatedSuccess -> { /* Handled in AddGoalRoute */ }
                is GoalUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    GoalDetailsScreen(
        goalId = goalId,
        state = uiState,
        onBack = onBack,
        onEvent = viewModel::onEvent,
        onEdit = { onEdit(goalId) }
    )
}
