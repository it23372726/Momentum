package com.example.projectpbd.presentation.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpbd.presentation.components.MomentumCard
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim

@Composable
fun FinancialPulseCard(
    totalIncome: Double,
    totalExpenses: Double,
    availableBalance: Double,
    savingsRate: Float
) {
    MomentumCard(
        backgroundColor = MaterialTheme.colorScheme.secondary
    ) {
        Column(modifier = Modifier.padding(Dim.SpacingXLarge)) {
            Text(
                text = "Available Balance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
            )
            Text(
                text = formatCurrency(availableBalance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondary
            )
            
            Spacer(modifier = Modifier.height(Dim.SpacingXLarge))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(
                    label = "Income",
                    value = formatCurrency(totalIncome),
                    color = MaterialTheme.colorScheme.primary
                )
                MetricColumn(
                    label = "Expenses",
                    value = formatCurrency(totalExpenses),
                    color = MaterialTheme.colorScheme.error
                )
                MetricColumn(
                    label = "Savings Rate",
                    value = formatPercentage(savingsRate),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(Dim.SpacingXLarge))
            
            LinearProgressIndicator(
                progress = { (savingsRate / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun formatCurrency(value: Double): String = "LKR ${"%,.0f".format(value)}"
private fun formatPercentage(value: Float): String = "${value.toInt()}%"
