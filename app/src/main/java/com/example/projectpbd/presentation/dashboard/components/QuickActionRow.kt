package com.example.projectpbd.presentation.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton

@Composable
fun QuickActionRow(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onUpdateGoal: () -> Unit,
    onViewAnalytics: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onAddExpense, modifier = Modifier.weight(1f)) {
                Text(text = "Expense", style = MaterialTheme.typography.labelLarge)
            }
            Button(onClick = onAddIncome, modifier = Modifier.weight(1f)) {
                Text(text = "Income", style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onUpdateGoal, modifier = Modifier.weight(1f)) {
                Text(text = "Goal", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(onClick = onViewAnalytics, modifier = Modifier.weight(1f)) {
                Text(text = "Analytics", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
