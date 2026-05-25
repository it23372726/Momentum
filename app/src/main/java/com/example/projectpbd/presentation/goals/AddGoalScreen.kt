package com.example.projectpbd.presentation.goals

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpbd.domain.model.CurrencyRegistry
import com.example.projectpbd.domain.model.Wallet
import com.example.projectpbd.presentation.components.MomentumButton
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.goals.state.GoalUiState
import com.example.projectpbd.presentation.goals.state.GoalEvent
import com.example.projectpbd.presentation.transactions.components.CurrencyPickerSheet
import com.example.projectpbd.presentation.transactions.util.CategoryVisualMapper
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    state: GoalUiState,
    onEvent: (GoalEvent) -> Unit,
    onBack: () -> Unit,
    onCreateVault: (String, String) -> Unit
) {
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") }
    var showVaultDialog by remember { mutableStateOf(false) }
    var showExistingWalletPicker by remember { mutableStateOf(false) }
    var newVaultName by remember { mutableStateOf("") }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("LKR") }

    if (showVaultDialog) {
        AlertDialog(
            onDismissRequest = { showVaultDialog = false },
            title = { Text("Initialize Savings Vault") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
                    Text("Every goal needs a dedicated vault (wallet) to hold its funds securely.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = newVaultName,
                        onValueChange = { newVaultName = it },
                        label = { Text("Vault Name") },
                        placeholder = { Text("e.g. Travel Fund") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedCard(
                        onClick = { showCurrencyPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(CurrencyRegistry.getByCode(selectedCurrency).flag, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(selectedCurrency, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateVault(newVaultName, selectedCurrency)
                        showVaultDialog = false
                    },
                    enabled = newVaultName.isNotBlank()
                ) {
                    Text("Create Vault")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVaultDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCurrencyPicker) {
        CurrencyPickerSheet(
            onCurrencySelected = { 
                selectedCurrency = it.code
                showCurrencyPicker = false
            },
            onDismiss = { showCurrencyPicker = false }
        )
    }

    if (showExistingWalletPicker) {
        ModalBottomSheet(onDismissRequest = { showExistingWalletPicker = false }) {
            Column(modifier = Modifier.padding(Dim.SpacingXLarge).padding(bottom = Dim.SpacingXXXLarge)) {
                Text("Select Existing Wallet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(Dim.SpacingMedium))
                state.wallets.forEach { wallet ->
                    ListItem(
                        headlineContent = { Text(wallet.name) },
                        supportingContent = { Text(wallet.currency) },
                        leadingContent = { 
                             val info = CurrencyRegistry.getByCode(wallet.currency)
                             Text(info.flag, fontSize = 20.sp)
                        },
                        modifier = Modifier.clickable {
                            onEvent(GoalEvent.WalletSelected(wallet.id))
                            showExistingWalletPicker = false
                        }
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.form.goalId != null) "Edit Financial Goal" else "New Financial Goal", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dim.SpacingLarge),
            verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            item {
                OutlinedTextField(
                    value = state.form.title,
                    onValueChange = { onEvent(GoalEvent.TitleChanged(it)) },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. Dream Wedding") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dim.RadiusMedium)
                )
            }

            item {
                OutlinedTextField(
                    value = state.form.targetAmount,
                    onValueChange = { onEvent(GoalEvent.TargetAmountChanged(it)) },
                    label = { Text("Target Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(Dim.RadiusMedium)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)) {
                    Text("Goal Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)) {
                        items(state.categories) { category ->
                            val visuals = CategoryVisualMapper.getVisuals(category)
                            FilterChip(
                                selected = state.form.categoryId == category.id,
                                onClick = { onEvent(GoalEvent.CategorySelected(category.id)) },
                                label = { 
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(visuals.icon, null, modifier = Modifier.size(16.dp), tint = visuals.color)
                                        Text(category.name) 
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item {
                val calendar = Calendar.getInstance().apply { timeInMillis = state.form.targetDateMillis }
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val selected = Calendar.getInstance().apply { 
                            set(year, month, day, 23, 59, 59) 
                        }.timeInMillis
                        onEvent(GoalEvent.TargetDateChanged(selected))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                
                OutlinedCard(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Target Date", style = MaterialTheme.typography.labelSmall)
                            Text(Instant.ofEpochMilli(state.form.targetDateMillis).atZone(ZoneId.systemDefault()).format(formatter), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)) {
                    Text("Dedicated Vault", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    
                    if (state.form.walletId.isEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
                            Button(
                                onClick = { showVaultDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("New Vault")
                            }
                            OutlinedButton(
                                onClick = { showExistingWalletPicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Wallet, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Use Existing")
                            }
                        }
                    } else {
                        // Show selected vault status
                        OutlinedCard(modifier = Modifier.fillMaxWidth(), onClick = { showExistingWalletPicker = true }) {
                             Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                 Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary)
                                 Spacer(modifier = Modifier.width(12.dp))
                                 Text("Vault Selected", fontWeight = FontWeight.Bold)
                                 Spacer(modifier = Modifier.weight(1f))
                                 Icon(Icons.Default.CheckCircle, null, tint = Color.Green)
                             }
                        }
                        TextButton(onClick = { onEvent(GoalEvent.WalletSelected("")) }) {
                            Text("Clear Selection", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Dim.SpacingLarge))
                MomentumButton(
                    text = if (state.form.goalId != null) "UPDATE VAULT GOAL" else "CREATE VAULT GOAL",
                    onClick = { 
                        if (state.form.goalId != null) onEvent(GoalEvent.UpdateGoalClicked)
                        else onEvent(GoalEvent.SaveGoalClicked)
                    },
                    isLoading = state.isLoading,
                    enabled = state.form.title.isNotBlank() && state.form.targetAmount.isNotBlank() && state.form.walletId.isNotBlank()
                )
            }
        }
    }
}
