package com.example.projectpbd.presentation.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projectpbd.presentation.dashboard.state.ActivityItemUi

@Composable
fun RecentActivityList(
    activities: List<ActivityItemUi>,
    onSeeAll: () -> Unit,
    onItemClick: (String, Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Recent Activity", style = MaterialTheme.typography.titleMedium)
                if (activities.isNotEmpty()) {
                    TextButton(onClick = onSeeAll) {
                        Text(text = "See All")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (activities.isEmpty()) {
                Text(
                    text = "No recent activity yet.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                activities.forEach { item ->
                    val sign = if (item.isIncome) "+" else "-"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(item.id, item.isIncome) }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "$sign LKR ${"%.2f".format(item.amount)} · ${item.label}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = item.dateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

