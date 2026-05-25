package com.example.projectpbd.presentation.recurring

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectpbd.core.util.CurrencyFormatter
import com.example.projectpbd.core.util.RecurrenceCalculator
import com.example.projectpbd.domain.model.Expense
import com.example.projectpbd.domain.model.Income
import com.example.projectpbd.presentation.components.MomentumCard
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.history.components.HistoryEmptyState
import com.example.projectpbd.presentation.recurring.state.RecurringEvent
import com.example.projectpbd.presentation.recurring.state.RecurringUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionsScreen(
    state: RecurringUiState,
    onEvent: (RecurringEvent) -> Unit,
    onBack: () -> Unit,
    onEditExpense: (String) -> Unit,
    onEditIncome: (String) -> Unit,
    isEmbedded: Boolean = false
) {
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (!isEmbedded && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val content = @Composable { padding: PaddingValues ->
        if (state.expenses.isEmpty() && state.incomes.isEmpty() && !state.isLoading) {
            HistoryEmptyState(
                message = "No recurring schedules configured",
                icon = Icons.Outlined.Repeat
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dim.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
            ) {
                if (state.isLoading) {
                    item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                }

                if (state.incomes.isNotEmpty()) {
                    item { SectionHeader("Recurring Income") }
                    items(state.incomes) { income ->
                        RecurringIncomeCard(
                            income = income,
                            onToggle = { onEvent(RecurringEvent.ToggleIncomeActive(income.id, it)) },
                            onDelete = { onEvent(RecurringEvent.DeleteIncomeSchedule(income.id)) },
                            onEdit = { onEditIncome(income.id) }
                        )
                    }
                }

                if (state.expenses.isNotEmpty()) {
                    item { SectionHeader("Recurring Expenses") }
                    items(state.expenses) { expense ->
                        RecurringExpenseCard(
                            expense = expense,
                            onToggle = { onEvent(RecurringEvent.ToggleExpenseActive(expense.id, it)) },
                            onDelete = { onEvent(RecurringEvent.DeleteExpenseSchedule(expense.id)) },
                            onEdit = { onEditExpense(expense.id) }
                        )
                    }
                }
            }
        }
    }

    if (isEmbedded) {
        content(PaddingValues(0.dp))
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Recurring Schedules", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { padding ->
            content(padding)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = Dim.SpacingSmall)
    )
}

@Composable
private fun RecurringExpenseCard(
    expense: Expense,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    RecurringItemCard(
        title = expense.notes.ifBlank { "Expense" },
        amount = CurrencyFormatter.format(expense.amount, expense.currency),
        isIncome = false,
        repeatSummary = RecurrenceCalculator.getRepeatSummary(expense.repeatConfig),
        nextDate = expense.repeatConfig.nextExecutionDate,
        isActive = expense.repeatConfig.isActive,
        onToggle = onToggle,
        onDelete = onDelete,
        onEdit = onEdit
    )
}

@Composable
private fun RecurringIncomeCard(
    income: Income,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    RecurringItemCard(
        title = income.notes.ifBlank { "Income" },
        amount = CurrencyFormatter.format(income.amount, income.currency),
        isIncome = true,
        repeatSummary = RecurrenceCalculator.getRepeatSummary(income.repeatConfig),
        nextDate = income.repeatConfig.nextExecutionDate,
        isActive = income.repeatConfig.isActive,
        onToggle = onToggle,
        onDelete = onDelete,
        onEdit = onEdit
    )
}

@Composable
private fun RecurringItemCard(
    title: String,
    amount: String,
    isIncome: Boolean,
    repeatSummary: String,
    nextDate: Long,
    isActive: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }
    val nextDateLabel = if (nextDate > 0) {
        Instant.ofEpochMilli(nextDate)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    } else "Not scheduled"

    MomentumCard(onClick = onEdit) {
        Column(modifier = Modifier.padding(Dim.SpacingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = (if (isIncome) "+" else "-") + " " + amount,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isActive,
                        onCheckedChange = onToggle,
                        modifier = Modifier.scale(0.8f)
                    )
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "More")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Edit Original") },
                                onClick = { showMenu = false; onEdit() }
                            )
                            DropdownMenuItem(
                                text = { Text("Remove Schedule", color = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onDelete() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dim.SpacingMedium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Schedule", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(repeatSummary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Next Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(nextDateLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            
            if (!isActive) {
                Spacer(modifier = Modifier.height(Dim.SpacingSmall))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "PAUSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
