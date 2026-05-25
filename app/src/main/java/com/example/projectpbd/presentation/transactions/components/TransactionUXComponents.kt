package com.example.projectpbd.presentation.transactions.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpbd.core.util.CurrencyFormatter
import com.example.projectpbd.domain.model.*
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.transactions.util.CategoryVisualMapper

import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun PreviewTransactionAmountField() {
    MaterialTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            TransactionAmountField(amount = "2500", onAmountChange = {}, isIncome = false, currencyCode = "LKR")
            TransactionAmountField(amount = "120", onAmountChange = {}, isIncome = true, currencyCode = "USD")
            TransactionAmountField(amount = "50", onAmountChange = {}, isIncome = false, currencyCode = "EUR")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerSheet(
    onCurrencySelected: (CurrencyInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCurrencies = remember(searchQuery) {
        if (searchQuery.isBlank()) CurrencyRegistry.currencies
        else CurrencyRegistry.currencies.filter { 
            it.code.contains(searchQuery, ignoreCase = true) ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.country.contains(searchQuery, ignoreCase = true)
        }
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
                .fillMaxHeight(0.85f)
                .padding(horizontal = Dim.SpacingXLarge)
        ) {
            Text(
                text = "Select Currency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            
            Spacer(modifier = Modifier.height(Dim.SpacingLarge))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, code or country") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(Dim.RadiusMedium),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            
            Spacer(modifier = Modifier.height(Dim.SpacingLarge))
            
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)
            ) {
                items(filteredCurrencies) { currency ->
                    CurrencyListItem(
                        currency = currency,
                        onClick = {
                            onCurrencySelected(currency)
                            onDismiss()
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dim.SpacingXLarge))
        }
    }
}

@Composable
private fun CurrencyListItem(
    currency: CurrencyInfo,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Dim.RadiusMedium),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Dim.SpacingLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            Text(text = currency.flag, fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currency.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currency.country,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = currency.code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TransactionAmountField(
    amount: String,
    onAmountChange: (String) -> Unit,
    isIncome: Boolean,
    currencyCode: String = "LKR",
    modifier: Modifier = Modifier
) {
    val currencyInfo = remember(currencyCode) { CurrencyRegistry.getByCode(currencyCode) }
    val textColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    
    // Dynamic font sizing to prevent clipping
    val fontSize = when {
        amount.length > 8 -> 32.sp
        amount.length > 6 -> 40.sp
        else -> 48.sp
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dim.SpacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AMOUNT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            letterSpacing = 2.sp
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = Dim.SpacingSmall)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 12.dp, top = 8.dp)
            ) {
                Text(
                    text = currencyInfo.symbol,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.4f)
                )
                Text(
                    text = currencyInfo.code,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = textColor.copy(alpha = 0.3f)
                )
            }
            
            BasicTextField(
                value = amount,
                onValueChange = { 
                    if (it.isEmpty() || (it.length <= 10 && it.all { char -> char.isDigit() || char == '.' })) {
                        onAmountChange(it)
                    }
                },
                modifier = Modifier.widthIn(min = 40.dp, max = 280.dp),
                textStyle = TextStyle(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-1).sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = SolidColor(textColor),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.Center) {
                        if (amount.isEmpty()) {
                            Text(
                                "0.00",
                                style = TextStyle(
                                    fontSize = fontSize,
                                    fontWeight = FontWeight.Black,
                                    color = textColor.copy(alpha = 0.15f),
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
}

@Composable
fun TransactionSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dim.SpacingXLarge),
        verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            letterSpacing = 1.5.sp
        )
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(Dim.RadiusExtraLarge),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(Dim.SpacingLarge),
                content = content
            )
        }
    }
}

@Composable
fun WalletCarousel(
    wallets: List<Wallet>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        contentPadding = PaddingValues(horizontal = Dim.SpacingSmall),
        horizontalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)
    ) {
        items(wallets) { wallet ->
            CompactWalletItem(
                wallet = wallet,
                isSelected = wallet.id == selectedId,
                onClick = { onSelect(wallet.id) }
            )
        }
    }
}

