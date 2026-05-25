package com.example.projectpbd.presentation.dashboard.state

import com.example.projectpbd.presentation.wallets.state.WalletItemUi

data class DashboardUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val baseCurrency: String = "LKR",
    val snapshot: FinancialSnapshotUi = FinancialSnapshotUi(),
    val wallets: List<WalletItemUi> = emptyList(),
    val cashFlow: CashFlowInsightUi = CashFlowInsightUi(),
    val goals: List<GoalMomentumUi> = emptyList(),
    val upcomingRecurring: List<UpcomingRecurringUi> = emptyList(),
    val insights: List<BehavioralInsightUi> = emptyList(),
    val recentActivity: List<ActivityItemUi> = emptyList()
)

data class FinancialSnapshotUi(
    val totalAvailable: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val savingsMomentum: Double = 0.0,
    val netCashFlow: Double = 0.0
)

data class CashFlowInsightUi(
    val savingsRate: Float = 0f,
    val topCategory: String? = null,
    val topCategoryAmount: Double = 0.0,
    val walletDrainFactor: Double = 0.0 // Percentage of income spent
)

data class GoalMomentumUi(
    val id: String,
    val title: String,
    val currentAmount: Double,
    val targetAmount: Double,
    val progress: Float,
    val projectedDate: String? = null
)

data class UpcomingRecurringUi(
    val id: String,
    val title: String,
    val amount: Double,
    val currencyCode: String = "LKR",
    val currencySymbol: String = "Rs",
    val dateLabel: String,
    val isIncome: Boolean,
    val frequencyLabel: String
)

data class BehavioralInsightUi(
    val id: String,
    val message: String,
    val type: InsightType = InsightType.NEUTRAL
)

enum class InsightType {
    POSITIVE, NEGATIVE, NEUTRAL, ALERT
}

data class ActivityItemUi(
    val id: String,
    val label: String,
    val amount: Double,
    val convertedAmountLkr: Double = 0.0,
    val currencyCode: String = "LKR",
    val currencySymbol: String = "Rs",
    val dateLabel: String,
    val isIncome: Boolean,
    val walletName: String? = null,
    val categoryName: String? = null,
    val notes: String = "",
    val timestamp: Long
)
