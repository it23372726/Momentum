package com.example.projectpbd.presentation.history

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectpbd.core.util.CurrencyFormatter
import com.example.projectpbd.presentation.components.MomentumCard
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.history.components.HistoryEmptyState
import com.example.projectpbd.presentation.history.components.HistoryTabBar
import com.example.projectpbd.presentation.history.state.HistoryEvent
import com.example.projectpbd.presentation.history.state.HistoryUiState
import com.example.projectpbd.presentation.recurring.RecurringTransactionsScreen
import com.example.projectpbd.presentation.recurring.state.RecurringEvent
import com.example.projectpbd.presentation.recurring.state.RecurringUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryHubScreen(
    historyState: HistoryUiState,
    recurringState: RecurringUiState,
    onHistoryEvent: (HistoryEvent) -> Unit,
    onRecurringEvent: (RecurringEvent) -> Unit,
    onBack: () -> Unit,
    onEditTransaction: (String, Boolean) -> Unit,
    onEditRecurringExpense: (String) -> Unit,
    onEditRecurringIncome: (String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Activity", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (selectedTabIndex == 0) {
                OutlinedTextField(
                    value = historyState.searchQuery,
                    onValueChange = { onHistoryEvent(HistoryEvent.SearchQueryChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dim.SpacingLarge, vertical = Dim.SpacingSmall),
                    placeholder = { Text("Search transactions...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (historyState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onHistoryEvent(HistoryEvent.SearchQueryChanged("")) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(Dim.RadiusMedium),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            HistoryTabBar(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            Crossfade(
                targetState = selectedTabIndex,
                label = "HistoryTabTransition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> TransactionsTabContent(
                        state = historyState,
                        onEvent = onHistoryEvent,
                        onItemClick = onEditTransaction
                    )
                    1 -> RecurringTransactionsScreen(
                        state = recurringState,
                        onEvent = onRecurringEvent,
                        onBack = {}, // Not used in hub
                        onEditExpense = onEditRecurringExpense,
                        onEditIncome = onEditRecurringIncome,
                        isEmbedded = true
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionsTabContent(
    state: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    onItemClick: (String, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips
        if (state.wallets.isNotEmpty() || state.currencies.isNotEmpty()) {
            androidx.compose.foundation.lazy.LazyRow(
                contentPadding = PaddingValues(horizontal = Dim.SpacingLarge),
                horizontalArrangement = Arrangement.spacedBy(Dim.SpacingSmall),
                modifier = Modifier.padding(vertical = Dim.SpacingSmall)
            ) {
                // Wallet Filters
                items(state.wallets) { wallet ->
                    FilterChip(
                        selected = state.selectedWallet == wallet,
                        onClick = { onEvent(HistoryEvent.WalletFilterChanged(if (state.selectedWallet == wallet) null else wallet)) },
                        label = { Text(wallet) }
                    )
                }
                
                // Currency Filters
                items(state.currencies) { currency ->
                    FilterChip(
                        selected = state.selectedCurrency == currency,
                        onClick = { onEvent(HistoryEvent.CurrencyFilterChanged(if (state.selectedCurrency == currency) null else currency)) },
                        label = { Text(currency) }
                    )
                }
            }
        }

        if (state.activities.isEmpty() && !state.isLoading) {
            HistoryEmptyState(
                message = if (state.selectedWallet != null || state.selectedCurrency != null) "No matches for filters" else "No transactions found",
                icon = Icons.Outlined.History
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(Dim.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
            ) {
                if (state.isLoading) {
                    item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                }
                items(state.activities) { item ->
                    TransactionItem(
                        item = item, 
                        baseCurrency = state.baseCurrency,
                        onClick = { onItemClick(item.id, item.isIncome) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    item: com.example.projectpbd.presentation.dashboard.state.ActivityItemUi,
    baseCurrency: String,
    onClick: () -> Unit
) {
    MomentumCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .padding(Dim.SpacingLarge)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatWithSign(item.amount, item.currencyCode, item.isIncome),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                if (item.currencyCode != baseCurrency) {
                    Text(
                        text = "≈ ${CurrencyFormatter.format(item.convertedAmountLkr, baseCurrency)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
