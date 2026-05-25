package com.example.projectpbd.presentation.goals.state

import com.example.projectpbd.domain.model.*

sealed class GoalEvent {
    data class TitleChanged(val value: String) : GoalEvent()
    data class TargetAmountChanged(val value: String) : GoalEvent()
    data class TargetDateChanged(val dateMillis: Long) : GoalEvent()
    data class DescriptionChanged(val value: String) : GoalEvent()
    data class CategorySelected(val categoryId: String) : GoalEvent()
    data class WalletSelected(val walletId: String) : GoalEvent()
    data class InitialContributionChanged(val value: String) : GoalEvent()
    
    data class LoadGoalForEdit(val goalId: String) : GoalEvent()
    data object SaveGoalClicked : GoalEvent()
    data object UpdateGoalClicked : GoalEvent()
    
    data class DeleteGoalRequested(val goalId: String) : GoalEvent()
    data class ConfirmDelete(val goalId: String, val walletAction: WalletDeleteAction, val destinationWalletId: String? = null) : GoalEvent()
    
    data class BuyNowClicked(val goalId: String, val amount: Double, val note: String) : GoalEvent()
    
    data class ContributeClicked(val goalId: String, val sourceWalletId: String, val amount: Double, val note: String) : GoalEvent()
    data class WithdrawClicked(val goalId: String, val destinationWalletId: String, val amount: Double, val note: String) : GoalEvent()

    data class TransferAmountChanged(val value: String) : GoalEvent()
    data class TransferSourceWalletSelected(val walletId: String) : GoalEvent()
    data class TransferDestinationWalletSelected(val walletId: String) : GoalEvent()
    data class InitializeTransfer(val sourceId: String, val destId: String) : GoalEvent()
    
    data class StatusChanged(val goalId: String, val status: GoalStatus) : GoalEvent()
    data object MessageShown : GoalEvent()
    data object ClearTransferState : GoalEvent()
}

sealed class GoalUiEvent {
    data class ShowToast(val message: String) : GoalUiEvent()
    data object NavigateBack : GoalUiEvent()
    data class GoalCreatedSuccess(val goalId: String) : GoalUiEvent()
    data object GoalUpdatedSuccess : GoalUiEvent()
}

enum class WalletDeleteAction {
    TRANSFER_AND_DELETE,
    ARCHIVE_WALLET,
    PERMANENTLY_DELETE
}

data class GoalFormState(
    val goalId: String? = null,
    val title: String = "",
    val targetAmount: String = "",
    val targetDateMillis: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val description: String = "",
    val categoryId: String = "miscellaneous",
    val walletId: String = "",
    val initialContribution: String = "0"
)

data class GoalUiState(
    val isLoading: Boolean = false,
    val baseCurrency: String = "LKR",
    val goals: List<GoalItemUi> = emptyList(),
    val wallets: List<Wallet> = emptyList(), 
    val categories: List<Category> = emptyList(),
    val summary: GoalSummaryUi = GoalSummaryUi(),
    val form: GoalFormState = GoalFormState(),
    val transferPreview: TransferPreview = TransferPreview(),
    val lastTransferSuccess: TransferPreview? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class TransferPreview(
    val sourceWalletId: String = "",
    val destinationWalletId: String = "",
    val sourceAmount: Double = 0.0,
    val sourceCurrency: String = "",
    val targetAmount: Double = 0.0,
    val targetCurrency: String = "",
    val exchangeRate: Double = 1.0,
    val sourceBalanceBefore: Double = 0.0,
    val sourceBalanceAfter: Double = 0.0,
    val targetBalanceBefore: Double = 0.0,
    val targetBalanceAfter: Double = 0.0,
    val isInsufficientBalance: Boolean = false,
    val sourceWalletName: String = "",
    val targetWalletName: String = "",
    val amountInput: String = ""
)

data class GoalItemUi(
    val goal: SavingsGoal,
    val wallet: Wallet,
    val category: Category? = null,
    val currentAmount: Double,
    val progress: Float,
    val remainingAmount: Double,
    val remainingDays: Long,
    val dailyRequired: Double,
    val weeklyRequired: Double,
    val paceStatus: PaceStatus,
    val isOverdue: Boolean,
    val isReadyToBuy: Boolean,
    val projectedCompletionDate: String,
    val monthlyRequired: Double,
    val momentum: Float,
    val isAtRisk: Boolean = false,
    val insights: List<String> = emptyList(),
    val recentTransfers: List<Transfer> = emptyList()
)

enum class PaceStatus {
    ON_TRACK,
    SLIGHTLY_BEHIND,
    AT_RISK,
    AHEAD_OF_SCHEDULE,
    OVERDUE,
    STALLED
}

data class GoalSummaryUi(
    val totalSavingsBase: Double = 0.0,
    val completionRate: Float = 0f,
    val monthlyMomentumBase: Double = 0.0,
    val upcomingDeadlinesCount: Int = 0,
    val fastestGrowingGoalTitle: String? = null,
    val atRiskGoalsCount: Int = 0
)
