package com.example.projectpbd.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.core.util.RecurrenceCalculator
import com.example.projectpbd.domain.model.*
import com.example.projectpbd.domain.repository.*
import com.example.projectpbd.presentation.dashboard.state.*
import com.example.projectpbd.presentation.wallets.state.WalletItemUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val settingsRepository: SettingsRepository,
    private val exchangeRateRepository: ExchangeRateRepository
) : ViewModel() {
    private val formatter = DateTimeFormatter.ofPattern("MMM d")

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeDashboard()
    }

    private fun observeDashboard() {
        viewModelScope.launch {
            val incomeFlow = incomeRepository.observeIncomes()
            val expenseFlow = expenseRepository.observeExpenses()
            val goalFlow = goalRepository.observeGoals()
            val categoryFlow = categoryRepository.observeCategories()
            val walletFlow = walletRepository.observeWallets()
            val settingsFlow = settingsRepository.getSettings()

            combine(
                incomeFlow,
                expenseFlow,
                goalFlow,
                categoryFlow,
                walletFlow,
                settingsFlow
            ) { args ->
                val incomesRes = args[0] as Resource<List<Income>>
                val expensesRes = args[1] as Resource<List<Expense>>
                val goalsRes = args[2] as Resource<List<SavingsGoal>>
                val categoriesRes = args[3] as Resource<List<Category>>
                val walletsRes = args[4] as Resource<List<Wallet>>
                val settings = args[5] as AppSettings

                val isLoading = listOf(incomesRes, expensesRes, goalsRes, categoriesRes, walletsRes).any { it is Resource.Loading }
                val error = firstError(incomesRes, expensesRes, goalsRes, categoriesRes, walletsRes)

                val incomeItems = (incomesRes as? Resource.Success)?.data.orEmpty()
                val expenseItems = (expensesRes as? Resource.Success)?.data.orEmpty()
                val goalItems = (goalsRes as? Resource.Success)?.data.orEmpty()
                val categoryItems = (categoriesRes as? Resource.Success)?.data.orEmpty()
                val walletItems = (walletsRes as? Resource.Success)?.data.orEmpty()
                val baseCurrency = settings.baseCurrency

                // Map Wallets to UI items with conversion
                val walletUiItems = walletItems.map { wallet ->
                    val rate = exchangeRateRepository.getRate(wallet.currency, baseCurrency)
                    val convertedBalance = wallet.currentBalance * rate
                    val currencyInfo = CurrencyRegistry.getByCode(wallet.currency)
                    WalletItemUi(
                        id = wallet.id,
                        name = wallet.name,
                        type = wallet.type,
                        originalBalance = wallet.currentBalance,
                        convertedBalanceLkr = convertedBalance,
                        currencyCode = wallet.currency,
                        currencySymbol = currencyInfo.symbol,
                        flagEmoji = currencyInfo.flag
                    )
                }

                // Calculate Snapshot in base currency
                val snapshot = buildSnapshot(walletItems, incomeItems, expenseItems, baseCurrency)
                val cashFlow = buildCashFlow(incomeItems, expenseItems, categoryItems, baseCurrency)
                val goals = buildGoals(goalItems, walletItems)
                val recurring = buildUpcomingRecurring(incomeItems, expenseItems)
                val insights = generateInsights(walletItems, incomeItems, expenseItems, baseCurrency)
                val activity = buildRecentActivity(incomeItems, expenseItems, categoryItems, walletItems, baseCurrency)

                DashboardUiState(
                    isLoading = isLoading,
                    errorMessage = error,
                    baseCurrency = baseCurrency,
                    snapshot = snapshot,
                    wallets = walletUiItems,
                    cashFlow = cashFlow,
                    goals = goals,
                    upcomingRecurring = recurring,
                    insights = insights,
                    recentActivity = activity
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    private suspend fun buildSnapshot(wallets: List<Wallet>, incomes: List<Income>, expenses: List<Expense>, baseCurrency: String): FinancialSnapshotUi {
        val currentMonth = currentMonthMillis()
        
        var totalAvailable = 0.0
        wallets.forEach { 
            val rate = exchangeRateRepository.getRate(it.currency, baseCurrency)
            totalAvailable += it.currentBalance * rate
        }
        
        val monthlyIncomes = incomes.filter { isSameMonth(it.date, currentMonth) }
        val totalIncome = monthlyIncomes.sumOf { 
            exchangeRateRepository.convert(it.amount, it.currency, baseCurrency)
        }
        
        val monthlyExpenses = expenses.filter { isSameMonth(it.date, currentMonth) && it.transactionType == TransactionType.REGULAR }
        val totalExpense = monthlyExpenses.sumOf { 
            exchangeRateRepository.convert(it.amount, it.currency, baseCurrency)
        }

        val netCashFlow = totalIncome - totalExpense
        
        val goalAllocations = expenses.filter { isSameMonth(it.date, currentMonth) && it.transactionType == TransactionType.GOAL_ALLOCATION }
            .sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        val savingsMomentum = totalIncome - totalExpense - goalAllocations

        return FinancialSnapshotUi(
            totalAvailable = totalAvailable,
            monthlyIncome = totalIncome,
            monthlyExpense = totalExpense,
            savingsMomentum = savingsMomentum,
            netCashFlow = netCashFlow
        )
    }

    private suspend fun buildCashFlow(incomes: List<Income>, expenses: List<Expense>, categories: List<Category>, baseCurrency: String): CashFlowInsightUi {
        val currentMonth = currentMonthMillis()
        val totalIncome = incomes.filter { isSameMonth(it.date, currentMonth) }
            .sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        val regularExpenses = expenses.filter { isSameMonth(it.date, currentMonth) && it.transactionType == TransactionType.REGULAR }
        val totalExpense = regularExpenses.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }

        val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome).toFloat() else 0f
        
        val categoryMap = categories.associateBy { it.id }
        val topCategoryEntry = regularExpenses
            .groupBy { it.categoryId }
            .mapValues { entry -> 
                entry.value.sumOf { e -> exchangeRateRepository.convert(e.amount, e.currency, baseCurrency) }
            }
            .maxByOrNull { it.value }

        val topCategoryName = categoryMap[topCategoryEntry?.key]?.name
        val drainFactor = if (totalIncome > 0) totalExpense / totalIncome else 0.0

        return CashFlowInsightUi(
            savingsRate = savingsRate.coerceIn(0f, 1f),
            topCategory = topCategoryName,
            topCategoryAmount = topCategoryEntry?.value ?: 0.0,
            walletDrainFactor = drainFactor
        )
    }

    private fun buildGoals(goals: List<SavingsGoal>, wallets: List<Wallet>): List<GoalMomentumUi> {
        val walletMap = wallets.associateBy { it.id }
        return goals.filter { it.status == GoalStatus.ACTIVE }.take(2).map { goal ->
            val wallet = walletMap[goal.walletId]
            val currentAmount = wallet?.currentBalance ?: 0.0
            val progress = if (goal.targetAmount > 0) (currentAmount / goal.targetAmount).toFloat() else 0f
            GoalMomentumUi(
                id = goal.id,
                title = goal.title,
                currentAmount = currentAmount,
                targetAmount = goal.targetAmount,
                progress = progress.coerceIn(0f, 1f),
                projectedDate = null // Calculated in detailed analytics now
            )
        }
    }

    private fun buildUpcomingRecurring(incomes: List<Income>, expenses: List<Expense>): List<UpcomingRecurringUi> {
        val currentTime = System.currentTimeMillis()
        val upcomingExpenses = expenses.filter { it.repeatConfig.isEnabled && it.repeatConfig.isActive && it.repeatConfig.nextExecutionDate > currentTime }
            .map { 
                val currencyInfo = CurrencyRegistry.getByCode(it.currency)
                UpcomingRecurringUi(
                    id = it.id,
                    title = it.notes.ifBlank { "Recurring Expense" },
                    amount = it.amount,
                    currencyCode = it.currency,
                    currencySymbol = currencyInfo.symbol,
                    dateLabel = formatDate(it.repeatConfig.nextExecutionDate),
                    isIncome = false,
                    frequencyLabel = RecurrenceCalculator.getRepeatSummary(it.repeatConfig)
                )
            }
        
        val upcomingIncomes = incomes.filter { it.repeatConfig.isEnabled && it.repeatConfig.isActive && it.repeatConfig.nextExecutionDate > currentTime }
            .map {
                val currencyInfo = CurrencyRegistry.getByCode(it.currency)
                UpcomingRecurringUi(
                    id = it.id,
                    title = it.notes.ifBlank { "Recurring Income" },
                    amount = it.amount,
                    currencyCode = it.currency,
                    currencySymbol = currencyInfo.symbol,
                    dateLabel = formatDate(it.repeatConfig.nextExecutionDate),
                    isIncome = true,
                    frequencyLabel = RecurrenceCalculator.getRepeatSummary(it.repeatConfig)
                )
            }

        return (upcomingExpenses + upcomingIncomes).sortedBy { it.dateLabel }.take(3)
    }

    private suspend fun generateInsights(wallets: List<Wallet>, incomes: List<Income>, expenses: List<Expense>, baseCurrency: String): List<BehavioralInsightUi> {
        val insights = mutableListOf<BehavioralInsightUi>()
        val currentMonth = currentMonthMillis()
        
        val monthlyIncome = incomes.filter { isSameMonth(it.date, currentMonth) }
            .sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        val monthlyExpense = expenses.filter { isSameMonth(it.date, currentMonth) && it.transactionType == TransactionType.REGULAR }
            .sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        
        if (monthlyIncome > 0) {
            val rate = (monthlyIncome - monthlyExpense) / monthlyIncome
            if (rate > 0.2) {
                insights.add(BehavioralInsightUi("savings_good", "Great job! Your savings rate is ${ (rate * 100).toInt() }% this month.", com.example.projectpbd.presentation.dashboard.state.InsightType.POSITIVE))
            } else if (rate < 0.05 && rate > 0) {
                insights.add(BehavioralInsightUi("savings_low", "Your savings rate is a bit low this month. Try to minimize discretionary spending.", com.example.projectpbd.presentation.dashboard.state.InsightType.NEUTRAL))
            } else if (rate <= 0) {
                insights.add(BehavioralInsightUi("savings_neg", "You've spent more than you earned this month. Watch your wallet drain!", com.example.projectpbd.presentation.dashboard.state.InsightType.NEGATIVE))
            }
        }

        if (wallets.size > 1) {
            val walletBalancesBase = wallets.associate { 
                it.id to it.currentBalance * exchangeRateRepository.getRate(it.currency, baseCurrency)
            }
            val totalNetWorth = walletBalancesBase.values.sum()
            val mainWalletEntry = walletBalancesBase.maxByOrNull { it.value }
            
            if (mainWalletEntry != null && totalNetWorth > 0) {
                val ratio = mainWalletEntry.value / totalNetWorth
                if (ratio > 0.8) {
                    val walletName = wallets.find { it.id == mainWalletEntry.key }?.name ?: "one wallet"
                    insights.add(BehavioralInsightUi("wallet_dist", "80%+ of your funds are in $walletName. Consider distributing your assets.", com.example.projectpbd.presentation.dashboard.state.InsightType.NEUTRAL))
                }
            }
        }

        return insights.take(3)
    }

    private suspend fun buildRecentActivity(
        incomes: List<Income>,
        expenses: List<Expense>,
        categories: List<Category>,
        wallets: List<Wallet>,
        baseCurrency: String
    ): List<ActivityItemUi> {
        val categoryMap = categories.associateBy { it.id }
        val walletMap = wallets.associateBy { it.id }

        val incomeItems = incomes.map {
            val currencyInfo = CurrencyRegistry.getByCode(it.currency)
            val convertedAmount = exchangeRateRepository.convert(it.amount, it.currency, baseCurrency)
            ActivityItemUi(
                id = it.id,
                label = it.notes.ifBlank { categoryMap[it.categoryId]?.name ?: "Income" },
                amount = it.amount,
                convertedAmountLkr = convertedAmount,
                currencyCode = it.currency,
                currencySymbol = currencyInfo.symbol,
                dateLabel = formatDate(it.date),
                isIncome = true,
                walletName = walletMap[it.walletId]?.name,
                categoryName = categoryMap[it.categoryId]?.name,
                notes = it.notes,
                timestamp = it.date
            )
        }
        val expenseItems = expenses.map {
            val currencyInfo = CurrencyRegistry.getByCode(it.currency)
            val convertedAmount = exchangeRateRepository.convert(it.amount, it.currency, baseCurrency)
            ActivityItemUi(
                id = it.id,
                label = it.notes.ifBlank { if (it.transactionType == TransactionType.GOAL_ALLOCATION) "Goal Allocation" else (categoryMap[it.categoryId]?.name ?: "Expense") },
                amount = it.amount,
                convertedAmountLkr = convertedAmount,
                currencyCode = it.currency,
                currencySymbol = currencyInfo.symbol,
                dateLabel = formatDate(it.date),
                isIncome = false,
                walletName = walletMap[it.walletId]?.name,
                categoryName = categoryMap[it.categoryId]?.name,
                notes = it.notes,
                timestamp = it.date
            )
        }
        return (incomeItems + expenseItems)
            .sortedByDescending { it.timestamp }
            .take(5)
    }

    private fun currentMonthMillis(): Long {
        return ZonedDateTime.now().toInstant().toEpochMilli()
    }

    private fun isSameMonth(dateMillis: Long, referenceMillis: Long): Boolean {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(dateMillis).atZone(zone)
        val reference = Instant.ofEpochMilli(referenceMillis).atZone(zone)
        return date.year == reference.year && date.month == reference.month
    }

    private fun formatDate(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    private fun firstError(vararg resources: Resource<*>): String? {
        return resources.filterIsInstance<Resource.Error>().firstOrNull()?.message
    }
}
