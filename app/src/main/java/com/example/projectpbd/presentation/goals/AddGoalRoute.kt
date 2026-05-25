package com.example.projectpbd.presentation.goals

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projectpbd.presentation.goals.state.GoalEvent
import com.example.projectpbd.presentation.goals.state.GoalUiEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddGoalRoute(
    goalId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(goalId) {
        if (goalId != null) {
            viewModel.onEvent(GoalEvent.LoadGoalForEdit(goalId))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is GoalUiEvent.NavigateBack -> {
                    onBack()
                }
                is GoalUiEvent.GoalCreatedSuccess -> {
                    onSaved()
                }
                is GoalUiEvent.GoalUpdatedSuccess -> {
                    onSaved()
                }
                is GoalUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    AddGoalScreen(
        state = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onCreateVault = viewModel::createVaultAndGoal
    )
}
