package com.example.projectpbd.presentation.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpbd.core.util.CurrencyFormatter
import com.example.projectpbd.domain.model.InsightType
import com.example.projectpbd.presentation.analytics.state.*
import com.example.projectpbd.presentation.components.MomentumCard
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.ui.theme.MintPrimary
import com.example.projectpbd.ui.theme.ErrorCoral

@Composable
fun FinancialHealthCard(health: FinancialHealthUi, baseCurrency: String) {
    val statusColor = when {
        health.healthScore > 80 -> MintPrimary
        health.healthScore > 40 -> MaterialTheme.colorScheme.primary
        else -> ErrorCoral
    }

    MomentumCard(
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(statusColor.copy(alpha = 0.15f), Color.Transparent)
                        )
                    )
                    .padding(Dim.SpacingXLarge)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "MOMENTUM SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(Dim.SpacingSmall))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${health.healthScore}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(Dim.SpacingMedium))
                            Surface(
                                color = statusColor,
                                shape = RoundedCornerShape(Dim.RadiusSmall)
                            ) {
                                Text(
                                    text = health.stabilityLabel.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                        CircularProgressIndicator(
                            progress = { health.healthScore / 100f },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 6.dp,
                            color = statusColor,
                            trackColor = statusColor.copy(alpha = 0.1f),
                            strokeCap = StrokeCap.Round
                        )
                        Icon(
                            imageVector = if (health.healthScore > 60) Icons.Default.VerifiedUser else Icons.Default.Shield,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Column(modifier = Modifier.padding(horizontal = Dim.SpacingXLarge, vertical = Dim.SpacingLarge)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    HealthMetric(
                        label = "Net Cash Flow",
                        value = CurrencyFormatter.format(health.netCashFlow, baseCurrency),
                        icon = Icons.Default.AccountBalanceWallet,
                        color = if (health.netCashFlow >= 0) MintPrimary else ErrorCoral,
                        modifier = Modifier.weight(1f)
                    )
                    HealthMetric(
                        label = "Projected Surplus",
                        value = CurrencyFormatter.format(health.projectedNetCashFlow, baseCurrency),
                        icon = Icons.Default.FlashOn,
                        color = if (health.projectedNetCashFlow >= 0) Color(0xFFFFC107) else ErrorCoral,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(Dim.SpacingLarge))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(Dim.SpacingLarge))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Assets",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.format(health.totalAvailable, baseCurrency),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (health.savingsRate > 20) {
                        Surface(
                            color = MintPrimary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "ON TRACK",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MintPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthMetric(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SmartTrendChart(
    trends: List<TrendPointUi>,
    selectedTimeframe: TrendTimeframe,
    onTimeframeChange: (TrendTimeframe) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )

    MomentumCard {
        Column(modifier = Modifier.padding(Dim.SpacingXLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cash Flow Trends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dim.RadiusSmall))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(2.dp)
                ) {
                    TimeframeChip("7D", selectedTimeframe == TrendTimeframe.SEVEN_DAYS) { onTimeframeChange(TrendTimeframe.SEVEN_DAYS) }
                    TimeframeChip("30D", selectedTimeframe == TrendTimeframe.THIRTY_DAYS) { onTimeframeChange(TrendTimeframe.THIRTY_DAYS) }
                    TimeframeChip("3M", selectedTimeframe == TrendTimeframe.THREE_MONTHS) { onTimeframeChange(TrendTimeframe.THREE_MONTHS) }
                }
            }
            
            Spacer(modifier = Modifier.height(Dim.SpacingXLarge))

            if (trends.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else {
                val maxAbsVal = trends.maxOf { maxOf(Math.abs(it.income), Math.abs(it.expenses), Math.abs(it.net)) }.coerceAtLeast(1.0)
                val roundedMax = if (maxAbsVal < 10) 10.0 else ((maxAbsVal / 10).toInt() + 1) * 10.0

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingLeft = 60.dp.toPx()
                    val paddingBottom = 40.dp.toPx()
                    val chartWidth = width - paddingLeft
                    val chartHeight = height - paddingBottom

                    val gridCount = 4
                    for (i in 0..gridCount) {
                        val y = chartHeight - (i * (chartHeight / gridCount))
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            start = Offset(paddingLeft, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        
                        val labelValue = (roundedMax / gridCount * i).toInt()
                        val labelText = if (labelValue >= 1_000_000) "${labelValue / 1_000_000}M" 
                                       else if (labelValue >= 1000) "${labelValue / 1000}k" 
                                       else "$labelValue"
                        drawText(
                            textMeasurer = textMeasurer,
                            text = labelText,
                            style = labelStyle,
                            topLeft = Offset(0f, y - 10.dp.toPx())
                        )
                    }

                    val itemCount = trends.size
                    val groupSpacing = chartWidth / itemCount
                    val barWidth = (groupSpacing * 0.35f)
                    val barInnerSpacing = 4.dp.toPx()

                    trends.forEachIndexed { index, point ->
                        val groupCenterX = paddingLeft + (index * groupSpacing) + (groupSpacing / 2)
                        
                        val incomeHeight = (point.income / roundedMax * chartHeight).toFloat().coerceAtLeast(4.dp.toPx())
                        val incomeX = groupCenterX - barWidth - (barInnerSpacing / 2)
                        drawRoundRect(
                            color = MintPrimary,
                            topLeft = Offset(incomeX, chartHeight - incomeHeight),
                            size = Size(barWidth, incomeHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        
                        val expenseHeight = (point.expenses / roundedMax * chartHeight).toFloat().coerceAtLeast(4.dp.toPx())
                        val expenseX = groupCenterX + (barInnerSpacing / 2)
                        drawRoundRect(
                            color = ErrorCoral,
                            topLeft = Offset(expenseX, chartHeight - expenseHeight),
                            size = Size(barWidth, expenseHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        drawText(
                            textMeasurer = textMeasurer,
                            text = point.label,
                            style = labelStyle,
                            topLeft = Offset(groupCenterX - 15.dp.toPx(), chartHeight + 10.dp.toPx())
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Dim.SpacingLarge))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendIndicator("Income", MintPrimary)
                Spacer(modifier = Modifier.width(Dim.SpacingLarge))
                LegendIndicator("Expense", ErrorCoral)
            }
        }
    }
}

@Composable
private fun TimeframeChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(Dim.RadiusSmall)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendIndicator(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun WalletIntelligenceCard(intel: WalletIntelligenceUi, baseCurrency: String) {
    MomentumCard {
        Column(modifier = Modifier.padding(Dim.SpacingXLarge)) {
            Text(
                text = "Wallet Intelligence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(Dim.SpacingLarge))
            
            intel.items.forEach { usage ->
                WalletUsageRow(usage, baseCurrency)
                Spacer(modifier = Modifier.height(Dim.SpacingMedium))
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = Dim.SpacingSmall), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Dim.SpacingSmall),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InsightTag(label = "Most Used", value = intel.mostUsedWalletName, icon = Icons.Default.Wallet)
                InsightTag(label = "High Burn", value = intel.fastestDrainingWalletName, icon = Icons.Default.LocalFireDepartment, color = ErrorCoral)
                InsightTag(label = "Dormant", value = "${intel.dormantWalletCount}", icon = Icons.Default.Bedtime, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun WalletUsageRow(usage: WalletUsageUi, baseCurrency: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(usage.walletName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "${(usage.spendingPressure * 100).toInt()}% of total spend",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (usage.spendingPressure > 0.6) ErrorCoral else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(CurrencyFormatter.format(usage.balanceInBase, baseCurrency), style = MaterialTheme.typography.labelLarge)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { usage.usagePercentage },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun CurrencyExposureCard(exposure: List<CurrencyExposureUi>, baseCurrency: String) {
    MomentumCard {
        Column(modifier = Modifier.padding(Dim.SpacingXLarge)) {
            Text(
                text = "Currency Exposure",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(Dim.SpacingLarge))
            
            exposure.forEachIndexed { index, item ->
                CurrencyExposureRow(item, baseCurrency)
                if (index < exposure.size - 1) Spacer(modifier = Modifier.height(Dim.SpacingMedium))
            }
        }
    }
}

@Composable
private fun CurrencyExposureRow(item: CurrencyExposureUi, baseCurrency: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.flag, fontSize = 20.sp)
                Text(item.currencyCode, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
            Text(CurrencyFormatter.format(item.amountInBase, baseCurrency), style = MaterialTheme.typography.labelLarge)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { item.percentage },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = "${(item.percentage * 100).toInt()}% of net worth",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun InsightTag(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color = MintPrimary) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(12.dp), tint = color)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecurringImpactCard(impact: RecurringImpactUi, baseCurrency: String) {
    MomentumCard {
        Column(modifier = Modifier.padding(Dim.SpacingXLarge)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Repeat, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(Dim.SpacingSmall))
                Text(
                    text = "Recurring Impact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }
            
            Spacer(modifier = Modifier.height(Dim.SpacingLarge))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Monthly Burden",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.format(impact.monthlyBurdenAmount, baseCurrency),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Surface(
                    color = if (impact.burdenPercentage > 0.4f) ErrorCoral.copy(alpha = 0.1f) else MintPrimary.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "${(impact.burdenPercentage * 100).toInt()}% of Income",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = if (impact.burdenPercentage > 0.4f) ErrorCoral else MintPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dim.SpacingLarge))
            
            Text(
                text = "Next 30 days: ${impact.upcomingRecurringCount} payments scheduled",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SpendingBreakdownCard(breakdown: SpendingBreakdownUi, baseCurrency: String) {
    MomentumCard {
        Column(modifier = Modifier.padding(Dim.SpacingXLarge)) {
            Text(
                text = "Spending Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Dim.SpacingLarge))
            
            if (breakdown.categories.isEmpty()) {
                Text(
                    text = "No spending data yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                        DonutChart(breakdown.categories)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatCompact(breakdown.totalMonthlySpending, baseCurrency),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(Dim.SpacingXLarge))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)) {
                        breakdown.categories.take(4).forEachIndexed { index, item ->
                            LegendItem(
                                label = item.categoryName,
                                color = getCategoryColor(item.categoryId, index),
                                percentage = (item.percentage * 100).toInt()
                            )
                        }
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = Dim.SpacingLarge), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discretionary Spending",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "${(breakdown.discretionaryRatio * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutChart(categories: List<CategorySpendingUi>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        var startAngle = -90f
        categories.forEachIndexed { index, category ->
            val sweepAngle = category.percentage * 360f
            drawArc(
                color = getCategoryColor(category.categoryId, index),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color, percentage: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(Dim.SpacingSmall))
        Text(
            text = "$label ($percentage%)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun IncomeDistributionCard(distribution: IncomeDistributionUi, baseCurrency: String) {
    MomentumCard {
        Column(modifier = Modifier.padding(Dim.SpacingXLarge)) {
            Text(
                text = "Income Sources",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Dim.SpacingLarge))

            if (distribution.sources.isEmpty()) {
                Text(
                    text = "No income sources logged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                distribution.sources.forEachIndexed { index, item ->
                    IncomeBar(item, index, baseCurrency)
                    if (index < distribution.sources.size - 1) {
                        Spacer(modifier = Modifier.height(Dim.SpacingLarge))
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomeBar(item: SourceIncomeUi, index: Int, baseCurrency: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = item.categoryName, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = CurrencyFormatter.format(item.amount, baseCurrency),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { item.percentage },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = getSourceColor(index),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

private fun getSourceColor(index: Int): Color {
    val colors = listOf(
        MintPrimary,
        Color(0xFF2DC653),
        Color(0xFF25A244),
        Color(0xFF208B3A),
        Color(0xFF1A7431)
    )
    return colors[index % colors.size]
}

private fun getCategoryColor(categoryId: String, index: Int): Color {
    return when (categoryId.lowercase()) {
        "survival" -> Color(0xFFF07167)
        "convenience" -> Color(0xFFFFB703)
        "impulse" -> Color(0xFFE5989B)
        "lifestyle" -> Color(0xFF4895EF)
        "growth" -> MintPrimary
        else -> {
            val colors = listOf(Color(0xFF4DB6AC), Color(0xFF7986CB), Color(0xFFA1887F), Color(0xFF9575CD), Color(0xFF4FC3F7))
            colors[index % colors.size]
        }
    }
}

@Composable
fun BehavioralInsightCard(insight: BehavioralInsightUi) {
    val (icon, color) = when(insight.type) {
        InsightType.POSITIVE -> Icons.Default.CheckCircle to MintPrimary
        InsightType.NEGATIVE -> Icons.Default.Warning to ErrorCoral
        InsightType.NEUTRAL -> Icons.Default.Info to MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(Dim.RadiusMedium),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(Dim.SpacingLarge),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
            Spacer(modifier = Modifier.width(Dim.SpacingMedium))
            Column {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun GoalAnalyticsCard(goal: GoalAnalyticUi, baseCurrency: String) {
    MomentumCard {
        Column(modifier = Modifier.padding(Dim.SpacingLarge)) {
            Text(
                text = goal.goalTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Dim.SpacingMedium))
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(Dim.SpacingLarge))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Monthly Velocity",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.format(goal.velocity, baseCurrency),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Black
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = goal.projectedCompletionDate,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyAnalyticsState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(Dim.SpacingXXXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(Dim.SpacingXXLarge))
        Text(
            text = "Financial Intelligence Pending",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dim.SpacingMedium))
        Text(
            text = "We need more transaction history to generate smart insights and spending trends.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
