package com.example.projectpbd.presentation.goals.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpbd.core.util.CurrencyFormatter
import com.example.projectpbd.domain.model.Wallet
import com.example.projectpbd.presentation.components.MomentumCard
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.goals.state.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionSheet(
    goalItem: GoalItemUi,
    wallets: List<Wallet>,
    state: GoalUiState,
    onDismiss: () -> Unit,
    onEvent: (GoalEvent) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Input, 2: Confirmation
    var note by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val defaultSource = wallets.filter { it.id != goalItem.goal.walletId }.firstOrNull()?.id ?: ""
        onEvent(GoalEvent.InitializeTransfer(
            sourceId = defaultSource,
            destId = goalItem.goal.walletId
        ))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                }, label = "transfer_step"
            ) { targetStep ->
                when (targetStep) {
                    1 -> TransferInputContent(
                        title = "Contribute",
                        subtitle = "Moving funds to ${goalItem.goal.title}",
                        preview = state.transferPreview,
                        wallets = wallets.filter { it.id != goalItem.goal.walletId },
                        isContribution = true,
                        onAmountChange = { onEvent(GoalEvent.TransferAmountChanged(it)) },
                        onWalletSelect = { onEvent(GoalEvent.TransferSourceWalletSelected(it)) },
                        onNext = { step = 2 }
                    )
                    2 -> TransferConfirmationContent(
                        title = "Confirm Contribution",
                        preview = state.transferPreview,
                        note = note,
                        onNoteChange = { note = it },
                        onBack = { step = 1 },
                        onConfirm = {
                            onEvent(GoalEvent.ContributeClicked(
                                goalId = goalItem.goal.id,
                                sourceWalletId = state.transferPreview.sourceWalletId,
                                amount = state.transferPreview.sourceAmount,
                                note = note
                            ))
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawalSheet(
    goalItem: GoalItemUi,
    wallets: List<Wallet>,
    state: GoalUiState,
    onDismiss: () -> Unit,
    onEvent: (GoalEvent) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val defaultDest = wallets.filter { it.id != goalItem.goal.walletId }.firstOrNull()?.id ?: ""
        onEvent(GoalEvent.InitializeTransfer(
            sourceId = goalItem.goal.walletId,
            destId = defaultDest
        ))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                }, label = "transfer_step"
            ) { targetStep ->
                when (targetStep) {
                    1 -> TransferInputContent(
                        title = "Withdraw",
                        subtitle = "Moving funds out of vault",
                        preview = state.transferPreview,
                        wallets = wallets.filter { it.id != goalItem.goal.walletId },
                        isContribution = false,
                        onAmountChange = { onEvent(GoalEvent.TransferAmountChanged(it)) },
                        onWalletSelect = { onEvent(GoalEvent.TransferDestinationWalletSelected(it)) },
                        onNext = { step = 2 }
                    )
                    2 -> TransferConfirmationContent(
                        title = "Confirm Withdrawal",
                        preview = state.transferPreview,
                        note = note,
                        onNoteChange = { note = it },
                        onBack = { step = 1 },
                        onConfirm = {
                            onEvent(GoalEvent.WithdrawClicked(
                                goalId = goalItem.goal.id,
                                destinationWalletId = state.transferPreview.destinationWalletId,
                                amount = state.transferPreview.sourceAmount,
                                note = note
                            ))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TransferInputContent(
    title: String,
    subtitle: String,
    preview: TransferPreview,
    wallets: List<Wallet>,
    isContribution: Boolean,
    onAmountChange: (String) -> Unit,
    onWalletSelect: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = Dim.SpacingXLarge)
            .padding(bottom = Dim.SpacingXLarge),
        verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Transfer Flow Capsules
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowCapsule(
                name = if (isContribution) preview.sourceWalletName else "Vault",
                icon = if (isContribution) Icons.Default.Wallet else Icons.Default.Savings,
                modifier = Modifier.weight(1f),
                isActive = true
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
            FlowCapsule(
                name = if (isContribution) "Vault" else preview.targetWalletName,
                icon = if (isContribution) Icons.Default.Savings else Icons.Default.Wallet,
                modifier = Modifier.weight(1f),
                isActive = false
            )
        }

        // Amount Input Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dim.SpacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = preview.sourceCurrency,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(Dim.SpacingSmall))
                BasicTextField(
                    value = preview.amountInput,
                    onValueChange = onAmountChange,
                    textStyle = MaterialTheme.typography.displayLarge.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        color = if (preview.isInsufficientBalance) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(IntrinsicSize.Min),
                    decorationBox = { innerTextField ->
                        if (preview.amountInput.isEmpty()) {
                            Text(
                                "0.00",
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                fontWeight = FontWeight.Black
                            )
                        }
                        innerTextField()
                    }
                )
            }

            if (preview.isInsufficientBalance) {
                Text(
                    "Insufficient funds in source",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Live Conversion Bridge
        if (preview.sourceAmount > 0) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(Dim.RadiusLarge),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Dim.SpacingMedium), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SyncAlt, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(Dim.SpacingSmall))
                        Text(
                            text = CurrencyFormatter.format(preview.targetAmount, preview.targetCurrency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "Rate: 1 ${preview.sourceCurrency} = ${"%.4f".format(preview.exchangeRate)} ${preview.targetCurrency}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Wallet Selection Header
        Text(
            if (isContribution) "Source Wallet" else "Destination Wallet",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Wallet List
        LazyColumn(
            modifier = Modifier.heightIn(max = 240.dp),
            verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)
        ) {
            items(wallets) { wallet ->
                val isSelected = if (isContribution) preview.sourceWalletId == wallet.id else preview.destinationWalletId == wallet.id
                WalletCardItem(
                    wallet = wallet,
                    isSelected = isSelected,
                    onClick = { onWalletSelect(wallet.id) }
                )
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(Dim.RadiusLarge),
            enabled = preview.sourceAmount > 0 && !preview.isInsufficientBalance,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Text("Review Transfer", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }
    }
}

@Composable
private fun TransferConfirmationContent(
    title: String,
    preview: TransferPreview,
    note: String,
    onNoteChange: (String) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = Dim.SpacingXLarge)
            .padding(bottom = Dim.SpacingXLarge),
        verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)

        // Impact Summary
        MomentumCard {
            Column(modifier = Modifier.padding(Dim.SpacingLarge), verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)) {
                ImpactRow(
                    label = "Deducting from ${preview.sourceWalletName}",
                    amount = preview.sourceAmount,
                    currency = preview.sourceCurrency,
                    isNegative = true,
                    balanceAfter = preview.sourceBalanceAfter
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                
                ImpactRow(
                    label = "Adding to ${preview.targetWalletName}",
                    amount = preview.targetAmount,
                    currency = preview.targetCurrency,
                    isNegative = false,
                    balanceAfter = preview.targetBalanceAfter
                )
            }
        }

        // Exchange Rate Recap if needed
        if (preview.sourceCurrency != preview.targetCurrency) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(Dim.RadiusMedium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(Dim.SpacingMedium), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(Dim.SpacingSmall))
                    Text(
                        "Includes conversion at ${"%.4f".format(preview.exchangeRate)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            label = { Text("Transfer Note") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dim.RadiusLarge),
            placeholder = { Text("What's this for?") }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(64.dp),
                shape = RoundedCornerShape(Dim.RadiusLarge)
            ) {
                Text("Edit")
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(64.dp),
                shape = RoundedCornerShape(Dim.RadiusLarge)
            ) {
                Text("Confirm", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun ImpactRow(
    label: String,
    amount: Double,
    currency: String,
    isNegative: Boolean,
    balanceAfter: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = (if (isNegative) "-" else "+") + CurrencyFormatter.format(amount, currency),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isNegative) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text("New Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(CurrencyFormatter.format(balanceAfter, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FlowCapsule(
    name: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isActive: Boolean
) {
    Surface(
        modifier = modifier,
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(Dim.RadiusLarge),
        border = if (isActive) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dim.SpacingMedium, vertical = Dim.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WalletCardItem(
    wallet: Wallet,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(Dim.RadiusLarge),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Dim.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(wallet.currency.take(1), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(wallet.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = CurrencyFormatter.format(wallet.currentBalance, wallet.currency),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
