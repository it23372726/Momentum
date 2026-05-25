package com.example.projectpbd.presentation.analytics.state

import com.example.projectpbd.domain.model.InsightType

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val baseCurrency: String = "LKR",
    val financialHealth: FinancialHealthUi = FinancialHealthUi(),
    val spendingBreakdown: SpendingBreakdownUi = SpendingBreakdownUi(),
    val incomeDistribution: IncomeDistributionUi = IncomeDistributionUi(),
    val walletIntelligence: WalletIntelligenceUi = WalletIntelligenceUi(),
    val currencyExposure: List<CurrencyExposureUi> = emptyList(),
    val recurringImpact: RecurringImpactUi = RecurringImpactUi(),
    val trends: List<TrendPointUi> = emptyList(),
    val selectedTimeframe: TrendTimeframe = TrendTimeframe.SEVEN_DAYS,
    val insights: List<BehavioralInsightUi> = emptyList(),
    val awarenessScore: AwarenessScoreUi = AwarenessScoreUi(),
    val goalAnalytics: List<GoalAnalyticUi> = emptyList(),
    val errorMessage: String? = null
)

data class FinancialHealthUi(
    val totalAvailable: Double = 0.0,
    val savingsRate: Double = 0.0,
    val expenseIncomeRatio: Float = 0f,
    val netCashFlow: Double = 0.0,
    val projectedNetCashFlow: Double = 0.0,
    val healthScore: Int = 0,
    val stabilityLabel: String = "Unknown"
)

data class SpendingBreakdownUi(
    val totalMonthlySpending: Double = 0.0,
    val categories: List<CategorySpendingUi> = emptyList(),
    val discretionaryRatio: Float = 0f
)

data class CategorySpendingUi(
    val categoryId: String,
    val categoryName: String,
    val amount: Double,
    val percentage: Float
)

data class IncomeDistributionUi(
    val totalMonthlyIncome: Double = 0.0,
    val sources: List<SourceIncomeUi> = emptyList(),
    val sideIncomeRatio: Float = 0f
)

data class SourceIncomeUi(
    val categoryId: String,
    val categoryName: String,
    val amount: Double,
    val percentage: Float
)

data class WalletIntelligenceUi(
    val items: List<WalletUsageUi> = emptyList(),
    val mostUsedWalletName: String = "N/A",
    val fastestDrainingWalletName: String = "N/A",
    val dormantWalletCount: Int = 0,
    val walletDependencyScore: Float = 0f, // 0 to 1, how much depends on one wallet
    val totalNetWorth: Double = 0.0
)

data class WalletUsageUi(
    val walletId: String,
    val walletName: String,
    val balance: Double, // Original currency
    val balanceInBase: Double,
    val usagePercentage: Float, // Balance distribution
    val spendingPressure: Float // Percentage of total expenses from this wallet
)

data class CurrencyExposureUi(
    val currencyCode: String,
    val percentage: Float,
    val amountInBase: Double,
    val flag: String
)

data class RecurringImpactUi(
    val monthlyBurdenAmount: Double = 0.0,
    val burdenPercentage: Float = 0f,
    val recurringIncomeReliability: Float = 0f,
    val upcomingRecurringCount: Int = 0
)

data class TrendPointUi(
    val label: String,
    val income: Double,
    val expenses: Double,
    val net: Double
)

enum class TrendTimeframe {
    SEVEN_DAYS, THIRTY_DAYS, THREE_MONTHS, SIX_MONTHS, ONE_YEAR
}

sealed class AnalyticsEvent {
    data class TimeframeChanged(val timeframe: TrendTimeframe) : AnalyticsEvent()
    data object Refresh : AnalyticsEvent()
}

data class BehavioralInsightUi(
    val title: String,
    val message: String,
    val type: InsightType
)

data class AwarenessScoreUi(
    val score: Int = 0,
    val label: String = "N/A",
    val message: String = "",
    val trend: ScoreTrend = ScoreTrend.STABLE
)

enum class ScoreTrend { UP, DOWN, STABLE }

data class GoalAnalyticUi(
    val goalTitle: String,
    val velocity: Double,
    val projectedCompletionDate: String,
    val progress: Float
)
