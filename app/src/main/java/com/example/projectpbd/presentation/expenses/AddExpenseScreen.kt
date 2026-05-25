package com.example.projectpbd.presentation.expenses

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
import com.example.projectpbd.presentation.expenses.state.ExpenseEvent
import com.example.projectpbd.presentation.expenses.state.ExpenseUiState
import com.example.projectpbd.presentation.transactions.components.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    state: ExpenseUiState,
    onEvent: (ExpenseEvent) -> Unit,
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
        categoryViewModel.observeCategories(CategoryType.EXPENSE)
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
            onEvent(ExpenseEvent.MessageShown)
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
                        "Add a new category for your expenses.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        placeholder = { Text("e.g. Gaming, Coffee") },
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
                            categoryViewModel.addCategory(newCategoryName, CategoryType.EXPENSE)
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
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to delete this expense? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(ExpenseEvent.DeleteClicked)
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
                onEvent(ExpenseEvent.RepeatConfigChanged(it))
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
                onEvent(ExpenseEvent.DateChanged(updated))
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
                isIncome = false,
                isEditing = state.isEditing,
                onBack = onBack,
                onDelete = if (state.isEditing) { { showDeleteConfirm = true } } else null
            )
        },
        bottomBar = {
            SaveTransactionBar(
                text = if (state.isEditing) "Update Expense" else "Save Expense",
                onClick = { onEvent(ExpenseEvent.SaveClicked) },
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
                    onAmountChange = { onEvent(ExpenseEvent.AmountChanged(it)) },
                    isIncome = false,
                    currencyCode = state.form.currency
                )
            }

            item {
                TransactionSectionCard(title = "Payment Wallet") {
                    WalletCarousel(
                        wallets = state.wallets,
                        selectedId = state.form.walletId,
                        onSelect = { onEvent(ExpenseEvent.WalletSelected(it)) }
                    )
                }
            }

            item {
                TransactionSectionCard(title = "Category") {
                    CategorySelectorGrid(
                        categories = state.categories,
                        selectedId = state.form.categoryId,
                        onSelect = { onEvent(ExpenseEvent.CategorySelected(it)) },
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
                        helperText = if (isFuture) "This scheduled expense will begin on ${dateLabel.split(",")[1]}" else null
                    )
                }
            }

            item {
                TransactionSectionCard(title = "Notes") {
                    TransactionNotesField(
                        notes = state.form.notes,
                        onNotesChange = { onEvent(ExpenseEvent.NotesChanged(it)) }
                    )
                }
            }
        }
    }
}
