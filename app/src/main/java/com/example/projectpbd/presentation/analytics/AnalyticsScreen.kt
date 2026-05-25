package com.example.projectpbd.presentation.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectpbd.presentation.analytics.components.*
import com.example.projectpbd.presentation.analytics.state.AnalyticsEvent
import com.example.projectpbd.presentation.analytics.state.AnalyticsUiState
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    state: AnalyticsUiState,
    onBack: () -> Unit,
    onEvent: (AnalyticsEvent) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "INTELLIGENCE",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (!state.isLoading && state.trends.isEmpty() && state.insights.isEmpty()) {
             Box(modifier = Modifier.padding(padding)) {
                 EmptyAnalyticsState()
             }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = Dim.SpacingXXXLarge),
                verticalArrangement = Arrangement.spacedBy(Dim.SpacingXLarge)
            ) {
                // 1. Financial Health (Hero)
                item {
                    FinancialHealthCard(health = state.financialHealth, baseCurrency = state.baseCurrency)
                }

                // 2. Spending & Income Trends (Graphs)
                item {
                    SmartTrendChart(
                        trends = state.trends,
                        selectedTimeframe = state.selectedTimeframe,
                        onTimeframeChange = { onEvent(AnalyticsEvent.TimeframeChanged(it)) }
                    )
                }

                // 3. Behavioral Insights
                if (state.insights.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = Dim.SpacingXLarge),
                            verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
                        ) {
                            SectionHeader(title = "Behavioral Patterns")
                            state.insights.forEach { insight ->
                                BehavioralInsightCard(insight = insight)
                            }
                        }
                    }
                }

                // 4. Wallet Intelligence
                item {
                    WalletIntelligenceCard(intel = state.walletIntelligence, baseCurrency = state.baseCurrency)
                }

                // 5. Currency Exposure
                if (state.currencyExposure.isNotEmpty()) {
                    item {
                        CurrencyExposureCard(exposure = state.currencyExposure, baseCurrency = state.baseCurrency)
                    }
                }

                // 6. Recurring Impact
                item {
                    RecurringImpactCard(impact = state.recurringImpact, baseCurrency = state.baseCurrency)
                }

                // 7. Distribution Insights
                item {
                    SpendingBreakdownCard(breakdown = state.spendingBreakdown, baseCurrency = state.baseCurrency)
                }

                item {
                    IncomeDistributionCard(distribution = state.incomeDistribution, baseCurrency = state.baseCurrency)
                }

                // 8. Goal Progress Intelligence
                if (state.goalAnalytics.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = Dim.SpacingXLarge),
                            verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
                        ) {
                            SectionHeader(title = "Goal Velocity")
                            state.goalAnalytics.forEach { goal ->
                                GoalAnalyticsCard(goal = goal, baseCurrency = state.baseCurrency)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(Dim.SpacingXLarge))
                }
            }
        }
    }
}
