package com.example.projectpbd.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectpbd.core.util.CurrencyFormatter
import com.example.projectpbd.presentation.dashboard.components.*
import com.example.projectpbd.presentation.dashboard.state.DashboardUiState
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.components.SectionHeader
import com.example.projectpbd.presentation.components.ModernTransactionItem

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onAddWallet: () -> Unit,
    onWalletClick: (String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onEditActivity: (String, Boolean) -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(Dim.SpacingXLarge)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dim.SpacingXLarge, vertical = Dim.SpacingLarge),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Momentum",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Financial Command Center",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            }

            // Hero Snapshot
            item {
                Box(modifier = Modifier.padding(horizontal = Dim.SpacingXLarge)) {
                    FinancialSnapshotCard(snapshot = state.snapshot, baseCurrency = state.baseCurrency)
                }
            }

            // Wallets
            item {
                WalletOverviewCarousel(
                    wallets = state.wallets,
                    baseCurrency = state.baseCurrency,
                    onWalletClick = onWalletClick,
                    onAddWallet = onAddWallet
                )
            }

            // Insights Feed
            if (state.insights.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = Dim.SpacingXLarge),
                        verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)
                    ) {
                        state.insights.forEach { insight ->
                            BehavioralInsightChip(insight = insight)
                        }
                    }
                }
            }

            // Progress / Cash Flow
            item {
                Box(modifier = Modifier.padding(horizontal = Dim.SpacingXLarge)) {
                    CashFlowInsightCard(insight = state.cashFlow)
                }
            }

            // Recurring Module
            if (state.upcomingRecurring.isNotEmpty()) {
                item {
                    UpcomingRecurringModule(items = state.upcomingRecurring)
                }
            }

            // Activity Feed
            item {
                SectionHeader(
                    title = "Recent Activity",
                    actionLabel = "See All",
                    onActionClick = onNavigateToHistory,
                    modifier = Modifier.padding(horizontal = Dim.SpacingXLarge)
                )
                Spacer(modifier = Modifier.height(Dim.SpacingSmall))
                Column(modifier = Modifier.padding(horizontal = Dim.SpacingXLarge)) {
                    if (state.recentActivity.isEmpty()) {
                        Text("No recent activity", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        state.recentActivity.forEach { activity ->
                            ModernTransactionItem(
                                label = activity.label,
                                amount = CurrencyFormatter.formatWithSign(activity.amount, activity.currencyCode, activity.isIncome),
                                date = if (activity.currencyCode != state.baseCurrency) "${activity.dateLabel} • ≈ ${CurrencyFormatter.format(activity.convertedAmountLkr, state.baseCurrency)}" else activity.dateLabel,
                                icon = if (activity.isIncome) Icons.Default.Add else Icons.Default.Remove,
                                iconContainerColor = if (activity.isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                isIncome = activity.isIncome,
                                onClick = { onEditActivity(activity.id, activity.isIncome) }
                            )
                        }
                    }
                }
            }
        }
    }
}
