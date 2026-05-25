package com.example.projectpbd.presentation.dashboard.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpbd.domain.model.CurrencyRegistry
import com.example.projectpbd.core.util.CurrencyFormatter
import com.example.projectpbd.domain.model.Wallet
import com.example.projectpbd.domain.model.WalletType
import com.example.projectpbd.presentation.components.MomentumCard
import com.example.projectpbd.presentation.dashboard.state.*
import com.example.projectpbd.presentation.wallets.state.WalletItemUi
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim

@Composable
fun FinancialSnapshotCard(snapshot: FinancialSnapshotUi, baseCurrency: String) {
    MomentumCard(
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(Dim.SpacingXLarge),
            verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            Column {
                Text(
                    "Total Available",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                AnimatedBalanceCounter(
                    amount = snapshot.totalAvailable,
                    currencyCode = baseCurrency,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SnapshotItem("Income", snapshot.monthlyIncome, baseCurrency, Icons.Default.TrendingUp, Color(0xFF4CAF50))
                SnapshotItem("Expenses", snapshot.monthlyExpense, baseCurrency, Icons.Default.TrendingDown, Color(0xFFF44336))
                SnapshotItem("Momentum", snapshot.savingsMomentum, baseCurrency, Icons.Default.FlashOn, Color(0xFFFFC107))
            }
        }
    }
}

@Composable
private fun SnapshotItem(label: String, amount: Double, currency: String, icon: ImageVector, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
        }
        Text(
            text = CurrencyFormatter.format(amount, currency),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun WalletOverviewCarousel(
    wallets: List<WalletItemUi>,
    baseCurrency: String,
    onWalletClick: (String) -> Unit,
    onAddWallet: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dim.SpacingXLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Wallets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onAddWallet) {
                Text("Add New", style = MaterialTheme.typography.labelMedium)
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = Dim.SpacingXLarge),
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
        ) {
            items(wallets) { wallet ->
                CompactWalletCard(wallet = wallet, baseCurrency = baseCurrency, onClick = { onWalletClick(wallet.id) })
            }
        }
    }
}

@Composable
private fun CompactWalletCard(wallet: WalletItemUi, baseCurrency: String, onClick: () -> Unit) {
    val gradient = getWalletGradient(wallet.type)
    Surface(
        onClick = onClick,
        modifier = Modifier.width(160.dp).height(100.dp),
        shape = RoundedCornerShape(Dim.RadiusLarge),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.background(Brush.linearGradient(gradient)).padding(Dim.SpacingMedium)) {
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        getWalletIcon(wallet.type), 
                        null, 
                        modifier = Modifier.size(16.dp), 
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                    Text(wallet.flagEmoji, fontSize = 14.sp)
                }
                Column {
                    Text(wallet.name, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), maxLines = 1)
                    Text(
                        text = CurrencyFormatter.format(wallet.originalBalance, wallet.currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "≈ ${CurrencyFormatter.format(wallet.convertedBalanceLkr, baseCurrency)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CashFlowInsightCard(insight: CashFlowInsightUi) {
    MomentumCard {
        Row(
            modifier = Modifier.padding(Dim.SpacingLarge).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Monthly Progress", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${ (insight.savingsRate * 100).toInt() }% Saved",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                insight.topCategory?.let {
                    Text(
                        "Main spend: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                CircularProgressIndicator(
                    progress = { insight.savingsRate },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Icon(Icons.Default.AutoGraph, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun AnimatedBalanceCounter(
    amount: Double,
    currencyCode: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color
) {
    val animatedAmount by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "BalanceAnimation"
    )

    Text(
        text = CurrencyFormatter.format(animatedAmount.toDouble(), currencyCode),
        style = style,
        color = color
    )
}

@Composable
fun BehavioralInsightChip(insight: BehavioralInsightUi) {
    val (icon, color) = when(insight.type) {
        InsightType.POSITIVE -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        InsightType.NEGATIVE -> Icons.Default.Error to Color(0xFFF44336)
        InsightType.ALERT -> Icons.Default.Warning to Color(0xFFFF9800)
        InsightType.NEUTRAL -> Icons.Default.Info to MaterialTheme.colorScheme.primary
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(Dim.RadiusMedium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Dim.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = color)
            Text(insight.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun UpcomingRecurringModule(items: List<UpcomingRecurringUi>) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
        Text(
            "Upcoming", 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Dim.SpacingXLarge)
        )
        
        items.forEach { item ->
            Surface(
                modifier = Modifier.padding(horizontal = Dim.SpacingXLarge),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(Dim.RadiusMedium)
            ) {
                Row(
                    modifier = Modifier.padding(Dim.SpacingMedium).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if(item.isIncome) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFF44336).copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if(item.isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                null,
                                modifier = Modifier.padding(8.dp),
                                tint = if(item.isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                        Column {
                            Text(item.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(item.frequencyLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyFormatter.format(item.amount, item.currencyCode),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(item.dateLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardQuickActions(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onTransfer: () -> Unit,
    onContribute: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Dim.SpacingXLarge),
        horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
    ) {
        QuickActionItem("Pay", Icons.Default.Remove, MaterialTheme.colorScheme.error, onAddExpense, Modifier.weight(1f))
        QuickActionItem("Add", Icons.Default.Add, MaterialTheme.colorScheme.primary, onAddIncome, Modifier.weight(1f))
        QuickActionItem("Move", Icons.Default.Sync, MaterialTheme.colorScheme.secondary, onTransfer, Modifier.weight(1f))
        QuickActionItem("Goal", Icons.Default.Star, Color(0xFFFFC107), onContribute, Modifier.weight(1f))
    }
}

@Composable
private fun QuickActionItem(label: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(Dim.RadiusLarge),
        contentColor = color
    ) {
        Column(
            modifier = Modifier.padding(vertical = Dim.SpacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

private fun getWalletIcon(type: WalletType): ImageVector {
    return when (type) {
        WalletType.CASH -> Icons.Default.Payments
        WalletType.BANK -> Icons.Default.AccountBalance
        WalletType.DIGITAL -> Icons.Default.AccountBalanceWallet
        WalletType.CRYPTO -> Icons.Default.CurrencyBitcoin
        WalletType.SAVINGS -> Icons.Default.Savings
        WalletType.CUSTOM -> Icons.Default.Wallet
    }
}

private fun getWalletGradient(type: WalletType): List<Color> {
    return when (type) {
        WalletType.CASH -> listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
        WalletType.BANK -> listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
        WalletType.DIGITAL -> listOf(Color(0xFFAD1457), Color(0xFF880E4F))
        WalletType.CRYPTO -> listOf(Color(0xFFFF8F00), Color(0xFFE65100))
        WalletType.SAVINGS -> listOf(Color(0xFF00838F), Color(0xFF006064))
        WalletType.CUSTOM -> listOf(Color(0xFF455A64), Color(0xFF263238))
    }
}
