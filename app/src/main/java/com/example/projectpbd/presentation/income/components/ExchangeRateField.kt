package com.example.projectpbd.presentation.income.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim

@Composable
fun ExchangeRateField(
    currency: String,
    exchangeRate: String,
    convertedAmount: String,
    onRateChange: (String) -> Unit,
    onConvertedChange: (String) -> Unit
) {
    if (currency == "LKR") return

    Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
        ) {
            OutlinedTextField(
                value = exchangeRate,
                onValueChange = onRateChange,
                label = { Text("Rate") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value = convertedAmount,
                onValueChange = onConvertedChange,
                label = { Text("LKR Value") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }
        Text(
            "Optional: Helps calculate total balance in LKR.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
