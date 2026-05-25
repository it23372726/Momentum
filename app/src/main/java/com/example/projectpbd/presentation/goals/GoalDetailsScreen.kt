package com.example.projectpbd.presentation.goals

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpbd.core.util.CurrencyFormatter
import com.example.projectpbd.domain.model.GoalStatus
import com.example.projectpbd.domain.model.Transfer
import com.example.projectpbd.presentation.components.MomentumCard
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.components.SectionHeader
import com.example.projectpbd.presentation.goals.components.*
import com.example.projectpbd.presentation.goals.state.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailsScreen(
    goalId: String,
    state: GoalUiState,
    onBack: () -> Unit,
    onEvent: (GoalEvent) -> Unit,
    onEdit: () -> Unit
) {
    val goalItem = remember(state.goals, goalId) { state.goals.find { it.goal.id == goalId } }
    var showContributeSheet by remember { mutableStateOf(false) }
    var showWithdrawSheet by remember { mutableStateOf(false) }
    var showBuyNowSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }

    if (goalItem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                // If goal is not found and we're not loading, it might have been deleted.
                // Navigation is handled in the Route, but we show a placeholder here.
                Text("Goal not found")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(goalItem.goal.title, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 8.dp) {
                Column(modifier = Modifier.padding(Dim.SpacingLarge).navigationBarsPadding()) {
                    if (goalItem.isReadyToBuy) {
                        Button(
                            onClick = { showBuyNowSheet = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.ShoppingCart, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BUY NOW", fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(Dim.SpacingMedium))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
                    ) {
                        OutlinedButton(
                            onClick = { showWithdrawSheet = true },
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Icon(Icons.Default.Remove, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Withdraw")
                        }
                        Button(
                            onClick = { showContributeSheet = true },
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Contribute")
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dim.SpacingLarge),
            verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            item {
                GoalVaultCard(item = goalItem, baseCurrency = state.baseCurrency, onClick = {})
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
                    AnalyticsMiniCard(
                        label = "Daily Goal",
                        value = CurrencyFormatter.format(goalItem.dailyRequired, goalItem.wallet.currency),
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsMiniCard(
                        label = "Weekly Goal",
                        value = CurrencyFormatter.format(goalItem.weeklyRequired, goalItem.wallet.currency),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (goalItem.insights.isNotEmpty()) {
                item {
                    SectionHeader(title = "Vault Intelligence")
                    Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)) {
                        goalItem.insights.forEach { insight ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Row(modifier = Modifier.padding(Dim.SpacingMedium), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(insight, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Vault Activity")
            }

            items(goalItem.recentTransfers) { transfer ->
                val isContribution = transfer.destinationWalletId == goalItem.goal.walletId
                val displayAmount = if (isContribution) transfer.targetAmount else transfer.amount
                val displayCurrency = if (isContribution) transfer.targetCurrency else transfer.sourceCurrency
                
                ListItem(
                    headlineContent = { Text(if (isContribution) "Vault Contribution" else "Vault Withdrawal") },
                    supportingContent = { 
                        Column {
                            Text(Instant.ofEpochMilli(transfer.date).atZone(ZoneId.systemDefault()).format(formatter))
                            if (transfer.sourceCurrency != transfer.targetCurrency) {
                                Text(
                                    text = "Converted from ${CurrencyFormatter.format(if(isContribution) transfer.amount else transfer.targetAmount, if(isContribution) transfer.sourceCurrency else transfer.targetCurrency)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    },
                    trailingContent = {
                        Text(
                            text = (if (isContribution) "+" else "-") + CurrencyFormatter.format(displayAmount, displayCurrency.ifBlank { goalItem.wallet.currency }),
                            fontWeight = FontWeight.Bold,
                            color = if (isContribution) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }

    if (showContributeSheet) {
        ContributionSheet(
            goalItem = goalItem,
            wallets = state.wallets,
            state = state,
            onDismiss = { 
                showContributeSheet = false
                onEvent(GoalEvent.ClearTransferState)
            },
            onEvent = { event ->
                onEvent(event)
                if (event is GoalEvent.ContributeClicked) {
                    showContributeSheet = false
                }
            }
        )
    }

    if (showWithdrawSheet) {
        WithdrawalSheet(
            goalItem = goalItem,
            wallets = state.wallets,
            state = state,
            onDismiss = { 
                showWithdrawSheet = false
                onEvent(GoalEvent.ClearTransferState)
            },
            onEvent = { event ->
                onEvent(event)
                if (event is GoalEvent.WithdrawClicked) {
                    showWithdrawSheet = false
                }
            }
        )
    }

    if (showBuyNowSheet) {
        BuyNowSheet(
            goalItem = goalItem,
            onDismiss = { showBuyNowSheet = false },
            onConfirm = { amount, note ->
                onEvent(GoalEvent.BuyNowClicked(goalId, amount, note))
                showBuyNowSheet = false
            }
        )
    }

    if (showDeleteConfirm) {
        GoalDeleteDialog(
            goalItem = goalItem,
            wallets = state.wallets,
            onDismiss = { showDeleteConfirm = false },
            onConfirm = { action, destId ->
                onEvent(GoalEvent.ConfirmDelete(goalId, action, destId))
                showDeleteConfirm = false
            }
        )
    }

    // Premium Success Overlay
    AnimatedVisibility(
        visible = state.successMessage != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onEvent(GoalEvent.MessageShown) },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(Dim.SpacingXXLarge)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dim.SpacingXXLarge)
            ) {
                // Animated Check
                Surface(
                    color = Color(0xFF4CAF50),
                    shape = CircleShape,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(60.dp))
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)) {
                    Text(
                        state.successMessage ?: "Success!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Transaction completed successfully",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                state.lastTransferSuccess?.let { preview ->
                    MomentumCard(backgroundColor = Color.White.copy(alpha = 0.1f)) {
                        Column(
                            modifier = Modifier.padding(Dim.SpacingLarge),
                            verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    CurrencyFormatter.format(preview.sourceAmount, preview.sourceCurrency),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(horizontal = 12.dp).size(20.dp)
                                )
                                Text(
                                    CurrencyFormatter.format(preview.targetAmount, preview.targetCurrency),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            
                            if (preview.sourceCurrency != preview.targetCurrency) {
                                Text(
                                    "Rate: 1 ${preview.sourceCurrency} = ${"%.4f".format(preview.exchangeRate)} ${preview.targetCurrency}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                
                Button(
                    onClick = { onEvent(GoalEvent.MessageShown) },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(Dim.RadiusLarge),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("CONTINUE", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun AnalyticsMiniCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(Dim.RadiusMedium)
    ) {
        Column(modifier = Modifier.padding(Dim.SpacingMedium)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyNowSheet(
    goalItem: GoalItemUi,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf(goalItem.goal.targetAmount.toString()) }
    var note by remember { mutableStateOf("Purchased: ${goalItem.goal.title}") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(Dim.SpacingXLarge).padding(bottom = Dim.SpacingXXXLarge), verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
            Text("Goal Completion Purchase", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text("This will record an expense and deduct funds from '${goalItem.wallet.name}'.", style = MaterialTheme.typography.bodyMedium)
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Final Purchase Amount") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text(goalItem.wallet.currency + " ") }
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0, note) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("CONFIRM PURCHASE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GoalDeleteDialog(
    goalItem: GoalItemUi,
    wallets: List<com.example.projectpbd.domain.model.Wallet>,
    onDismiss: () -> Unit,
    onConfirm: (WalletDeleteAction, String?) -> Unit
) {
    var selectedAction by remember { mutableStateOf(WalletDeleteAction.TRANSFER_AND_DELETE) }
    var destinationWalletId by remember { mutableStateOf(wallets.firstOrNull { it.id != goalItem.goal.walletId }?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Goal Vault?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
                Text("This vault contains ${CurrencyFormatter.format(goalItem.currentAmount, goalItem.wallet.currency)}. Choose what happens to the funds:")
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedAction == WalletDeleteAction.TRANSFER_AND_DELETE, onClick = { selectedAction = WalletDeleteAction.TRANSFER_AND_DELETE })
                    Text("Transfer funds to another wallet", modifier = Modifier.padding(start = 8.dp))
                }
                if (selectedAction == WalletDeleteAction.TRANSFER_AND_DELETE) {
                    // Simple selector
                    wallets.forEach { w ->
                        if (w.id != goalItem.goal.walletId) {
                            Surface(onClick = { destinationWalletId = w.id }, color = if(destinationWalletId == w.id) MaterialTheme.colorScheme.primaryContainer else Color.Transparent) {
                                Text(w.name, modifier = Modifier.padding(8.dp).fillMaxWidth())
                            }
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedAction == WalletDeleteAction.ARCHIVE_WALLET, onClick = { selectedAction = WalletDeleteAction.ARCHIVE_WALLET })
                    Text("Archive vault wallet only", modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedAction == WalletDeleteAction.PERMANENTLY_DELETE, onClick = { selectedAction = WalletDeleteAction.PERMANENTLY_DELETE })
                    Text("Permanently delete funds (Not Recommended)", color = Color.Red, modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedAction, if (selectedAction == WalletDeleteAction.TRANSFER_AND_DELETE) destinationWalletId else null) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Confirm Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
