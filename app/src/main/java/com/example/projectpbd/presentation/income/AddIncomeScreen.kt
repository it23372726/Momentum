package com.example.projectpbd.presentation.income

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projectpbd.domain.model.CategoryType
import com.example.projectpbd.presentation.analytics.CategoryViewModel
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.components.RepeatConfigurationDialog
import com.example.projectpbd.presentation.income.state.IncomeEvent
import com.example.projectpbd.presentation.income.state.IncomeUiState
import com.example.projectpbd.presentation.transactions.components.*
import com.example.projectpbd.presentation.income.components.ExchangeRateField
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(
    state: IncomeUiState,
    onEvent: (IncomeEvent) -> Unit,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var showRepeatDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val categoryState by categoryViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        categoryViewModel.observeCategories(CategoryType.INCOME)
    }

    LaunchedEffect(categoryState.success) {
        if (categoryState.success) {
            showAddCategory = false
            newCategoryName = ""
            categoryViewModel.clearMessages()
        }
    }

    LaunchedEffect(state.errorMessage, state.successMessage) {
        val message = state.errorMessage ?: state.successMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            onEvent(IncomeEvent.MessageShown)
            if (state.successMessage != null) {
                onSaved()
            }
        }
    }

    if (showAddCategory) {
        AlertDialog(
            onDismissRequest = { showAddCategory = false },
            title = { 
                Text(
                    "Create New Category", 
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
                    Text(
                        "Add a new category for your income.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        placeholder = { Text("e.g. Salary, Gift") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(Dim.RadiusMedium),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (newCategoryName.isNotBlank()) {
                            categoryViewModel.addCategory(newCategoryName, CategoryType.INCOME)
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(Dim.RadiusSmall)
                ) {
                    Text("Add Category")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategory = false }) {
                    Text("Cancel")
                }
            },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(Dim.RadiusLarge)
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Income?") },
            text = { Text("Are you sure you want to delete this income entry?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(IncomeEvent.DeleteClicked)
                        showDeleteConfirm = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRepeatDialog) {
        RepeatConfigurationDialog(
            initialConfig = state.form.repeatConfig,
            onDismiss = { showRepeatDialog = false },
            onConfirm = {
                onEvent(IncomeEvent.RepeatConfigChanged(it))
                showRepeatDialog = false
            }
        )
    }

    val calendar = Calendar.getInstance().apply { timeInMillis = state.form.dateMillis }
    val datePickerDialog = remember(state.form.dateMillis) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val updated = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                }.timeInMillis
                onEvent(IncomeEvent.DateChanged(updated))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TransactionTypeHeader(
                isIncome = true,
                isEditing = state.isEditing,
                onBack = onBack,
                onDelete = if (state.isEditing) { { showDeleteConfirm = true } } else null
            )
        },
        bottomBar = {
            SaveTransactionBar(
                text = if (state.isEditing) "Update Income" else "Save Income",
                onClick = { onEvent(IncomeEvent.SaveClicked) },
                isLoading = state.isLoading,
                enabled = state.form.amount.isNotEmpty() && state.form.walletId.isNotEmpty() && state.form.categoryId.isNotEmpty()
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = Dim.SpacingXXXLarge),
            verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            item {
                TransactionAmountField(
                    amount = state.form.amount,
                    onAmountChange = { onEvent(IncomeEvent.AmountChanged(it)) },
                    isIncome = true,
                    currencyCode = state.form.currency
                )
            }

            item {
                TransactionSectionCard(title = "Deposit Wallet") {
                    WalletCarousel(
                        wallets = state.wallets,
                        selectedId = state.form.walletId,
                        onSelect = { onEvent(IncomeEvent.WalletSelected(it)) }
                    )
                }
            }

            item {
                TransactionSectionCard(title = "Category") {
                    CategorySelectorGrid(
                        categories = state.categories,
                        selectedId = state.form.categoryId,
                        onSelect = { onEvent(IncomeEvent.CategorySelected(it)) },
                        onAddCategory = { showAddCategory = true }
                    )
                }
            }

            item {
                TransactionSectionCard(title = "Schedule & Date") {
                    RepeatSummaryCard(
                        config = state.form.repeatConfig,
                        onClick = { showRepeatDialog = true }
                    )
                    
                    Spacer(modifier = Modifier.height(Dim.SpacingMedium))
                    
                    val dateLabel = Instant.ofEpochMilli(state.form.dateMillis)
                        .atZone(ZoneId.systemDefault())
                        .format(formatter)
                    
                    val isFuture = state.form.dateMillis > System.currentTimeMillis()
                    
                    ModernDateSelector(
                        dateLabel = dateLabel,
                        onClick = { datePickerDialog.show() },
                        helperText = if (isFuture) "This scheduled income will begin on ${dateLabel.split(",")[1]}" else null
                    )
                }
            }

            item {
                TransactionSectionCard(title = "Exchange Rate & Notes") {
                    if (state.form.currency != "LKR") {
                        ExchangeRateField(
                            currency = state.form.currency,
                            exchangeRate = state.form.exchangeRate,
                            convertedAmount = state.form.convertedAmountLkr,
                            onRateChange = { onEvent(IncomeEvent.ExchangeRateChanged(it)) },
                            onConvertedChange = { onEvent(IncomeEvent.ConvertedAmountChanged(it)) }
                        )
                        Spacer(modifier = Modifier.height(Dim.SpacingMedium))
                    }

                    TransactionNotesField(
                        notes = state.form.notes,
                        onNotesChange = { onEvent(IncomeEvent.NotesChanged(it)) }
                    )
                }
            }
        }
    }
}
