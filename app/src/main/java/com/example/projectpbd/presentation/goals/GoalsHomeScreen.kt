package com.example.projectpbd.presentation.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectpbd.domain.model.GoalStatus
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.components.SectionHeader
import com.example.projectpbd.presentation.goals.components.*
import com.example.projectpbd.presentation.goals.state.GoalUiState
import com.example.projectpbd.presentation.goals.state.GoalEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsHomeScreen(
    state: GoalUiState,
    onEvent: (GoalEvent) -> Unit,
    onAddGoal: () -> Unit,
    onGoalClick: (String) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SAVINGS VAULTS", fontWeight = FontWeight.Black) },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, "History")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGoal,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "New Goal")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dim.SpacingLarge),
            verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            // 1. Motivational Hero Card
            item {
                GoalMotivationalHero(summary = state.summary, baseCurrency = state.baseCurrency)
            }

            // 2. Active Goals
            val activeGoals = state.goals.filter { it.goal.status == GoalStatus.ACTIVE }
            if (activeGoals.isNotEmpty()) {
                item { SectionHeader(title = "Active Vaults") }
                items(activeGoals) { item ->
                    GoalVaultCard(
                        item = item,
                        baseCurrency = state.baseCurrency,
                        onClick = { onGoalClick(item.goal.id) }
                    )
                }
            }

            // 3. Other Goals
            val otherGoals = state.goals.filter { it.goal.status != GoalStatus.ACTIVE }
            if (otherGoals.isNotEmpty()) {
                item {
                    SectionHeader(title = "Other Goals")
                }
                items(otherGoals) { item ->
                    GoalVaultCard(
                        item = item,
                        baseCurrency = state.baseCurrency,
                        onClick = { onGoalClick(item.goal.id) },
                        isCompact = true
                    )
                }
            }

            if (state.goals.isEmpty() && !state.isLoading) {
                item {
                    GoalEmptyState(onAddGoal = onAddGoal)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
