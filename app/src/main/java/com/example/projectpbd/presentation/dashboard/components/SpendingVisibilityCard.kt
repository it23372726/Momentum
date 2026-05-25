package com.example.projectpbd.presentation.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectpbd.presentation.components.MomentumCard
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.dashboard.state.CashFlowInsightUi

@Composable
fun SpendingVisibilityCard(state: CashFlowInsightUi) {
    MomentumCard {
        Column(modifier = Modifier.padding(Dim.SpacingLarge)) {
            Text(
                text = "Spending Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Dim.SpacingMedium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Top Category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = state.topCategory ?: "None",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "LKR ${"%,.0f".format(state.topCategoryAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
