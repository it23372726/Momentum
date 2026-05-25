package com.example.projectpbd.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.CategoryType
import com.example.projectpbd.domain.model.Expense
import com.example.projectpbd.domain.model.PaymentMethod
import com.example.projectpbd.domain.repository.CategoryRepository
import com.example.projectpbd.domain.repository.ExchangeRateRepository
import com.example.projectpbd.domain.repository.ExpenseRepository
import com.example.projectpbd.domain.repository.WalletRepository
import com.example.projectpbd.domain.repository.SettingsRepository
import com.example.projectpbd.presentation.expenses.state.ExpenseEvent
import com.example.projectpbd.presentation.expenses.state.ExpenseFormState
import com.example.projectpbd.presentation.expenses.state.ExpenseUiState
import com.example.projectpbd.core.util.RecurrenceCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val settingsRepository: SettingsRepository,
    private val exchangeRateRepository: ExchangeRateRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        observeCategories()
        observeWalletsAndSettings()
    }

    private fun observeWalletsAndSettings() {
        viewModelScope.launch {
            combine(
                walletRepository.observeWallets(),
                settingsRepository.getSettings()
            ) { walletRes, settings ->
                if (walletRes is Resource.Success) {
                    val wallets = walletRes.data
                    _uiState.update { state ->
                        val currentWalletId = state.form.walletId
                        val newWalletId = if (currentWalletId.isBlank() && !state.isEditing) {
                            settings.defaultWalletId ?: ""
                        } else {
                            currentWalletId
                        }
                        
                        val selectedWallet = wallets.find { it.id == newWalletId }
                        
                        state.copy(
                            wallets = wallets,
                            form = state.form.copy(
                                walletId = newWalletId,
                                currency = selectedWallet?.currency ?: state.form.currency
                            )
                        )
                    }
                }
            }.collect()
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            categoryRepository.observeCategories(CategoryType.EXPENSE).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(categories = resource.data) }
                }
            }
        }
    }

    fun onEvent(event: ExpenseEvent) {
        when (event) {
            is ExpenseEvent.LoadExpense -> loadExpense(event.id)
            is ExpenseEvent.AmountChanged -> updateForm { copy(amount = event.value) }
            is ExpenseEvent.CategorySelected -> updateForm { copy(categoryId = event.categoryId) }
            is ExpenseEvent.WalletSelected -> {
                val wallet = _uiState.value.wallets.find { it.id == event.walletId }
                updateForm { 
                    copy(
                        walletId = event.walletId,
                        currency = wallet?.currency ?: "LKR"
                    ) 
                }
            }
            is ExpenseEvent.PaymentSelected -> updateForm { copy(paymentMethod = event.method) }
            is ExpenseEvent.NotesChanged -> updateForm { copy(notes = event.value) }
            is ExpenseEvent.DateChanged -> updateForm { copy(dateMillis = event.dateMillis) }
            is ExpenseEvent.RepeatConfigChanged -> updateForm { copy(repeatConfig = event.config) }
            is ExpenseEvent.DiscretionaryToggled -> updateForm { copy(isDiscretionary = event.enabled) }
            ExpenseEvent.SaveClicked -> saveExpense()
            ExpenseEvent.DeleteClicked -> deleteExpense()
            ExpenseEvent.MessageShown -> clearMessages()
        }
    }

    private fun loadExpense(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, expenseId = id) }
            when (val result = repository.getExpense(id)) {
                is Resource.Success -> {
                    val expense = result.data
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            form = ExpenseFormState(
                                amount = expense.amount.toString(),
                                currency = expense.currency,
                                categoryId = expense.categoryId,
                                paymentMethod = expense.paymentMethod,
                                walletId = expense.walletId,
                                notes = expense.notes,
                                dateMillis = expense.date,
                                repeatConfig = expense.repeatConfig,
                                isDiscretionary = expense.isDiscretionary
                            )
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun updateForm(reducer: ExpenseFormState.() -> ExpenseFormState) {
        _uiState.update { state ->
            state.copy(
                form = state.form.reducer(),
                amountError = null,
                categoryError = null,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun saveExpense() {
        if (_uiState.value.isLoading) return
        val currentState = _uiState.value
        val form = currentState.form
        val amount = form.amount.toDoubleOrNull()
        val categoryId = form.categoryId
        val walletId = form.walletId

        var isValid = true
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(amountError = "Enter a valid amount.") }
            isValid = false
        }
        if (categoryId.isBlank()) {
            _uiState.update { it.copy(categoryError = "Pick a category.") }
            isValid = false
        }
        if (walletId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Pick a wallet.") }
            isValid = false
        }
        if (!isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val exchangeRate = if (form.currency == "LKR") 1.0 else exchangeRateRepository.getRate(form.currency, "LKR")
            val convertedAmountLkr = (amount ?: 0.0) * exchangeRate
            
            val currentTime = System.currentTimeMillis()
            val isFuture = form.dateMillis > currentTime
            
            var repeatConfig = form.repeatConfig.copy(
                startDate = form.dateMillis,
                nextExecutionDate = form.dateMillis
            )

            // If it's recurring and date is NOT in future, execute initial occurrence immediately
            val initialExecuteNeeded = !isFuture && repeatConfig.isEnabled

            if (initialExecuteNeeded) {
                repeatConfig = repeatConfig.copy(
                    lastExecutionDate = form.dateMillis,
                    nextExecutionDate = RecurrenceCalculator.calculateNextOccurrence(
                        repeatConfig,
                        form.dateMillis
                    ),
                    isInitialOccurrenceExecuted = true
                )
            }

            val expense = Expense(
                id = currentState.expenseId ?: "",
                amount = amount ?: 0.0,
                currency = form.currency,
                categoryId = categoryId,
                paymentMethod = form.paymentMethod,
                walletId = walletId,
                date = form.dateMillis,
                notes = form.notes.trim(),
                exchangeRate = exchangeRate,
                convertedAmountLkr = convertedAmountLkr,
                repeatConfig = repeatConfig,
                isDiscretionary = form.isDiscretionary,
                createdAt = System.currentTimeMillis()
            )

            val result = if (currentState.isEditing) {
                repository.updateExpense(expense)
            } else {
                val addRes = repository.addExpense(expense)
                // Only update balance immediately if NOT in future
                if (addRes is Resource.Success && !isFuture) {
                    walletRepository.updateBalance(walletId, amount ?: 0.0, false)
                }
                addRes
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = if (state.isEditing) "Expense updated." else "Expense saved.",
                            form = if (state.isEditing) state.form else state.form.copy(
                                amount = "",
                                notes = "",
                                dateMillis = System.currentTimeMillis()
                            )
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun deleteExpense() {
        val id = _uiState.value.expenseId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.deleteExpense(id)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Expense deleted.") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
