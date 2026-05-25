package com.example.projectpbd.presentation.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.core.util.RecurrenceCalculator
import com.example.projectpbd.domain.model.CategoryType
import com.example.projectpbd.domain.model.Income
import com.example.projectpbd.domain.repository.CategoryRepository
import com.example.projectpbd.domain.repository.ExchangeRateRepository
import com.example.projectpbd.domain.repository.IncomeRepository
import com.example.projectpbd.domain.repository.WalletRepository
import com.example.projectpbd.domain.repository.SettingsRepository
import com.example.projectpbd.presentation.income.state.IncomeEvent
import com.example.projectpbd.presentation.income.state.IncomeFormState
import com.example.projectpbd.presentation.income.state.IncomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val repository: IncomeRepository,
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository,
    private val settingsRepository: SettingsRepository,
    private val exchangeRateRepository: ExchangeRateRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(IncomeUiState())
    val uiState: StateFlow<IncomeUiState> = _uiState.asStateFlow()

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
                        // Only set default wallet if it hasn't been set yet and we're not editing
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
            categoryRepository.observeCategories(CategoryType.INCOME).collect { resource ->
                if (resource is Resource.Success) {
                    _uiState.update { it.copy(categories = resource.data) }
                }
            }
        }
    }

    fun onEvent(event: IncomeEvent) {
        when (event) {
            is IncomeEvent.LoadIncome -> loadIncome(event.id)
            is IncomeEvent.AmountChanged -> updateForm { copy(amount = event.value) }
            is IncomeEvent.CurrencySelected -> updateForm { copy(currency = event.currency) }
            is IncomeEvent.CategorySelected -> updateForm { copy(categoryId = event.categoryId) }
            is IncomeEvent.WalletSelected -> {
                val wallet = _uiState.value.wallets.find { it.id == event.walletId }
                updateForm { 
                    copy(
                        walletId = event.walletId,
                        currency = wallet?.currency ?: "LKR"
                    ) 
                }
            }
            is IncomeEvent.DateChanged -> updateForm { copy(dateMillis = event.dateMillis) }
            is IncomeEvent.NotesChanged -> updateForm { copy(notes = event.value) }
            is IncomeEvent.RepeatConfigChanged -> updateForm { copy(repeatConfig = event.config) }
            is IncomeEvent.ExchangeRateChanged -> updateForm { copy(exchangeRate = event.value) }
            is IncomeEvent.ConvertedAmountChanged -> updateForm { copy(convertedAmountLkr = event.value) }
            IncomeEvent.SaveClicked -> saveIncome()
            IncomeEvent.DeleteClicked -> deleteIncome()
            IncomeEvent.MessageShown -> clearMessages()
        }
    }

    private fun loadIncome(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, incomeId = id) }
            when (val result = repository.getIncome(id)) {
                is Resource.Success -> {
                    val income = result.data
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            form = IncomeFormState(
                                amount = income.amount.toString(),
                                currency = income.currency,
                                categoryId = income.categoryId,
                                walletId = income.walletId,
                                dateMillis = income.date,
                                notes = income.notes,
                                repeatConfig = income.repeatConfig,
                                exchangeRate = income.exchangeRate.toString(),
                                convertedAmountLkr = income.convertedAmountLkr.toString()
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

    private fun updateForm(reducer: IncomeFormState.() -> IncomeFormState) {
        _uiState.update { state ->
            state.copy(
                form = state.form.reducer(),
                amountError = null,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun saveIncome() {
        if (_uiState.value.isLoading) return
        val currentState = _uiState.value
        val form = currentState.form
        val amount = form.amount.toDoubleOrNull()
        val walletId = form.walletId

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(amountError = "Enter a valid amount.") }
            return
        }

        if (walletId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Pick a wallet.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val exchangeRate = if (form.currency == "LKR") 1.0 
                             else form.exchangeRate.toDoubleOrNull() ?: exchangeRateRepository.getRate(form.currency, "LKR")
            
            val convertedLkr = resolveConvertedAmount(
                amount = amount,
                currency = form.currency,
                exchangeRate = exchangeRate,
                convertedInput = form.convertedAmountLkr
            )

            val currentTime = System.currentTimeMillis()
            val isFuture = form.dateMillis > currentTime
            
            var repeatConfig = form.repeatConfig.copy(
                startDate = form.dateMillis,
                nextExecutionDate = form.dateMillis
            )

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

            val income = Income(
                id = currentState.incomeId ?: "",
                amount = amount,
                currency = form.currency,
                categoryId = form.categoryId,
                walletId = walletId,
                date = form.dateMillis,
                notes = form.notes.trim(),
                exchangeRate = exchangeRate,
                convertedAmountLkr = convertedLkr,
                repeatConfig = repeatConfig,
                createdAt = System.currentTimeMillis()
            )

            val result = if (currentState.isEditing) {
                repository.updateIncome(income)
            } else {
                val addRes = repository.addIncome(income)
                if (addRes is Resource.Success && !isFuture) {
                    walletRepository.updateBalance(walletId, amount, true)
                }
                addRes
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            successMessage = if (state.isEditing) "Income updated." else "Income saved.",
                            form = if (state.isEditing) state.form else state.form.copy(
                                amount = "",
                                notes = "",
                                exchangeRate = "",
                                convertedAmountLkr = "",
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

    private fun deleteIncome() {
        val id = _uiState.value.incomeId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.deleteIncome(id)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, successMessage = "Income deleted.") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun resolveConvertedAmount(
        amount: Double,
        currency: String,
        exchangeRate: Double,
        convertedInput: String
    ): Double {
        if (currency == "LKR") return amount
        val converted = convertedInput.toDoubleOrNull()
        return converted ?: (amount * exchangeRate)
    }

    private fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
