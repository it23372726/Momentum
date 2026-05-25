package com.example.projectpbd.presentation.main

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.projectpbd.presentation.auth.AuthRoutes
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem(AuthRoutes.Dashboard, "Home", Icons.Default.Home)
    object History : BottomNavItem(AuthRoutes.History, "Activity", Icons.AutoMirrored.Filled.List)
    object Wallets : BottomNavItem(AuthRoutes.Wallets, "Wallets", Icons.Default.AccountBalanceWallet)
    object Analytics : BottomNavItem(AuthRoutes.Analytics, "Insights", Icons.Default.Info)
    object Goals : BottomNavItem(AuthRoutes.GoalDetails, "Goals", Icons.Default.Star)
}

@Composable
fun MainScreen(
    navController: NavController,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.History,
        BottomNavItem.Wallets,
        BottomNavItem.Analytics,
        BottomNavItem.Goals
    )

    var showQuickActions by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = items.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = Dim.ElevationLow
                ) {
                    items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    imageVector = item.icon, 
                                    contentDescription = item.title,
                                    modifier = Modifier.size(Dim.IconMedium)
                                ) 
                            },
                            label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(bottom = Dim.SpacingSmall)
                ) {
                    AnimatedVisibility(
                        visible = showQuickActions,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        Column(
                            modifier = Modifier.padding(bottom = Dim.SpacingLarge),
                            verticalArrangement = Arrangement.spacedBy(Dim.SpacingMedium),
                            horizontalAlignment = Alignment.End
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = { 
                                    showQuickActions = false
                                    onAddIncome()
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White,
                                icon = { Icon(Icons.Default.KeyboardArrowUp, null) },
                                text = { Text("Income") }
                            )
                            ExtendedFloatingActionButton(
                                onClick = {
                                    showQuickActions = false
                                    onAddExpense()
                                },
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White,
                                icon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                                text = { Text("Expense") }
                            )
                        }
                    }
                    
                    FloatingActionButton(
                        onClick = { showQuickActions = !showQuickActions },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(Dim.ElevationMedium)
                    ) {
                        Icon(
                            if (showQuickActions) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Quick Actions",
                            modifier = Modifier.size(Dim.IconLarge)
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        content(innerPadding)
    }
}
