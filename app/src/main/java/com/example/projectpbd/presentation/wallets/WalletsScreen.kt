package com.example.projectpbd.presentation.wallets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.wallets.components.WalletBalanceHeader
import com.example.projectpbd.presentation.wallets.components.WalletCard
import com.example.projectpbd.presentation.wallets.state.WalletUiState
import com.example.projectpbd.presentation.wallets.state.WalletEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    state: WalletUiState,
    onEvent: (WalletEvent) -> Unit,
    onAddWallet: () -> Unit,
    onTransfer: () -> Unit,
    onWalletClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wallets", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onTransfer) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddWallet,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Wallet")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            WalletBalanceHeader(totalBalance = state.totalBalance, baseCurrency = state.baseCurrency)
            
            if (state.wallets.isEmpty() && !state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No wallets found. Create one to get started.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(Dim.SpacingLarge),
                    verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.wallets) { wallet ->
                        WalletCard(
                            wallet = wallet,
                            baseCurrency = state.baseCurrency,
                            onClick = { onWalletClick(wallet.id) }
                        )
                    }
                }
            }
        }
    }
}
