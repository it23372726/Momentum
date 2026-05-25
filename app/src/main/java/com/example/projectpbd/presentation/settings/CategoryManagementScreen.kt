package com.example.projectpbd.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpbd.domain.model.Category
import com.example.projectpbd.domain.model.CategorySource
import com.example.projectpbd.domain.model.CategoryType
import com.example.projectpbd.presentation.components.MomentumCard
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import com.example.projectpbd.presentation.transactions.util.CategoryVisualMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    state: CategoryManagementUiState,
    onBack: () -> Unit,
    onTypeSelected: (CategoryType) -> Unit,
    onAddCategory: (String, String?, String?) -> Unit,
    onUpdateCategory: (Category) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onClearMessages: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Add Category")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = if (state.selectedType == CategoryType.EXPENSE) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = state.selectedType == CategoryType.EXPENSE,
                    onClick = { onTypeSelected(CategoryType.EXPENSE) },
                    text = { Text("Expense") }
                )
                Tab(
                    selected = state.selectedType == CategoryType.INCOME,
                    onClick = { onTypeSelected(CategoryType.INCOME) },
                    text = { Text("Income") }
                )
            }

            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Dim.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)
            ) {
                items(state.categories) { category ->
                    CategoryListItem(
                        category = category,
                        onEdit = { categoryToEdit = category },
                        onDelete = { onDeleteCategory(category.id) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog || categoryToEdit != null) {
        CategoryEditDialog(
            category = categoryToEdit,
            onDismiss = { 
                showAddDialog = false
                categoryToEdit = null
            },
            onConfirm = { name, icon, color ->
                if (categoryToEdit != null) {
                    onUpdateCategory(categoryToEdit!!.copy(name = name, iconKey = icon, colorKey = color))
                } else {
                    onAddCategory(name, icon, color)
                }
                showAddDialog = false
                categoryToEdit = null
            }
        )
    }
}

@Composable
fun CategoryListItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val visuals = remember(category) { CategoryVisualMapper.getVisuals(category) }
    
    MomentumCard(
        onClick = if (category.source == CategorySource.USER) onEdit else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dim.SpacingLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dim.SpacingLarge)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = visuals.color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = visuals.icon,
                        contentDescription = null,
                        tint = visuals.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    if (category.source == CategorySource.SYSTEM) "System Default" else "User Created",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            if (category.source == CategorySource.USER) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            } else {
                Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun CategoryEditDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    val icons = listOf("shopping_cart", "restaurant", "directions_car", "home", "star", "payments", "work", "school")
    val colors = listOf("#4CAF50", "#F44336", "#2196F3", "#FFC107", "#9C27B0", "#00BCD4", "#795548", "#607D8B")
    
    var selectedIcon by remember { mutableStateOf(category?.iconKey ?: icons.first()) }
    var selectedColor by remember { mutableStateOf(category?.colorKey ?: colors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "New Category" else "Edit Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Text("Icon", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.take(4).forEach { icon ->
                        FilterChip(
                            selected = selectedIcon == icon,
                            onClick = { selectedIcon = icon },
                            label = { Text(icon.take(1).uppercase()) }
                        )
                    }
                }

                Text("Color", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.take(4).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .clickable { selectedColor = color }
                                .let { 
                                    if (selectedColor == color) {
                                        it.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else {
                                        it
                                    }
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedIcon, selectedColor) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
