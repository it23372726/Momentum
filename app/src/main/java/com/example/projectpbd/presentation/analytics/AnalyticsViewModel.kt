package com.example.projectpbd.presentation.analytics

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.*
import com.example.projectpbd.domain.repository.*
import com.example.projectpbd.presentation.analytics.state.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val settingsRepository: SettingsRepository,
    private val exchangeRateRepository: ExchangeRateRepository
) : ViewModel() {

    private val INSIGHT_DEBUG = "INSIGHT_DEBUG"
    private val WALLET_INTELLIGENCE = "WALLET_INTELLIGENCE"
    private val CONVERSION_DEBUG = "CONVERSION_DEBUG"

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeAnalytics()
    }

    fun onEvent(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.TimeframeChanged -> {
                _uiState.update { it.copy(selectedTimeframe = event.timeframe) }
            }
            AnalyticsEvent.Refresh -> observeAnalytics()
        }
    }

    private fun observeAnalytics() {
        viewModelScope.launch {
            val incomeFlow = incomeRepository.observeIncomes()
            val expenseFlow = expenseRepository.observeExpenses()
            val goalFlow = goalRepository.observeGoals()
            val categoryFlow = categoryRepository.observeCategories()
            val walletFlow = walletRepository.observeWallets()
            val settingsFlow = settingsRepository.getSettings()
            val timeframeFlow = _uiState.map { it.selectedTimeframe }.distinctUntilChanged()

            combine(
                incomeFlow,
                expenseFlow,
                goalFlow,
                categoryFlow,
                walletFlow,
                settingsFlow,
                timeframeFlow
            ) { args ->
                val incomes = args[0] as Resource<List<Income>>
                val expenses = args[1] as Resource<List<Expense>>
                val goals = args[2] as Resource<List<SavingsGoal>>
                val categories = args[3] as Resource<List<Category>>
                val wallets = args[4] as Resource<List<Wallet>>
                val settings = args[5] as AppSettings
                val timeframe = args[6] as TrendTimeframe

                val isLoading = listOf(incomes, expenses, goals, categories, wallets).any { it is Resource.Loading }
                val error = firstError(incomes, expenses, goals, categories, wallets)

                val incomeItems = (incomes as? Resource.Success<List<Income>>)?.data.orEmpty()
                val expenseItems = (expenses as? Resource.Success<List<Expense>>)?.data.orEmpty()
                val goalItems = (goals as? Resource.Success<List<SavingsGoal>>)?.data.orEmpty()
                val categoryItems = (categories as? Resource.Success<List<Category>>)?.data.orEmpty()
                val walletItems = (wallets as? Resource.Success<List<Wallet>>)?.data.orEmpty()
                val baseCurrency = settings.baseCurrency

                Log.d(CONVERSION_DEBUG, "Calculating analytics with base currency: $baseCurrency")

                calculateState(incomeItems, expenseItems, goalItems, categoryItems, walletItems, isLoading, error, timeframe, baseCurrency)
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    private suspend fun calculateState(
        incomes: List<Income>,
        expenses: List<Expense>,
        goals: List<SavingsGoal>,
        categories: List<Category>,
        wallets: List<Wallet>,
        isLoading: Boolean,
        error: String?,
        timeframe: TrendTimeframe,
        baseCurrency: String
    ): AnalyticsUiState {
        val currentMonth = currentMonthMillis()
        val monthlyIncomes = incomes.filter { isSameMonth(it.date, currentMonth) }
        val monthlyExpenses = expenses.filter { isSameMonth(it.date, currentMonth) }

        val health = calculateFinancialHealth(wallets, monthlyIncomes, monthlyExpenses, incomes, expenses, baseCurrency)
        val spending = calculateSpending(monthlyExpenses, categories, baseCurrency)
        val incomeDist = calculateIncomeDist(monthlyIncomes, categories, baseCurrency)
        val walletIntel = calculateWalletIntelligence(wallets, incomes, expenses, baseCurrency)
        val currencyExposure = calculateCurrencyExposure(wallets, baseCurrency)
        val recurringImpact = calculateRecurringImpact(monthlyIncomes, monthlyExpenses, baseCurrency)
        val trends = calculateTrends(incomes, expenses, timeframe, baseCurrency)
        val insights = generateInsights(monthlyIncomes, monthlyExpenses, goals, categories, wallets, baseCurrency)
        val score = calculateAwarenessScore(incomes, expenses, goals, baseCurrency)
        val goalAnalytics = calculateGoalAnalytics(goals, wallets, baseCurrency)

        return AnalyticsUiState(
            isLoading = isLoading,
            baseCurrency = baseCurrency,
            financialHealth = health,
            spendingBreakdown = spending,
            incomeDistribution = incomeDist,
            walletIntelligence = walletIntel,
            currencyExposure = currencyExposure,
            recurringImpact = recurringImpact,
            trends = trends,
            selectedTimeframe = timeframe,
            insights = insights,
            awarenessScore = score,
            goalAnalytics = goalAnalytics,
            errorMessage = error
        )
    }

    private suspend fun calculateFinancialHealth(
        wallets: List<Wallet>,
        monthlyIncomes: List<Income>,
        monthlyExpenses: List<Expense>,
        allIncomes: List<Income>,
        allExpenses: List<Expense>,
        baseCurrency: String
    ): FinancialHealthUi {
        var totalAvailable = 0.0
        wallets.forEach { 
            val rate = exchangeRateRepository.getRate(it.currency, baseCurrency)
            totalAvailable += it.currentBalance * rate
        }
        
        val currentIncome = monthlyIncomes.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        val currentExpense = monthlyExpenses.filter { it.transactionType == TransactionType.REGULAR }
            .sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        
        val netCashFlow = currentIncome - currentExpense

        val now = System.currentTimeMillis()
        val endOfMonth = ZonedDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).toInstant().toEpochMilli()
        
        var upcomingIncome = 0.0
        allIncomes.filter { it.repeatConfig.isEnabled && it.repeatConfig.isActive }.forEach { income ->
            var next = income.repeatConfig.nextExecutionDate
            while (next > now && next <= endOfMonth) {
                upcomingIncome += exchangeRateRepository.convert(income.amount, income.currency, baseCurrency)
                next = calculateNextPotentialOccurrence(income.repeatConfig, next)
            }
        }
        
        var upcomingExpense = 0.0
        allExpenses.filter { it.repeatConfig.isEnabled && it.repeatConfig.isActive }.forEach { expense ->
            var next = expense.repeatConfig.nextExecutionDate
            while (next > now && next <= endOfMonth) {
                upcomingExpense += exchangeRateRepository.convert(expense.amount, expense.currency, baseCurrency)
                next = calculateNextPotentialOccurrence(expense.repeatConfig, next)
            }
        }

        val projectedNet = netCashFlow + upcomingIncome - upcomingExpense
        
        val savingsRate = if (totalIncomeBase(monthlyIncomes, baseCurrency) > 0) ((currentIncome - currentExpense) / currentIncome) * 100 else 0.0
        
        val healthScore = ((savingsRate.coerceIn(0.0, 50.0) * 2).toInt() + 
                          (if (totalAvailable > currentExpense * 2) 20 else 0)).coerceAtMost(100)

        return FinancialHealthUi(
            totalAvailable = totalAvailable,
            savingsRate = savingsRate,
            expenseIncomeRatio = if (currentIncome > 0) (currentExpense / currentIncome).toFloat() else 0f,
            netCashFlow = netCashFlow,
            projectedNetCashFlow = projectedNet,
            healthScore = healthScore,
            stabilityLabel = when {
                healthScore > 80 -> "Resilient"
                healthScore > 60 -> "Strong"
                healthScore > 40 -> "Stable"
                else -> "Vulnerable"
            }
        )
    }

    private suspend fun totalIncomeBase(incomes: List<Income>, baseCurrency: String): Double {
        return incomes.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
    }

    private fun calculateNextPotentialOccurrence(config: RepeatConfiguration, from: Long): Long {
        val zdt = Instant.ofEpochMilli(from).atZone(ZoneId.systemDefault())
        val next = when (config.frequency) {
            RepeatFrequency.DAILY -> zdt.plusDays(config.interval.toLong())
            RepeatFrequency.WEEKLY -> zdt.plusWeeks(config.interval.toLong())
            RepeatFrequency.MONTHLY -> zdt.plusMonths(config.interval.toLong())
            RepeatFrequency.ANNUALLY -> zdt.plusYears(config.interval.toLong())
            else -> zdt
        }
        return next.toInstant().toEpochMilli()
    }

    private suspend fun calculateWalletIntelligence(
        wallets: List<Wallet>,
        incomes: List<Income>,
        expenses: List<Expense>,
        baseCurrency: String
    ): WalletIntelligenceUi {
        if (wallets.isEmpty()) return WalletIntelligenceUi()
        
        val walletBalancesBase = wallets.associate { 
            it.id to it.currentBalance * exchangeRateRepository.getRate(it.currency, baseCurrency)
        }
        
        val totalNetWorth = walletBalancesBase.values.sum()
        val totalExpensesBase = expenses.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }

        val usage = wallets.map { wallet ->
            val balanceBase = walletBalancesBase[wallet.id] ?: 0.0
            val balanceWeight = if (totalNetWorth > 0) (balanceBase / totalNetWorth).toFloat() else 0f
            
            val walletExpensesBase = expenses.filter { it.walletId == wallet.id }
                .sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
            val pressure = if (totalExpensesBase > 0) (walletExpensesBase / totalExpensesBase).toFloat() else 0f
            
            WalletUsageUi(
                walletId = wallet.id,
                walletName = wallet.name,
                balance = wallet.currentBalance,
                balanceInBase = balanceBase,
                usagePercentage = balanceWeight,
                spendingPressure = pressure
            )
        }.sortedByDescending { it.balanceInBase }

        val expenseByWalletBase = expenses.groupBy { it.walletId }
            .mapValues { entry -> entry.value.sumOf { e -> exchangeRateRepository.convert(e.amount, e.currency, baseCurrency) } }
        
        val fastestDraining = wallets.maxByOrNull { expenseByWalletBase[it.id] ?: 0.0 }?.name ?: "None"
        val mostUsed = wallets.maxByOrNull { 
            (expenseByWalletBase[it.id] ?: 0.0) + (incomes.filter { i -> i.walletId == it.id }.sumOf { i -> exchangeRateRepository.convert(i.amount, i.currency, baseCurrency) }) 
        }?.name ?: "None"

        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        val dormantCount = wallets.count { wallet ->
            val hasRecentIncome = incomes.any { it.walletId == wallet.id && it.date > thirtyDaysAgo }
            val hasRecentExpense = expenses.any { it.walletId == wallet.id && it.date > thirtyDaysAgo }
            !hasRecentIncome && !hasRecentExpense
        }

        val maxWeight = usage.firstOrNull()?.usagePercentage ?: 0f

        return WalletIntelligenceUi(
            items = usage,
            mostUsedWalletName = mostUsed,
            fastestDrainingWalletName = fastestDraining,
            dormantWalletCount = dormantCount,
            walletDependencyScore = maxWeight,
            totalNetWorth = totalNetWorth
        )
    }

    private suspend fun calculateCurrencyExposure(
        wallets: List<Wallet>,
        baseCurrency: String
    ): List<CurrencyExposureUi> {
        val balancesByCurrency = wallets.groupBy { it.currency }
            .mapValues { entry -> 
                val sumNative = entry.value.sumOf { it.currentBalance }
                sumNative * exchangeRateRepository.getRate(entry.key, baseCurrency)
            }
        
        val totalInBase = balancesByCurrency.values.sum()
        if (totalInBase == 0.0) return emptyList()

        return balancesByCurrency.map { (code, amountBase) ->
            val currencyInfo = CurrencyRegistry.getByCode(code)
            CurrencyExposureUi(
                currencyCode = code,
                percentage = (amountBase / totalInBase).toFloat(),
                amountInBase = amountBase,
                flag = currencyInfo.flag
            )
        }.sortedByDescending { it.percentage }
    }

    private suspend fun calculateRecurringImpact(incomes: List<Income>, expenses: List<Expense>, baseCurrency: String): RecurringImpactUi {
        val totalIncome = incomes.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        val recurringExpenses = expenses.filter { it.repeatConfig.isEnabled }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        val recurringIncomes = incomes.filter { it.repeatConfig.isEnabled }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        
        val upcomingCount = expenses.count { it.repeatConfig.isEnabled && it.repeatConfig.isActive }

        return RecurringImpactUi(
            monthlyBurdenAmount = recurringExpenses,
            burdenPercentage = if (totalIncome > 0) (recurringExpenses / totalIncome).toFloat() else 0f,
            recurringIncomeReliability = if (totalIncome > 0) (recurringIncomes / totalIncome).toFloat() else 0f,
            upcomingRecurringCount = upcomingCount
        )
    }

    private suspend fun calculateSpending(expenses: List<Expense>, categories: List<Category>, baseCurrency: String): SpendingBreakdownUi {
        val regularExpenses = expenses.filter { it.transactionType == TransactionType.REGULAR }
        val totalBase = regularExpenses.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        if (totalBase == 0.0) return SpendingBreakdownUi()

        val categoryMap = categories.associateBy { it.id }

        val categoryBreakdown = regularExpenses.groupBy { it.categoryId }
            .map { (catId, list) ->
                val sumBase = list.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
                val category = categoryMap[catId]
                CategorySpendingUi(
                    categoryId = catId,
                    categoryName = category?.name ?: "Unknown",
                    amount = sumBase,
                    percentage = (sumBase / totalBase).toFloat()
                )
            }
            .sortedByDescending { it.amount }

        val discretionaryBase = regularExpenses.filter { it.isDiscretionary }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }

        return SpendingBreakdownUi(
            totalMonthlySpending = totalBase,
            categories = categoryBreakdown,
            discretionaryRatio = (discretionaryBase / totalBase).toFloat()
        )
    }

    private suspend fun calculateIncomeDist(incomes: List<Income>, categories: List<Category>, baseCurrency: String): IncomeDistributionUi {
        val totalBase = incomes.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        if (totalBase == 0.0) return IncomeDistributionUi()

        val categoryMap = categories.associateBy { it.id }

        val sources = incomes.groupBy { it.categoryId }
            .map { (catId, list) ->
                val sumBase = list.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
                val category = categoryMap[catId]
                SourceIncomeUi(
                    categoryId = catId,
                    categoryName = category?.name ?: "Unknown",
                    amount = sumBase,
                    percentage = (sumBase / totalBase).toFloat()
                )
            }
            .sortedByDescending { it.amount }

        val mainIncomeBase = sources.firstOrNull()?.amount ?: 0.0
        val sideRatio = if (totalBase > 0) ((totalBase - mainIncomeBase) / totalBase).toFloat() else 0f

        return IncomeDistributionUi(
            totalMonthlyIncome = totalBase,
            sources = sources,
            sideIncomeRatio = sideRatio
        )
    }

    private suspend fun calculateTrends(
        incomes: List<Income>,
        expenses: List<Expense>,
        timeframe: TrendTimeframe,
        baseCurrency: String
    ): List<TrendPointUi> {
        val now = ZonedDateTime.now()
        return when (timeframe) {
            TrendTimeframe.SEVEN_DAYS -> {
                (0..6).reversed().map { dayOffset ->
                    val targetDate = now.minusDays(dayOffset.toLong())
                    val label = targetDate.format(DateTimeFormatter.ofPattern("EEE"))
                    val dailyIncomes = incomes.filter { isSameDay(it.date, targetDate) }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
                    val dailyExpenses = expenses.filter { isSameDay(it.date, targetDate) }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
                    TrendPointUi(label, dailyIncomes, dailyExpenses, (dailyIncomes - dailyExpenses))
                }
            }
            TrendTimeframe.THIRTY_DAYS -> {
                (0..3).reversed().map { weekOffset ->
                    val end = now.minusWeeks(weekOffset.toLong()).with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
                    val start = end.minusDays(6)
                    val label = "W${4 - weekOffset}"
                    val wIncomes = incomes.filter { i -> 
                        val d = Instant.ofEpochMilli(i.date).atZone(ZoneId.systemDefault())
                        !d.isBefore(start) && !d.isAfter(end)
                    }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
                    val wExpenses = expenses.filter { e -> 
                        val d = Instant.ofEpochMilli(e.date).atZone(ZoneId.systemDefault())
                        !d.isBefore(start) && !d.isAfter(end)
                    }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
                    TrendPointUi(label, wIncomes, wExpenses, (wIncomes - wExpenses))
                }
            }
            TrendTimeframe.THREE_MONTHS, TrendTimeframe.SIX_MONTHS -> {
                val months = if (timeframe == TrendTimeframe.THREE_MONTHS) 3 else 6
                (0 until months).reversed().map { monthOffset ->
                    val targetMonth = now.minusMonths(monthOffset.toLong())
                    val label = targetMonth.format(DateTimeFormatter.ofPattern("MMM"))
                    val monthlyIncomes = incomes.filter { isSameMonth(it.date, targetMonth) }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
                    val monthlyExpenses = expenses.filter { isSameMonth(it.date, targetMonth) }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
                    TrendPointUi(label, monthlyIncomes, monthlyExpenses, (monthlyIncomes - monthlyExpenses))
                }
            }
            TrendTimeframe.ONE_YEAR -> {
                (0..5).reversed().map { biMonthOffset ->
                    val target = now.minusMonths(biMonthOffset.toLong() * 2)
                    val label = target.format(DateTimeFormatter.ofPattern("MMM"))
                    val mIncomes = incomes.filter { isSameMonth(it.date, target) }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
                    val mExpenses = expenses.filter { isSameMonth(it.date, target) }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
                    TrendPointUi(label, mIncomes, mExpenses, (mIncomes - mExpenses))
                }
            }
        }
    }

    private fun isSameDay(dateMillis: Long, reference: ZonedDateTime): Boolean {
        val date = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault())
        return date.year == reference.year && date.dayOfYear == reference.dayOfYear
    }

    private fun isSameMonth(dateMillis: Long, reference: ZonedDateTime): Boolean {
        val date = Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault())
        return date.year == reference.year && date.month == reference.month
    }

    private fun isSameMonth(dateMillis: Long, referenceMillis: Long): Boolean {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(dateMillis).atZone(zone)
        val reference = Instant.ofEpochMilli(referenceMillis).atZone(zone)
        return date.year == reference.year && date.month == reference.month
    }

    private suspend fun generateInsights(
        monthlyIncomes: List<Income>,
        monthlyExpenses: List<Expense>,
        goals: List<SavingsGoal>,
        categories: List<Category>,
        wallets: List<Wallet>,
        baseCurrency: String
    ): List<BehavioralInsightUi> {
        val insights = mutableListOf<BehavioralInsightUi>()
        val currentIncome = monthlyIncomes.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        val regularSpent = monthlyExpenses.filter { it.transactionType == TransactionType.REGULAR }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        
        val savingsRate = if (currentIncome > 0) ((currentIncome - regularSpent) / currentIncome) * 100 else 0.0
        
        if (savingsRate > 30) {
            insights.add(BehavioralInsightUi("High Momentum", "Excellent! You've saved ${savingsRate.roundToInt()}% of your income this month.", InsightType.POSITIVE))
        } else if (savingsRate < 10 && currentIncome > 0) {
            insights.add(BehavioralInsightUi("Cash Pressure", "Your savings rate is below 10%. Consider reviewing discretionary spending.", InsightType.NEGATIVE))
        }

        val recurringBurden = monthlyExpenses.filter { it.repeatConfig.isEnabled }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        if (currentIncome > 0 && recurringBurden > currentIncome * 0.4) {
            insights.add(BehavioralInsightUi("Recurring Load", "Recurring payments consume ${( (recurringBurden/currentIncome)*100 ).toInt()}% of your income.", InsightType.NEGATIVE))
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
                    insights.add(BehavioralInsightUi("Wallet Concentration", "Over 80% of your funds are in $walletName. Consider diversification.", InsightType.NEUTRAL))
                }
            }
        }

        return insights.take(5)
    }

    private suspend fun calculateAwarenessScore(
        incomes: List<Income>,
        expenses: List<Expense>,
        goals: List<SavingsGoal>,
        baseCurrency: String
    ): AwarenessScoreUi {
        var score = 0
        val last7Days = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val logsLast7Days = (incomes.count { it.date > last7Days } + expenses.count { it.date > last7Days && it.transactionType == TransactionType.REGULAR }).coerceAtMost(10)
        score += logsLast7Days * 4 

        val currentMonth = currentMonthMillis()
        val mIncome = incomes.filter { isSameMonth(it.date, currentMonth) }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        val mRegularExpense = expenses.filter { isSameMonth(it.date, currentMonth) && it.transactionType == TransactionType.REGULAR }.sumOf { exchangeRateRepository.convert(it.amount, it.currency, baseCurrency) }
        
        val sRate = if (mIncome > 0) (mIncome - mRegularExpense) / mIncome else 0.0
        score += (sRate * 40).roundToInt().coerceIn(0, 40) 

        val anyActiveGoal = goals.any { it.status == GoalStatus.ACTIVE }
        if (anyActiveGoal) score += 20

        val finalScore = score.coerceIn(0, 100)
        val label = when {
            finalScore > 80 -> "Exceptional"
            finalScore > 60 -> "Strong"
            finalScore > 40 -> "Developing"
            else -> "Needs Focus"
        }

        return AwarenessScoreUi(
            score = finalScore,
            label = label,
            message = "Based on your logging consistency and savings rate.",
            trend = ScoreTrend.STABLE
        )
    }

    private suspend fun calculateGoalAnalytics(
        goals: List<SavingsGoal>,
        wallets: List<Wallet>,
        baseCurrency: String
    ): List<GoalAnalyticUi> {
        val walletMap = wallets.associateBy { it.id }
        return goals.filter { it.status == GoalStatus.ACTIVE }.map { goal ->
            val wallet = walletMap[goal.walletId]
            val currentAmount = wallet?.currentBalance ?: 0.0
            val remaining = (goal.targetAmount - currentAmount).coerceAtLeast(0.0)
            
            val monthsRemaining = ChronoUnit.MONTHS.between(ZonedDateTime.now(), Instant.ofEpochMilli(goal.targetDate).atZone(ZoneId.systemDefault())).coerceAtLeast(1)
            val velocity = remaining / monthsRemaining

            GoalAnalyticUi(
                goalTitle = goal.title,
                velocity = velocity,
                projectedCompletionDate = formatDate(goal.targetDate), // Placeholder
                progress = if (goal.targetAmount > 0) (currentAmount / goal.targetAmount).toFloat() else 0f
            )
        }
    }

    private fun currentMonthMillis(): Long {
        return ZonedDateTime.now().toInstant().toEpochMilli()
    }

    private fun formatDate(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM yyyy"))
    }

    private fun firstError(vararg resources: Resource<*>): String? {
        return resources.filterIsInstance<Resource.Error>().firstOrNull()?.message
    }
}
