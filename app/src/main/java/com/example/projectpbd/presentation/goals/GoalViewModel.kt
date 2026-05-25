package com.example.projectpbd.presentation.goals

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.manager.CurrencyConversionManager
import com.example.projectpbd.domain.model.*
import com.example.projectpbd.domain.repository.*
import com.example.projectpbd.presentation.goals.state.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.ceil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val walletRepository: WalletRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val conversionManager: CurrencyConversionManager
) : ViewModel() {

    private val GOAL_DELETE = "GOAL_DELETE"
    private val GOAL_NAVIGATION = "GOAL_NAVIGATION"
    private val GOAL_PURCHASE = "GOAL_PURCHASE"
    private val GOAL_TRANSFER_LOG = "GOAL_TRANSFER_DEBUG"
    private val GOAL_CURRENCY = "GOAL_CURRENCY"
    private val GOAL_EXCHANGE = "GOAL_EXCHANGE"

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = Channel<GoalUiEvent>()
    val eventFlow = _eventFlow.receiveAsFlow()

    init {
        observeGoalSystem()
    }

    private fun observeGoalSystem() {
        viewModelScope.launch {
            val goalsFlow = goalRepository.observeGoals()
            val walletsFlow = walletRepository.observeWallets()
            val transfersFlow = walletRepository.observeTransfers()
            val settingsFlow = settingsRepository.getSettings()
            val categoriesFlow = categoryRepository.observeCategories(CategoryType.EXPENSE)

            combine(
                goalsFlow,
                walletsFlow,
                transfersFlow,
                settingsFlow,
                categoriesFlow
            ) { args ->
                @Suppress("UNCHECKED_CAST")
                val goalsRes = args[0] as Resource<List<SavingsGoal>>
                @Suppress("UNCHECKED_CAST")
                val walletsRes = args[1] as Resource<List<Wallet>>
                @Suppress("UNCHECKED_CAST")
                val transfersRes = args[2] as Resource<List<Transfer>>
                val settings = args[3] as AppSettings
                @Suppress("UNCHECKED_CAST")
                val categoriesRes = args[4] as Resource<List<Category>>

                val isLoading = listOf(goalsRes, walletsRes, transfersRes, categoriesRes).any { it is Resource.Loading }
                val error = (goalsRes as? Resource.Error)?.message 
                    ?: (walletsRes as? Resource.Error)?.message
                    ?: (transfersRes as? Resource.Error)?.message
                    ?: (categoriesRes as? Resource.Error)?.message

                val goals = (goalsRes as? Resource.Success<List<SavingsGoal>>)?.data.orEmpty()
                val wallets = (walletsRes as? Resource.Success<List<Wallet>>)?.data.orEmpty()
                val transfers = (transfersRes as? Resource.Success<List<Transfer>>)?.data.orEmpty()
                val categories = (categoriesRes as? Resource.Success<List<Category>>)?.data.orEmpty()
                val baseCurrency = settings.baseCurrency

                calculateGoalSystemState(goals, wallets, transfers, categories, baseCurrency, isLoading, error)
            }.collect { state ->
                _uiState.update { it.copy(
                    isLoading = state.isLoading,
                    baseCurrency = state.baseCurrency,
                    goals = state.goals,
                    wallets = state.wallets,
                    categories = state.categories,
                    summary = state.summary,
                    errorMessage = state.errorMessage
                ) }
            }
        }
    }

    private suspend fun calculateGoalSystemState(
        goals: List<SavingsGoal>,
        wallets: List<Wallet>,
        transfers: List<Transfer>,
        categories: List<Category>,
        baseCurrency: String,
        isLoading: Boolean,
        error: String?
    ): GoalUiState {
        val walletMap = wallets.associateBy { it.id }
        val categoryMap = categories.associateBy { it.id }
        val now = ZonedDateTime.now()
        
        val items = goals.map { goal ->
            val vaultWallet = walletMap[goal.walletId] ?: Wallet(goal.walletId, "Unknown Vault", WalletType.SAVINGS, 0.0, "LKR")
            val currentAmount = vaultWallet.currentBalance
            val remaining = (goal.targetAmount - currentAmount).coerceAtLeast(0.0)
            val progress = if (goal.targetAmount > 0) (currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
            
            val targetZdt = Instant.ofEpochMilli(goal.targetDate).atZone(ZoneId.systemDefault())
            val remainingDays = ChronoUnit.DAYS.between(now.toLocalDate(), targetZdt.toLocalDate()).coerceAtLeast(0)
            val isOverdue = now.toLocalDate().isAfter(targetZdt.toLocalDate()) && currentAmount < goal.targetAmount
            
            val dailyRequired = if (remainingDays > 0) remaining / remainingDays else remaining
            val weeklyRequired = if (remainingDays >= 7) (remaining / remainingDays) * 7 else remaining
            
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            val recentContributions = transfers.filter { it.destinationWalletId == goal.walletId && it.date > thirtyDaysAgo }
                .sumOf { it.amount }
            
            val monthlyPace = recentContributions
            val paceStatus = calculatePaceStatus(goal, currentAmount, monthlyPace, dailyRequired, isOverdue)
            
            GoalItemUi(
                goal = goal,
                wallet = vaultWallet,
                category = categoryMap[goal.categoryId],
                currentAmount = currentAmount,
                progress = progress,
                remainingAmount = remaining,
                remainingDays = remainingDays,
                dailyRequired = dailyRequired,
                weeklyRequired = weeklyRequired,
                paceStatus = paceStatus,
                isOverdue = isOverdue,
                isReadyToBuy = currentAmount >= goal.targetAmount && goal.status == GoalStatus.ACTIVE,
                projectedCompletionDate = calculateProjectedDate(goal, currentAmount, monthlyPace),
                monthlyRequired = calculateMonthlyRequired(goal, currentAmount),
                momentum = if (dailyRequired > 0) ((monthlyPace / 30.0) / dailyRequired).toFloat().coerceIn(0f, 2f) else 1f,
                isAtRisk = paceStatus == PaceStatus.AT_RISK,
                insights = generateGoalInsights(goal, currentAmount, monthlyPace, dailyRequired, remainingDays, isOverdue),
                recentTransfers = transfers.filter { it.sourceWalletId == goal.walletId || it.destinationWalletId == goal.walletId }
                    .sortedByDescending { it.date }
                    .take(10)
            )
        }.sortedByDescending { it.progress }

        val activeItems = items.filter { it.goal.status == GoalStatus.ACTIVE }
        val totalSavingsBase = activeItems.sumOf { 
            val rate = conversionManager.getRate(it.wallet.currency, baseCurrency)
            it.currentAmount * rate
        }
        val avgProgress = if (items.isNotEmpty()) items.map { it.progress }.average().toFloat() else 0f
        
        val summary = GoalSummaryUi(
            totalSavingsBase = totalSavingsBase,
            completionRate = avgProgress,
            monthlyMomentumBase = activeItems.sumOf { item ->
                val vaultCurrency = item.wallet.currency
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                val monthlyTotal = transfers.filter { t -> t.destinationWalletId == item.goal.walletId && t.date > thirtyDaysAgo }
                    .sumOf { t -> t.amount }
                
                val rate = conversionManager.getRate(vaultCurrency, baseCurrency)
                monthlyTotal * rate
            },
            upcomingDeadlinesCount = items.count { it.goal.status == GoalStatus.ACTIVE && it.goal.targetDate < System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000) },
            fastestGrowingGoalTitle = activeItems.maxByOrNull { it.momentum }?.goal?.title,
            atRiskGoalsCount = activeItems.count { it.isAtRisk }
        )

        return GoalUiState(
            isLoading = isLoading,
            baseCurrency = baseCurrency,
            goals = items,
            wallets = wallets,
            categories = categories,
            summary = summary,
            form = _uiState.value.form,
            errorMessage = error
        )
    }

    private fun calculatePaceStatus(goal: SavingsGoal, current: Double, monthlyPace: Double, dailyRequired: Double, isOverdue: Boolean): PaceStatus {
        if (isOverdue) return PaceStatus.OVERDUE
        if (current >= goal.targetAmount) return PaceStatus.ON_TRACK
        
        val dailyPace = monthlyPace / 30.0
        if (monthlyPace <= 0) return PaceStatus.STALLED
        
        return when {
            dailyPace >= dailyRequired * 1.1 -> PaceStatus.AHEAD_OF_SCHEDULE
            dailyPace >= dailyRequired * 0.9 -> PaceStatus.ON_TRACK
            dailyPace >= dailyRequired * 0.5 -> PaceStatus.SLIGHTLY_BEHIND
            else -> PaceStatus.AT_RISK
        }
    }

    private fun calculateMonthlyRequired(goal: SavingsGoal, currentAmount: Double): Double {
        val remaining = (goal.targetAmount - currentAmount).coerceAtLeast(0.0)
        val monthsRemaining = ChronoUnit.MONTHS.between(ZonedDateTime.now(), Instant.ofEpochMilli(goal.targetDate).atZone(ZoneId.systemDefault())).coerceAtLeast(1)
        return remaining / monthsRemaining
    }

    private fun calculateProjectedDate(goal: SavingsGoal, current: Double, monthlyPace: Double): String {
        if (monthlyPace <= 0) return "Stalled"
        val remaining = goal.targetAmount - current
        if (remaining <= 0) return "Ready"
        val monthsToFinish = ceil(remaining / monthlyPace).toLong()
        return ZonedDateTime.now().plusMonths(monthsToFinish).format(DateTimeFormatter.ofPattern("MMM yyyy"))
    }

    private fun generateGoalInsights(goal: SavingsGoal, current: Double, monthlyPace: Double, dailyRequired: Double, remainingDays: Long, isOverdue: Boolean): List<String> {
        val insights = mutableListOf<String>()
        
        if (isOverdue) {
            insights.add("Goal deadline passed. Increase contributions to recover immediately.")
            return insights
        }

        if (current >= goal.targetAmount) {
            insights.add("Goal target reached! You can now proceed with your purchase.")
            return insights
        }

        insights.add("You need to save ${"%.0f".format(dailyRequired)} /day to finish in $remainingDays days.")

        val dailyPace = monthlyPace / 30.0
        if (dailyPace > dailyRequired * 1.1) {
            insights.add("Contributing faster than required! You're ahead of schedule.")
        } else if (dailyPace < dailyRequired * 0.8 && dailyPace > 0) {
            insights.add("Slightly behind pace. Adding small amounts daily will help.")
        }
        
        return insights
    }

    fun onEvent(event: GoalEvent) {
        when (event) {
            is GoalEvent.TitleChanged -> updateForm { copy(title = event.value) }
            is GoalEvent.TargetAmountChanged -> updateForm { copy(targetAmount = event.value) }
            is GoalEvent.TargetDateChanged -> updateForm { copy(targetDateMillis = event.dateMillis) }
            is GoalEvent.DescriptionChanged -> updateForm { copy(description = event.value) }
            is GoalEvent.CategorySelected -> updateForm { copy(categoryId = event.categoryId) }
            is GoalEvent.WalletSelected -> updateForm { copy(walletId = event.walletId) }
            is GoalEvent.InitialContributionChanged -> updateForm { copy(initialContribution = event.value) }
            
            is GoalEvent.LoadGoalForEdit -> loadGoalForEdit(event.goalId)
            GoalEvent.SaveGoalClicked -> saveGoal()
            GoalEvent.UpdateGoalClicked -> updateGoal()
            
            is GoalEvent.ConfirmDelete -> handleDelete(event)
            is GoalEvent.BuyNowClicked -> handlePurchase(event)
            
            is GoalEvent.TransferAmountChanged -> updateTransferPreview(amount = event.value)
            is GoalEvent.TransferSourceWalletSelected -> updateTransferPreview(sourceId = event.walletId)
            is GoalEvent.TransferDestinationWalletSelected -> updateTransferPreview(destId = event.walletId)
            is GoalEvent.InitializeTransfer -> updateTransferPreview(sourceId = event.sourceId, destId = event.destId, amount = "0")

            is GoalEvent.ContributeClicked -> handleContribution(event)
            is GoalEvent.WithdrawClicked -> handleWithdrawal(event)
            is GoalEvent.StatusChanged -> updateGoalStatus(event.goalId, event.status)
            GoalEvent.MessageShown -> _uiState.update { it.copy(errorMessage = null, successMessage = null, lastTransferSuccess = null) }
            GoalEvent.ClearTransferState -> _uiState.update { it.copy(transferPreview = TransferPreview(), lastTransferSuccess = null) }
            else -> Unit
        }
    }

    private fun updateTransferPreview(
        amount: String? = null,
        sourceId: String? = null,
        destId: String? = null
    ) {
        val currentPreview = _uiState.value.transferPreview
        val newAmountStr = (amount ?: currentPreview.amountInput).replace(",", ".")
        val newAmount = newAmountStr.toDoubleOrNull() ?: 0.0
        val sId = sourceId ?: currentPreview.sourceWalletId
        val dId = destId ?: currentPreview.destinationWalletId

        if (sId.isEmpty() || dId.isEmpty()) {
            _uiState.update { 
                it.copy(
                    transferPreview = currentPreview.copy(
                        amountInput = newAmountStr, 
                        sourceAmount = newAmount,
                        sourceWalletId = sId,
                        destinationWalletId = dId
                    )
                ) 
            }
            return
        }

        viewModelScope.launch {
            val wallets = _uiState.value.wallets
            val sourceWallet = wallets.find { it.id == sId }
            val destWallet = wallets.find { it.id == dId }

            if (sourceWallet != null && destWallet != null) {
                val rate = conversionManager.getRate(sourceWallet.currency, destWallet.currency)
                val targetAmount = conversionManager.roundAmount(newAmount * rate)
                val isInsufficient = newAmount > sourceWallet.currentBalance

                _uiState.update { 
                    it.copy(
                        transferPreview = TransferPreview(
                            sourceWalletId = sId,
                            destinationWalletId = dId,
                            sourceAmount = newAmount,
                            sourceCurrency = sourceWallet.currency,
                            targetAmount = targetAmount,
                            targetCurrency = destWallet.currency,
                            exchangeRate = rate,
                            sourceBalanceBefore = sourceWallet.currentBalance,
                            sourceBalanceAfter = sourceWallet.currentBalance - newAmount,
                            targetBalanceBefore = destWallet.currentBalance,
                            targetBalanceAfter = destWallet.currentBalance + targetAmount,
                            isInsufficientBalance = isInsufficient,
                            sourceWalletName = sourceWallet.name,
                            targetWalletName = destWallet.name,
                            amountInput = newAmountStr
                        )
                    )
                }
            }
        }
    }

    private fun loadGoalForEdit(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = goalRepository.getGoal(id)) {
                is Resource.Success -> {
                    val goal = result.data
                    updateForm { 
                        copy(
                            goalId = goal.id,
                            title = goal.title,
                            targetAmount = goal.targetAmount.toString(),
                            targetDateMillis = goal.targetDate,
                            description = goal.description,
                            categoryId = goal.categoryId,
                            walletId = goal.walletId
                        )
                    }
                    _uiState.update { it.copy(isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun saveGoal() {
        val form = _uiState.value.form
        if (form.title.isBlank() || form.targetAmount.toDoubleOrNull() == null || form.walletId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill all required fields.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val goal = SavingsGoal(
                id = UUID.randomUUID().toString(),
                title = form.title,
                targetAmount = form.targetAmount.toDouble(),
                walletId = form.walletId,
                categoryId = form.categoryId,
                targetDate = form.targetDateMillis,
                description = form.description,
                status = GoalStatus.ACTIVE
            )
            
            val result = goalRepository.addGoal(goal)
            if (result is Resource.Success) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Goal vault created successfully!", form = GoalFormState()) }
                _eventFlow.send(GoalUiEvent.ShowToast("Goal created successfully"))
                _eventFlow.send(GoalUiEvent.GoalCreatedSuccess(goal.id))
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = (result as Resource.Error).message) }
            }
        }
    }

    private fun updateGoal() {
        val form = _uiState.value.form
        val id = form.goalId ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val goal = SavingsGoal(
                id = id,
                title = form.title,
                targetAmount = form.targetAmount.toDoubleOrNull() ?: 0.0,
                walletId = form.walletId,
                categoryId = form.categoryId,
                targetDate = form.targetDateMillis,
                description = form.description,
                status = GoalStatus.ACTIVE
            )
            val result = goalRepository.updateGoal(goal)
            if (result is Resource.Success) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Goal updated successfully!", form = GoalFormState()) }
                _eventFlow.send(GoalUiEvent.ShowToast("Goal updated successfully"))
                _eventFlow.send(GoalUiEvent.GoalUpdatedSuccess)
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = (result as Resource.Error).message) }
            }
        }
    }

    private fun handleDelete(event: GoalEvent.ConfirmDelete) {
        viewModelScope.launch {
            try {
                Log.d(GOAL_DELETE, "Delete started for goal: ${event.goalId}")
                _uiState.update { it.copy(isLoading = true) }
                val goalItem = _uiState.value.goals.find { it.goal.id == event.goalId } ?: run {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Goal not found.") }
                    return@launch
                }
                
                when (event.walletAction) {
                    WalletDeleteAction.TRANSFER_AND_DELETE -> {
                        val destId = event.destinationWalletId
                        if (destId != null && goalItem.currentAmount > 0) {
                            val transfer = Transfer("", goalItem.goal.walletId, destId, goalItem.currentAmount, "Closing Vault: ${goalItem.goal.title}", System.currentTimeMillis())
                            walletRepository.transferFunds(transfer)
                        }
                        walletRepository.deleteWallet(goalItem.goal.walletId)
                    }
                    WalletDeleteAction.ARCHIVE_WALLET -> {
                        val wallet = goalItem.wallet.copy(isArchived = true)
                        walletRepository.updateWallet(wallet)
                    }
                    WalletDeleteAction.PERMANENTLY_DELETE -> {
                        walletRepository.deleteWallet(goalItem.goal.walletId)
                    }
                }

                val result = goalRepository.deleteGoal(event.goalId)
                if (result is Resource.Success) {
                    Log.d(GOAL_NAVIGATION, "Deletion successful, emitting navigation state.")
                    _uiState.update { it.copy(isLoading = false) }
                    _eventFlow.send(GoalUiEvent.ShowToast("Goal deleted successfully"))
                    _eventFlow.send(GoalUiEvent.NavigateBack)
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to delete goal.") }
                }
            } catch (e: Exception) {
                Log.e(GOAL_DELETE, "Error during deletion", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "An error occurred during deletion.") }
            }
        }
    }

    private fun handlePurchase(event: GoalEvent.BuyNowClicked) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val goalItem = _uiState.value.goals.find { it.goal.id == event.goalId } ?: return@launch
            
            val exchangeRate = conversionManager.getRate(goalItem.wallet.currency, "LKR")
            val convertedLkr = event.amount * exchangeRate

            val expense = Expense(
                id = "",
                amount = event.amount,
                currency = goalItem.wallet.currency,
                categoryId = goalItem.goal.categoryId,
                paymentMethod = PaymentMethod.DIGITAL_WALLET,
                walletId = goalItem.goal.walletId,
                date = System.currentTimeMillis(),
                notes = event.note.ifBlank { "Goal Purchase: ${goalItem.goal.title}" },
                exchangeRate = exchangeRate,
                convertedAmountLkr = convertedLkr,
                transactionType = TransactionType.REGULAR,
                goalId = goalItem.goal.id,
                generated = true,
                isDiscretionary = false,
                createdAt = System.currentTimeMillis()
            )

            val expenseRes = expenseRepository.addExpense(expense)
            if (expenseRes is Resource.Success) {
                walletRepository.updateBalance(goalItem.goal.walletId, event.amount, false)
                
                val completedGoal = goalItem.goal.copy(status = GoalStatus.COMPLETED, completedAt = System.currentTimeMillis())
                goalRepository.updateGoal(completedGoal)
                
                Log.d(GOAL_PURCHASE, "Purchase completed for ${goalItem.goal.title}")
                _uiState.update { it.copy(isLoading = false, successMessage = "Congratulations! Purchase successful.") }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Purchase failed.") }
            }
        }
    }

    fun createVaultAndGoal(walletName: String, currency: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val wallet = Wallet(
                id = "",
                name = walletName,
                type = WalletType.SAVINGS,
                currentBalance = 0.0,
                currency = currency,
                iconKey = "vault",
                colorKey = "#FFC107"
            )
            
            val walletRes = walletRepository.addWallet(wallet)
            if (walletRes is Resource.Success) {
                val realId = walletRes.data
                _uiState.update { it.copy(form = it.form.copy(walletId = realId), isLoading = false) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to create vault wallet.") }
            }
        }
    }

    private fun handleContribution(event: GoalEvent.ContributeClicked) {
        viewModelScope.launch {
            val preview = _uiState.value.transferPreview
            _uiState.update { it.copy(isLoading = true) }
            val goalItem = _uiState.value.goals.find { it.goal.id == event.goalId } ?: return@launch
            
            val transfer = Transfer(
                id = "",
                sourceWalletId = event.sourceWalletId,
                destinationWalletId = goalItem.goal.walletId,
                amount = event.amount,
                notes = event.note.ifBlank { "Contribution to ${goalItem.goal.title}" },
                date = System.currentTimeMillis()
            )

            Log.d(GOAL_TRANSFER_LOG, "Initiating cross-currency contribution...")
            Log.d(GOAL_CURRENCY, "Source: ${preview.sourceCurrency}, Target: ${preview.targetCurrency}")
            Log.d(GOAL_EXCHANGE, "Rate: ${preview.exchangeRate}, Amount: ${event.amount} -> ${preview.targetAmount}")

            val result = walletRepository.transferFunds(transfer)
            if (result is Resource.Success) {
                Log.d(GOAL_TRANSFER_LOG, "Contribution successful: ${event.amount} ${preview.sourceCurrency} converted to ${preview.targetAmount} ${preview.targetCurrency}")
                _uiState.update { it.copy(
                    isLoading = false, 
                    successMessage = "Contribution successful!",
                    lastTransferSuccess = preview,
                    transferPreview = TransferPreview()
                ) }
            } else {
                Log.e(GOAL_TRANSFER_LOG, "Contribution failed: ${(result as Resource.Error).message}")
                _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    private fun handleWithdrawal(event: GoalEvent.WithdrawClicked) {
        viewModelScope.launch {
            val preview = _uiState.value.transferPreview
            _uiState.update { it.copy(isLoading = true) }
            val goalItem = _uiState.value.goals.find { it.goal.id == event.goalId } ?: return@launch
            
            val transfer = Transfer(
                id = "",
                sourceWalletId = goalItem.goal.walletId,
                destinationWalletId = event.destinationWalletId,
                amount = event.amount,
                notes = event.note.ifBlank { "Withdrawal from ${goalItem.goal.title}" },
                date = System.currentTimeMillis()
            )

            Log.d(GOAL_TRANSFER_LOG, "Initiating cross-currency withdrawal...")
            Log.d(GOAL_CURRENCY, "Source: ${preview.sourceCurrency}, Target: ${preview.targetCurrency}")
            Log.d(GOAL_EXCHANGE, "Rate: ${preview.exchangeRate}, Amount: ${event.amount} -> ${preview.targetAmount}")
            
            val result = walletRepository.transferFunds(transfer)
            if (result is Resource.Success) {
                Log.d(GOAL_TRANSFER_LOG, "Withdrawal successful: ${event.amount} ${preview.sourceCurrency} converted to ${preview.targetAmount} ${preview.targetCurrency}")
                _uiState.update { it.copy(
                    isLoading = false, 
                    successMessage = "Withdrawal successful!",
                    lastTransferSuccess = preview,
                    transferPreview = TransferPreview()
                ) }
            } else {
                Log.e(GOAL_TRANSFER_LOG, "Withdrawal failed: ${(result as Resource.Error).message}")
                _uiState.update { it.copy(isLoading = false, errorMessage = (result as Resource.Error).message) }
            }
        }
    }

    private fun updateGoalStatus(goalId: String, status: GoalStatus) {
        viewModelScope.launch {
            val goalItem = _uiState.value.goals.find { it.goal.id == goalId } ?: return@launch
            val updated = goalItem.goal.copy(status = status)
            goalRepository.updateGoal(updated)
        }
    }

    private fun updateForm(reducer: GoalFormState.() -> GoalFormState) {
        _uiState.update { it.copy(form = it.form.reducer()) }
    }
}
