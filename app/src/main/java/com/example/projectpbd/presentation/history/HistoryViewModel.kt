package com.example.projectpbd.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.*
import com.example.projectpbd.domain.repository.*
import com.example.projectpbd.presentation.dashboard.state.ActivityItemUi
import com.example.projectpbd.presentation.history.state.HistoryEvent
import com.example.projectpbd.presentation.history.state.HistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val goalRepository: GoalRepository,
    private val settingsRepository: SettingsRepository,
    private val exchangeRateRepository: ExchangeRateRepository
) : ViewModel() {
    private val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeHistory()
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            HistoryEvent.Refresh -> observeHistory()
            is HistoryEvent.WalletFilterChanged -> _uiState.update { it.copy(selectedWallet = event.wallet) }
            is HistoryEvent.CurrencyFilterChanged -> _uiState.update { it.copy(selectedCurrency = event.currency) }
            is HistoryEvent.SearchQueryChanged -> _uiState.update { it.copy(searchQuery = event.query) }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            combine(
                incomeRepository.observeIncomes(),
                expenseRepository.observeExpenses(),
                categoryRepository.observeCategories(),
                walletRepository.observeWallets(),
                walletRepository.observeTransfers(),
                goalRepository.observeGoals(),
                settingsRepository.getSettings(),
                _uiState.map { it.selectedWallet }.distinctUntilChanged(),
                _uiState.map { it.selectedCurrency }.distinctUntilChanged(),
                _uiState.map { it.searchQuery }.distinctUntilChanged()
            ) { args ->
                val incomesRes = args[0] as Resource<List<Income>>
                val expensesRes = args[1] as Resource<List<Expense>>
                val categoriesRes = args[2] as Resource<List<Category>>
                val walletsRes = args[3] as Resource<List<Wallet>>
                val transfersRes = args[4] as Resource<List<Transfer>>
                val goalsRes = args[5] as Resource<List<SavingsGoal>>
                val settings = args[6] as AppSettings
                val selectedWallet = args[7] as? String
                val selectedCurrency = args[8] as? String
                val searchQuery = args[9] as String

                val isLoading = listOf(incomesRes, expensesRes, categoriesRes, walletsRes, transfersRes, goalsRes).any { it is Resource.Loading }
                val error = (incomesRes as? Resource.Error)?.message 
                    ?: (expensesRes as? Resource.Error)?.message
                    ?: (transfersRes as? Resource.Error)?.message

                val incomeItems = (incomesRes as? Resource.Success)?.data.orEmpty()
                val expenseItems = (expensesRes as? Resource.Success)?.data.orEmpty()
                val categoryItems = (categoriesRes as? Resource.Success)?.data.orEmpty()
                val walletItems = (walletsRes as? Resource.Success)?.data.orEmpty()
                val goalItems = (goalsRes as? Resource.Success)?.data.orEmpty()
                val baseCurrency = settings.baseCurrency

                val fullList = buildActivityList(incomeItems, expenseItems, categoryItems, walletItems, goalItems, baseCurrency)
                
                val filtered = fullList.filter { 
                    (selectedWallet == null || it.walletName == selectedWallet) &&
                    (selectedCurrency == null || it.currencyCode == selectedCurrency) &&
                    (searchQuery.isBlank() || 
                        it.label.contains(searchQuery, ignoreCase = true) || 
                        it.notes.contains(searchQuery, ignoreCase = true) ||
                        it.categoryName?.contains(searchQuery, ignoreCase = true) == true ||
                        it.walletName?.contains(searchQuery, ignoreCase = true) == true)
                }

                val wallets = fullList.mapNotNull { it.walletName }.distinct().sorted()
                val currencies = fullList.map { it.currencyCode }.distinct().sorted()

                HistoryUiState(
                    isLoading = isLoading,
                    activities = filtered,
                    baseCurrency = baseCurrency,
                    wallets = wallets,
                    currencies = currencies,
                    selectedWallet = selectedWallet,
                    selectedCurrency = selectedCurrency,
                    searchQuery = searchQuery,
                    errorMessage = error
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    private suspend fun buildActivityList(
        incomes: List<Income>,
        expenses: List<Expense>,
        categories: List<Category>,
        wallets: List<Wallet>,
        goals: List<SavingsGoal>,
        baseCurrency: String
    ): List<ActivityItemUi> {
        val categoryMap = categories.associateBy { it.id }
        val walletMap = wallets.associateBy { it.id }
        val currentTime = System.currentTimeMillis()

        val incomeItems = incomes
            .filter { it.date <= currentTime }
            .map {
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
                    timestamp = it.date,
                    walletName = walletMap[it.walletId]?.name,
                    notes = it.notes
                )
            }
            val expenseItems = expenses
            .filter { it.date <= currentTime }
            .map {
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
                    timestamp = it.date,
                    walletName = walletMap[it.walletId]?.name,
                    notes = it.notes
                )
            }

        return (incomeItems + expenseItems).sortedByDescending { it.timestamp }
    }

    private fun formatDate(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }
}
