package com.example.projectpbd.presentation.wallets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.projectpbd.domain.model.CurrencyRegistry
import com.example.projectpbd.domain.model.WalletType
import com.example.projectpbd.presentation.components.MomentumButton
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.transactions.components.CurrencyPickerSheet
import com.example.projectpbd.presentation.wallets.state.WalletEvent
import com.example.projectpbd.presentation.wallets.state.WalletUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWalletScreen(
    state: WalletUiState,
    onEvent: (WalletEvent) -> Unit,
    onBack: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    val currencyInfo = remember(state.form.currency) { CurrencyRegistry.getByCode(state.form.currency) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            onEvent(WalletEvent.MessageShown)
            onBack()
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerSheet(
            onCurrencySelected = { onEvent(WalletEvent.CurrencySelected(it.code)) },
            onDismiss = { showCurrencyPicker = false }
        )
    }

    if (showDeleteConfirm) {
        // ... (existing AlertDialog code)
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Wallet?") },
            text = { Text("Are you sure you want to delete this wallet? All associated transaction references will remain, but the wallet will be gone.") },
            confirmButton = {
                TextButton(onClick = { 
                    onEvent(WalletEvent.DeleteClicked)
                    showDeleteConfirm = false
                }) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Wallet" else "New Wallet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dim.SpacingXLarge),
            verticalArrangement = Arrangement.spacedBy(Dim.SpacingXLarge)
        ) {
            OutlinedTextField(
                value = state.form.name,
                onValueChange = { onEvent(WalletEvent.NameChanged(it)) },
                label = { Text("Wallet Name") },
                placeholder = { Text("e.g. My Savings, Business") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
                Text(
                    text = "Wallet Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)
                ) {
                    items(WalletType.entries) { type ->
                        FilterChip(
                            selected = state.form.type == type,
                            onClick = { onEvent(WalletEvent.TypeSelected(type)) },
                            label = { Text(type.name) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
                Text(
                    text = "Currency",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    onClick = { showCurrencyPicker = true },
                    shape = RoundedCornerShape(Dim.RadiusMedium),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Dim.SpacingLarge),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
                    ) {
                        Text(text = currencyInfo.flag, fontSize = 24.sp)
                        Text(
                            text = "${currencyInfo.name} (${currencyInfo.code})",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.KeyboardArrowDown, null)
                    }
                }
            }

            OutlinedTextField(
                value = state.form.initialBalance,
                onValueChange = { onEvent(WalletEvent.BalanceChanged(it)) },
                label = { Text("Current Balance") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("${currencyInfo.symbol} ") },
                suffix = { Text(currencyInfo.code) },
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            MomentumButton(
                text = if (state.isEditing) "Update Wallet" else "Create Wallet",
                onClick = { 
                    onEvent(WalletEvent.SaveClicked)
                },
                isLoading = state.isLoading,
                enabled = state.form.name.isNotBlank()
            )
        }
    }
}
