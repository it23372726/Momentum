package com.example.projectpbd.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpbd.domain.model.CurrencyRegistry
import com.example.projectpbd.presentation.components.MomentumCard
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.settings.state.SettingsUiState
import com.example.projectpbd.presentation.transactions.components.CurrencyPickerSheet
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onUpdateBaseCurrency: (String) -> Unit,
    onUpdateDefaultWallet: (String?) -> Unit,
    onRefreshRates: () -> Unit,
    onManageCategories: () -> Unit,
    onClearCache: () -> Unit,
    onLogout: () -> Unit,
    onClearMessages: () -> Unit
) {
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showWalletPicker by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?") },
            text = { Text("Are you sure you want to log out from Momentum?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val lastUpdateLabel = remember(state.lastExchangeUpdate) {
        if (state.lastExchangeUpdate == 0L) "Never"
        else Instant.ofEpochMilli(state.lastExchangeUpdate)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
    }

    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessages()
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerSheet(
            onCurrencySelected = { 
                onUpdateBaseCurrency(it.code)
                showCurrencyPicker = false
            },
            onDismiss = { showCurrencyPicker = false }
        )
    }

    if (showWalletPicker) {
        ModalBottomSheet(
            onDismissRequest = { showWalletPicker = false }
        ) {
            Column(modifier = Modifier.padding(bottom = Dim.SpacingXXLarge)) {
                Text(
                    "Select Default Wallet", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(Dim.SpacingLarge)
                )
                state.wallets.forEach { wallet ->
                    ListItem(
                        headlineContent = { Text(wallet.name) },
                        supportingContent = { Text(wallet.currency) },
                        leadingContent = { 
                            val currency = CurrencyRegistry.getByCode(wallet.currency)
                            Text(currency.flag, fontSize = 24.sp)
                        },
                        trailingContent = {
                            if (wallet.id == state.defaultWalletId) {
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.clickable {
                            onUpdateDefaultWallet(wallet.id)
                            showWalletPicker = false
                        }
                    )
                }
                ListItem(
                    headlineContent = { Text("None", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        onUpdateDefaultWallet(null)
                        showWalletPicker = false
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dim.SpacingLarge),
            verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            // 1. General
            item { SettingsSectionHeader("General") }
            item {
                MomentumCard {
                    SettingsRow(
                        icon = Icons.Default.CurrencyExchange,
                        title = "Base Currency",
                        subtitle = CurrencyRegistry.getByCode(state.baseCurrency).run { "$name ($code)" },
                        onClick = { showCurrencyPicker = true }
                    )
                    SettingsRow(
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Default Wallet",
                        subtitle = state.wallets.find { it.id == state.defaultWalletId }?.name ?: "None",
                        onClick = { showWalletPicker = true }
                    )
                    SettingsRow(
                        icon = Icons.Default.Update,
                        title = "Exchange Rates",
                        subtitle = "Last updated: $lastUpdateLabel",
                        trailing = {
                            if (state.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                IconButton(onClick = onRefreshRates) {
                                    Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    )
                }
            }

            // 2. Financial Preferences
            item { SettingsSectionHeader("Financial Preferences") }
            item {
                MomentumCard {
                    SettingsRow(
                        icon = Icons.Default.Category,
                        title = "Manage Categories",
                        subtitle = "Customize income & expense labels",
                        onClick = onManageCategories
                    )
                }
            }

            // 3. Data Management
            item { SettingsSectionHeader("Data Management") }
            item {
                MomentumCard {
                    SettingsRow(
                        icon = Icons.Default.IosShare,
                        title = "Export Data",
                        subtitle = "CSV, PDF, JSON (Coming soon)",
                        onClick = { /* Future */ }
                    )
                    SettingsRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear Cache",
                        subtitle = "Reset exchange rate cache",
                        onClick = onClearCache,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                }
            }

            // 4. About
            item { SettingsSectionHeader("About") }
            item {
                MomentumCard {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "Version",
                        subtitle = "1.0.0 (BETA)",
                        onClick = {}
                    )
                    SettingsRow(
                        icon = Icons.Default.Code,
                        title = "Architecture",
                        subtitle = "MVVM + Hilt + DataStore + Firebase",
                        onClick = {}
                    )
                }
            }

            // 5. Account
            item { SettingsSectionHeader("Account") }
            item {
                MomentumCard {
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Log Out",
                        subtitle = "Sign out of your session",
                        onClick = { showLogoutDialog = true },
                        contentColor = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(Dim.SpacingXXLarge))
                Text(
                    "Momentum uses ExchangeRate-API for global currency conversion.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = Dim.SpacingMedium)
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = Dim.SpacingMedium, vertical = Dim.SpacingSmall)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dim.SpacingLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (contentColor == MaterialTheme.colorScheme.onSurface) MaterialTheme.colorScheme.primary else contentColor
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.Default.ChevronRight, 
                    null, 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}
