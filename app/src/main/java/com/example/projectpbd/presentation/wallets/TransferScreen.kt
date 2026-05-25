package com.example.projectpbd.presentation.wallets

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpbd.core.util.CurrencyFormatter
import com.example.projectpbd.domain.model.CurrencyRegistry
import com.example.projectpbd.domain.model.Wallet
import com.example.projectpbd.domain.model.WalletType
import com.example.projectpbd.presentation.components.MomentumButton
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.wallets.state.TransferEvent
import com.example.projectpbd.presentation.wallets.state.TransferUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    state: TransferUiState,
    onEvent: (TransferEvent) -> Unit,
    onBack: () -> Unit
) {
    var showSourcePicker by remember { mutableStateOf(false) }
    var showDestPicker by remember { mutableStateOf(false) }

    val sourceWallet = remember(state.wallets, state.sourceWalletId) { state.wallets.find { it.id == state.sourceWalletId } }
    val destWallet = remember(state.wallets, state.destinationWalletId) { state.wallets.find { it.id == state.destinationWalletId } }

    if (state.success) {
        TransferSuccessView(onDismiss = {
            onEvent(TransferEvent.ResetSuccess)
            onBack()
        })
    }

    if (state.showConfirmation && sourceWallet != null && destWallet != null) {
        TransferConfirmationSheet(
            state = state,
            source = sourceWallet,
            dest = destWallet,
            onDismiss = { onEvent(TransferEvent.DismissConfirmation) },
            onConfirm = { onEvent(TransferEvent.Submit) }
        )
    }

    if (showSourcePicker) {
        WalletSelectionSheet(
            title = "Transfer From",
            wallets = state.wallets,
            selectedId = state.sourceWalletId ?: "",
            onSelect = { onEvent(TransferEvent.SourceSelected(it)); showSourcePicker = false },
            onDismiss = { showSourcePicker = false }
        )
    }

    if (showDestPicker) {
        WalletSelectionSheet(
            title = "Transfer To",
            wallets = state.wallets,
            selectedId = state.destinationWalletId ?: "",
            onSelect = { onEvent(TransferEvent.DestinationSelected(it)); showDestPicker = false },
            onDismiss = { showDestPicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Funds", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 8.dp) {
                Box(modifier = Modifier.padding(Dim.SpacingXLarge).navigationBarsPadding()) {
                    MomentumButton(
                        text = if (state.isInsufficientFunds) "INSUFFICIENT FUNDS" else "REVIEW TRANSFER",
                        onClick = { onEvent(TransferEvent.RequestConfirmation) },
                        enabled = sourceWallet != null && destWallet != null && state.amount.isNotEmpty() && !state.isInsufficientFunds,
                        containerColor = if (state.isInsufficientFunds) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dim.SpacingLarge))

            // 1. Flow Visual (FROM -> SWAP -> TO)
            Column(
                modifier = Modifier.padding(horizontal = Dim.SpacingXLarge),
                verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TransferWalletCard(
                    label = "FROM",
                    wallet = sourceWallet,
                    baseCurrency = state.baseCurrency,
                    onClick = { showSourcePicker = true }
                )

                Box(contentAlignment = Alignment.Center) {
                    VerticalDivider(modifier = Modifier.height(40.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Surface(
                        onClick = { onEvent(TransferEvent.SwapWallets) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.SwapVert, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                TransferWalletCard(
                    label = "TO",
                    wallet = destWallet,
                    baseCurrency = state.baseCurrency,
                    onClick = { showDestPicker = true }
                )
            }

            Spacer(modifier = Modifier.height(Dim.SpacingXXLarge))

            // 2. Amount Entry
            Text(
                "AMOUNT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                letterSpacing = 2.sp
            )

            TransferAmountInput(
                value = state.amount,
                onValueChange = { onEvent(TransferEvent.AmountChanged(it)) },
                currencyCode = sourceWallet?.currency ?: state.baseCurrency
            )

            // 3. Conversion Preview
            if (sourceWallet != null && destWallet != null && sourceWallet.currency != destWallet.currency) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(Dim.RadiusMedium),
                    modifier = Modifier.padding(top = Dim.SpacingSmall)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Dim.SpacingLarge, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "≈ ${CurrencyFormatter.format(state.convertedAmount, destWallet.currency)} (Rate: 1 ${sourceWallet.currency} = ${"%.4f".format(state.exchangeRate)} ${destWallet.currency})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dim.SpacingXXLarge))

            // 4. Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = { onEvent(TransferEvent.NotesChanged(it)) },
                label = { Text("Transfer Note (Optional)") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = Dim.SpacingXLarge),
                shape = RoundedCornerShape(Dim.RadiusMedium),
                placeholder = { Text("e.g. Monthly Savings, Rebalancing") }
            )
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun TransferWalletCard(
    label: String,
    wallet: Wallet?,
    baseCurrency: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Dim.RadiusLarge),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(Dim.SpacingLarge).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when(wallet?.type) {
                    WalletType.CASH -> Icons.Default.Payments
                    WalletType.BANK -> Icons.Default.AccountBalance
                    WalletType.SAVINGS -> Icons.Default.Savings
                    else -> Icons.Default.AccountBalanceWallet
                }
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Text(wallet?.name ?: "Select Wallet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (wallet != null) {
                    Text(
                        text = CurrencyFormatter.format(wallet.currentBalance, wallet.currency),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (wallet != null && wallet.currency != baseCurrency) {
                 Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
                     Text(
                         text = wallet.currency,
                         modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                         style = MaterialTheme.typography.labelSmall,
                         fontWeight = FontWeight.Bold
                     )
                 }
            }
            
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun TransferAmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    currencyCode: String
) {
    val currencyInfo = CurrencyRegistry.getByCode(currencyCode)
    
    Row(
        modifier = Modifier.padding(vertical = Dim.SpacingLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = currencyInfo.symbol,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.padding(end = 8.dp)
        )
        
        BasicTextField(
            value = value,
            onValueChange = { 
                if (it.isEmpty() || (it.length <= 10 && it.all { char -> char.isDigit() || char == '.' })) {
                    onValueChange(it)
                }
            },
            textStyle = TextStyle(
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) {
                    if (value.isEmpty()) {
                        Text(
                            "0",
                            style = TextStyle(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletSelectionSheet(
    title: String,
    wallets: List<Wallet>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = Dim.SpacingXXLarge)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, modifier = Modifier.padding(Dim.SpacingLarge))
            
            wallets.forEach { wallet ->
                ListItem(
                    headlineContent = { Text(wallet.name, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(CurrencyFormatter.format(wallet.currentBalance, wallet.currency)) },
                    leadingContent = {
                        val info = CurrencyRegistry.getByCode(wallet.currency)
                        Text(info.flag, fontSize = 24.sp)
                    },
                    trailingContent = {
                        if (wallet.id == selectedId) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.clickable { onSelect(wallet.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransferConfirmationSheet(
    state: TransferUiState,
    source: Wallet,
    dest: Wallet,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(Dim.SpacingXLarge).padding(bottom = Dim.SpacingXXLarge),
            verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            Text("Review Transfer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(Dim.RadiusMedium))
                    .padding(Dim.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
            ) {
                BalancePreviewRow("From: ${source.name}", source.currentBalance, state.sourceResultingBalance, source.currency)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                BalancePreviewRow("To: ${dest.name}", dest.currentBalance, state.destResultingBalance, dest.currency)
            }

            Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)) {
                DetailRow("Transfer Amount", CurrencyFormatter.format(state.amount.toDoubleOrNull() ?: 0.0, source.currency))
                if (source.currency != dest.currency) {
                    DetailRow("Exchange Rate", "1 ${source.currency} = ${"%.4f".format(state.exchangeRate)} ${dest.currency}")
                    DetailRow("Receiving Amount", CurrencyFormatter.format(state.convertedAmount, dest.currency), isHighlight = true)
                }
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(Dim.RadiusMedium)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("CONFIRM TRANSFER", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun BalancePreviewRow(label: String, before: Double, after: Double, currency: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(CurrencyFormatter.format(before, currency), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
            Text(CurrencyFormatter.format(after, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isHighlight) FontWeight.Black else FontWeight.Bold,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TransferSuccessView(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val scale by animateFloatAsState(
                targetValue = 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "SuccessScale"
            )
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check, 
                    null, 
                    modifier = Modifier.size(60.dp), 
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(Dim.SpacingXXLarge))
            
            Text(
                "Transfer Complete", 
                style = MaterialTheme.typography.headlineMedium, 
                fontWeight = FontWeight.Black, 
                color = Color.White
            )
            Text(
                "Your balances have been updated live.", 
                style = MaterialTheme.typography.bodyMedium, 
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(Dim.SpacingXXXLarge))
            
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(Dim.RadiusMedium),
                modifier = Modifier.padding(horizontal = Dim.SpacingXLarge).fillMaxWidth()
            ) {
                Text("DONE", fontWeight = FontWeight.Black)
            }
        }
    }
}