@Composable
private fun CompactWalletItem(
    wallet: Wallet,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val gradient = getWalletGradient(wallet.type)
    Surface(
        onClick = onClick,
        modifier = Modifier.width(140.dp).height(80.dp),
        shape = RoundedCornerShape(Dim.RadiusMedium),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.background(Brush.linearGradient(gradient)).padding(Dim.SpacingMedium)) {
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = wallet.name, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Color.White.copy(alpha = 0.7f), 
                    maxLines = 1
                )
                Text(
                    text = CurrencyFormatter.format(wallet.currentBalance, wallet.currency), 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = Color.White, 
                    fontWeight = FontWeight.Black
                )
            }
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RepeatSummaryCard(
    config: RepeatConfiguration,
    onClick: () -> Unit
) {
    val summary = if (config.isEnabled) {
        val freq = config.frequency.name.lowercase().replaceFirstChar { it.uppercase() }
        if (config.interval > 1) "Every ${config.interval} ${config.frequency.name.lowercase()}s" else freq
    } else "No Repeat"

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dim.RadiusMedium),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(Dim.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)
        ) {
            Icon(Icons.Outlined.Repeat, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(summary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        }
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

@Composable
fun CategorySelectorGrid(
    categories: List<Category>,
    selectedId: String,
    onSelect: (String) -> Unit,
    onAddCategory: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(80.dp),
        modifier = Modifier.heightIn(max = 240.dp),
        contentPadding = PaddingValues(Dim.SpacingSmall),
        horizontalArrangement = Arrangement.spacedBy(Dim.SpacingSmall),
        verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)
    ) {
        items(categories) { category ->
            CategoryGridItem(
                category = category,
                isSelected = category.id == selectedId,
                onClick = { onSelect(category.id) }
            )
        }
        item {
            AddCategoryGridItem(onClick = onAddCategory)
        }
    }
}

@Composable
private fun CategoryGridItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val visuals = remember(category) { CategoryVisualMapper.getVisuals(category) }
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Dim.RadiusLarge),
        color = if (isSelected) visuals.color.copy(alpha = 0.15f) else Color.Transparent,
        modifier = Modifier.padding(Dim.SpacingXXSmall)
    ) {
        Column(
            modifier = Modifier.padding(Dim.SpacingSmall),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = if (isSelected) visuals.color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                tonalElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = visuals.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else visuals.color.copy(alpha = 0.7f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                textAlign = TextAlign.Center,
                color = if (isSelected) visuals.color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddCategoryGridItem(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Dim.RadiusLarge),
        color = Color.Transparent,
        modifier = Modifier.padding(Dim.SpacingXXSmall)
    ) {
        Column(
            modifier = Modifier.padding(Dim.SpacingSmall),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(2.dp, Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Add, 
                        null, 
                        modifier = Modifier.size(26.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Text(
                "Add New", 
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun TransactionTypeHeader(
    isIncome: Boolean,
    isEditing: Boolean,
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dim.SpacingMedium, vertical = Dim.SpacingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }

            Text(
                text = if (isEditing) "Edit ${if (isIncome) "Income" else "Expense"}"
                       else "New ${if (isIncome) "Income" else "Expense"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
fun ModernDateSelector(
    dateLabel: String,
    onClick: () -> Unit,
    helperText: String? = null
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dim.RadiusLarge),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(Dim.SpacingLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            Icon(Icons.Outlined.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text("Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(dateLabel, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                helperText?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun TransactionNotesField(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dim.RadiusLarge),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dim.SpacingLarge, vertical = 4.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            Icon(
                Icons.Outlined.Description, 
                null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
            TextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { Text("Add notes (optional)...") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 3
            )
        }
    }
}

@Composable
fun SaveTransactionBar(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 12.dp,
        shadowElevation = 12.dp
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dim.SpacingXLarge, vertical = Dim.SpacingLarge)
                .height(58.dp),
            enabled = enabled && !isLoading,
            shape = RoundedCornerShape(Dim.RadiusLarge),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 0.dp
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), 
                    color = Color.White, 
                    strokeWidth = 3.dp
                )
            } else {
                Text(
                    text = text.uppercase(), 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
