package com.example.projectpbd.presentation.goals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpbd.core.util.CurrencyFormatter
import com.example.projectpbd.domain.model.Category
import com.example.projectpbd.domain.model.GoalStatus
import com.example.projectpbd.presentation.components.MomentumCard
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.goals.state.*
import com.example.projectpbd.presentation.transactions.util.CategoryVisualMapper

@Composable
fun GoalMotivationalHero(summary: GoalSummaryUi, baseCurrency: String) {
    MomentumCard(
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(Dim.SpacingXLarge),
            verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "TOTAL SAVED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = CurrencyFormatter.format(summary.totalSavingsBase, baseCurrency),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Savings, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }

            LinearProgressIndicator(
                progress = { summary.completionRate },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                summary.fastestGrowingGoalTitle?.let {
                    HeroStat("Fastest", it, Icons.Default.FlashOn)
                }
                HeroStat("Momentum", CurrencyFormatter.format(summary.monthlyMomentumBase, baseCurrency), Icons.AutoMirrored.Filled.TrendingUp)
                HeroStat("At Risk", "${summary.atRiskGoalsCount}", Icons.Default.Warning, if (summary.atRiskGoalsCount > 0) Color.Red else null)
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String, icon: ImageVector, color: Color? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, modifier = Modifier.size(12.dp), tint = color ?: MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
        }
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun GoalVaultCard(
    item: GoalItemUi,
    baseCurrency: String,
    onClick: () -> Unit,
    isCompact: Boolean = false
) {
    val categoryVisuals = remember(item.category) {
        item.category?.let { CategoryVisualMapper.getVisuals(it) } ?: CategoryVisualMapper.getVisuals(
            Category("unknown", "Miscellaneous", com.example.projectpbd.domain.model.CategoryType.EXPENSE, com.example.projectpbd.domain.model.CategorySource.SYSTEM)
        )
    }
    val paceColor = getPaceColor(item.paceStatus)
    
    MomentumCard(onClick = onClick) {
        Column(modifier = Modifier.padding(Dim.SpacingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(Dim.RadiusSmall),
                        color = categoryVisuals.color.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(categoryVisuals.icon, null, tint = categoryVisuals.color, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text(item.goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(color = paceColor.copy(alpha = 0.1f), shape = RoundedCornerShape(2.dp)) {
                                Text(
                                    item.paceStatus.name.replace("_", " "), 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = paceColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                            Text(item.wallet.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.format(item.currentAmount, item.wallet.currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (item.wallet.currency != baseCurrency) {
                        Text(
                            text = "≈ ${CurrencyFormatter.format(item.currentAmount, baseCurrency)}", 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            if (!isCompact) {
                Spacer(modifier = Modifier.height(Dim.SpacingLarge))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${(item.progress * 100).toInt()}% progress", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Target: ${CurrencyFormatter.format(item.goal.targetAmount, item.wallet.currency)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(Dim.SpacingSmall))
                
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = categoryVisuals.color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(Dim.SpacingMedium))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    GoalMiniStat("Remaining", CurrencyFormatter.format(item.remainingAmount, item.wallet.currency))
                    GoalMiniStat("Daily Goal", CurrencyFormatter.format(item.dailyRequired, item.wallet.currency))
                    GoalMiniStat("Days Left", "${item.remainingDays}")
                }
            }
        }
    }
}

@Composable
private fun GoalMiniStat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SavingsForecastModule(goals: List<GoalItemUi>) {
    val activePredictions = goals.filter { it.momentum > 0 && it.goal.status == GoalStatus.ACTIVE }.take(3)
    if (activePredictions.isEmpty()) return

    MomentumCard(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(Dim.SpacingLarge)) {
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.spacedBy(Dim.SpacingSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoGraph, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text("SAVINGS PACE & FORECAST", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.height(Dim.SpacingMedium))
            
            activePredictions.forEach { item ->
                val trendText = when {
                    item.paceStatus == PaceStatus.AHEAD_OF_SCHEDULE -> "Ahead of schedule!"
                    item.paceStatus == PaceStatus.AT_RISK -> "Falling behind."
                    item.paceStatus == PaceStatus.SLIGHTLY_BEHIND -> "Increase pace."
                    else -> "On track."
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(Dim.RadiusSmall),
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Dim.SpacingMedium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.goal.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(text = trendText, style = MaterialTheme.typography.bodySmall, color = getPaceColor(item.paceStatus))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Estimated Finish", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = item.projectedCompletionDate, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

private fun getPaceColor(status: PaceStatus): Color {
    return when (status) {
        PaceStatus.AHEAD_OF_SCHEDULE -> Color(0xFF4CAF50)
        PaceStatus.ON_TRACK -> Color(0xFF2196F3)
        PaceStatus.SLIGHTLY_BEHIND -> Color(0xFFFF9800)
        PaceStatus.AT_RISK -> Color(0xFFF44336)
        PaceStatus.OVERDUE -> Color(0xFFD32F2F)
        PaceStatus.STALLED -> Color(0xFF9E9E9E)
    }
}
