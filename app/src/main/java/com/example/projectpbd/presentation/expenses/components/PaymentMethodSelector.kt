package com.example.projectpbd.presentation.expenses.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.projectpbd.domain.model.PaymentMethod
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim

@Composable
fun PaymentMethodSelector(
    selected: PaymentMethod,
    onSelect: (PaymentMethod) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)
    ) {
        items(PaymentMethod.entries) { method ->
            FilterChip(
                selected = selected == method,
                onClick = { onSelect(method) },
                label = { Text(text = methodLabel(method)) },
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}

private fun methodLabel(method: PaymentMethod): String {
    return method.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}
