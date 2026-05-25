package com.example.projectpbd.presentation.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.projectpbd.presentation.auth.AuthRoutes

@Composable
fun DashboardRoute(
    onNavigateToHistory: () -> Unit,
    onEditActivity: (String, Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    DashboardScreen(
        state = uiState,
        onAddWallet = { navController.navigate(AuthRoutes.AddWallet) },
        onWalletClick = { id -> navController.navigate(AuthRoutes.addWallet(id)) },
        onNavigateToHistory = onNavigateToHistory,
        onEditActivity = onEditActivity,
        onSettingsClick = onSettingsClick
    )
}
