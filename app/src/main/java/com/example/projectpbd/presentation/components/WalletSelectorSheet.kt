package com.example.projectpbd.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectpbd.core.util.CurrencyFormatter
import com.example.projectpbd.domain.model.Wallet
import com.example.projectpbd.domain.model.WalletType
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSelectorSheet(
    wallets: List<Wallet>,
    selectedWalletId: String,
    onWalletSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dim.SpacingXXLarge)
        ) {
            Text(
                text = "Select Wallet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(Dim.SpacingLarge)
            )
            
            LazyColumn {
                items(wallets) { wallet ->
                    WalletItem(
                        wallet = wallet,
                        isSelected = wallet.id == selectedWalletId,
                        onClick = {
                            onWalletSelected(wallet.id)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletItem(
    wallet: Wallet,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dim.SpacingLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            Icon(
                imageVector = getWalletIcon(wallet.type),
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = CurrencyFormatter.format(wallet.currentBalance, wallet.currency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                RadioButton(selected = true, onClick = null)
            }
        }
    }
}

private fun getWalletIcon(type: WalletType): ImageVector {
    return when (type) {
        WalletType.CASH -> Icons.Default.Payments
        WalletType.BANK -> Icons.Default.AccountBalance
        WalletType.DIGITAL -> Icons.Default.AccountBalanceWallet
        WalletType.CRYPTO -> Icons.Default.CurrencyBitcoin
        WalletType.SAVINGS -> Icons.Default.Savings
        WalletType.CUSTOM -> Icons.Default.Wallet
    }
}
