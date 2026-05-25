package com.example.projectpbd.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.projectpbd.domain.model.RepeatConfiguration
import com.example.projectpbd.domain.model.RepeatFrequency
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim

@Composable
fun RepeatSelectorButton(
    config: RepeatConfiguration,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = config.isEnabled
    val containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val contentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val label = if (isActive) {
        val frequencyLabel = config.frequency.name.lowercase().replaceFirstChar { it.uppercase() }
        if (config.interval > 1) {
            "Every ${config.interval} ${getUnit(config.frequency, config.interval)}"
        } else {
            frequencyLabel
        }
    } else {
        "Does not repeat"
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dim.RadiusLarge),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dim.SpacingLarge, vertical = Dim.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isActive) MaterialTheme.colorScheme.primary else contentColor.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(Dim.SpacingMedium))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun RepeatConfigurationDialog(
    initialConfig: RepeatConfiguration,
    onDismiss: () -> Unit,
    onConfirm: (RepeatConfiguration) -> Unit
) {
    var frequency by remember { mutableStateOf(initialConfig.frequency) }
    var intervalStr by remember { mutableStateOf(initialConfig.interval.toString()) }
    
    val interval = intervalStr.toIntOrNull() ?: 1

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dim.SpacingMedium),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(Dim.SpacingXLarge),
                verticalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
            ) {
                Text(
                    text = "Repeat Schedule",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = Dim.SpacingSmall)
                )

                VerticalFrequencySelector(
                    selected = frequency,
                    onSelected = { frequency = it }
                )

                AnimatedVisibility(
                    visible = frequency != RepeatFrequency.NONE,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        IntervalInputField(
                            value = intervalStr,
                            onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() }) intervalStr = it },
                            unit = getUnit(frequency, interval)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dim.SpacingSmall))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Dim.RadiusMedium)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onConfirm(RepeatConfiguration(frequency, interval))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Dim.RadiusMedium),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text("Set Schedule", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalFrequencySelector(
    selected: RepeatFrequency,
    onSelected: (RepeatFrequency) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        RepeatFrequency.values().forEach { freq ->
            FrequencyRow(
                frequency = freq,
                isSelected = selected == freq,
                onClick = { onSelected(freq) }
            )
        }
    }
}

@Composable
private fun FrequencyRow(
    frequency: RepeatFrequency,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (frequency) {
        RepeatFrequency.NONE -> Icons.Default.Close
        RepeatFrequency.DAILY -> Icons.Default.DateRange
        RepeatFrequency.WEEKLY -> Icons.Default.DateRange
        RepeatFrequency.MONTHLY -> Icons.Default.DateRange
        RepeatFrequency.ANNUALLY -> Icons.Default.DateRange
    }

    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dim.RadiusMedium),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dim.SpacingMedium, vertical = Dim.SpacingLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = when(frequency) {
                    RepeatFrequency.NONE -> "No Repeat"
                    else -> frequency.name.lowercase().replaceFirstChar { it.uppercase() }
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun IntervalInputField(
    value: String,
    onValueChange: (String) -> Unit,
    unit: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)) {
        Text(
            text = "Repeat Every",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(100.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(Dim.RadiusMedium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Spacer(modifier = Modifier.width(Dim.SpacingLarge))
            Text(
                text = unit,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getUnit(frequency: RepeatFrequency, interval: Int): String {
    return when (frequency) {
        RepeatFrequency.DAILY -> if (interval == 1) "day" else "days"
        RepeatFrequency.WEEKLY -> if (interval == 1) "week" else "weeks"
        RepeatFrequency.MONTHLY -> if (interval == 1) "month" else "months"
        RepeatFrequency.ANNUALLY -> if (interval == 1) "year" else "years"
        else -> ""
    }
}
