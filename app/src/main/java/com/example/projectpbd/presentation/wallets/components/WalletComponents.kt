package com.example.projectpbd.presentation.wallets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpbd.core.util.CurrencyFormatter
import com.example.projectpbd.domain.model.WalletType
import com.example.projectpbd.presentation.components.MomentumCard
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.wallets.state.WalletItemUi

@Composable
fun WalletCard(
    wallet: WalletItemUi,
    baseCurrency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = getWalletGradient(wallet.type)
    
    MomentumCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradient))
                .padding(Dim.SpacingXLarge)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(wallet.flagEmoji, fontSize = 16.sp)
                            }
                        }
                        Text(
                            text = wallet.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(Dim.RadiusSmall)
                    ) {
                        Text(
                            text = wallet.currencyCode,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Column {
                    Text(
                        text = CurrencyFormatter.format(wallet.originalBalance, wallet.currencyCode),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "≈ ${CurrencyFormatter.format(wallet.convertedBalanceLkr, baseCurrency)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun WalletBalanceHeader(totalBalance: Double, baseCurrency: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dim.SpacingXLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Total Available Balance",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = CurrencyFormatter.format(totalBalance, baseCurrency),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
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

private fun getWalletGradient(type: WalletType): List<Color> {
    return when (type) {
        WalletType.CASH -> listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
        WalletType.BANK -> listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
        WalletType.DIGITAL -> listOf(Color(0xFFAD1457), Color(0xFF880E4F))
        WalletType.CRYPTO -> listOf(Color(0xFFFF8F00), Color(0xFFE65100))
        WalletType.SAVINGS -> listOf(Color(0xFF00838F), Color(0xFF006064))
        WalletType.CUSTOM -> listOf(Color(0xFF455A64), Color(0xFF263238))
    }
}
