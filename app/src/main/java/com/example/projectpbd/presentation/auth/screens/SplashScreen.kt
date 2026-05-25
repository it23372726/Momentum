package com.example.projectpbd.presentation.auth.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projectpbd.presentation.auth.state.AuthState
import com.example.projectpbd.presentation.auth.viewmodel.AuthViewModel

@Composable
fun SplashScreen(
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit,
    onShowOnboarding: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.authState, uiState.isOnboardingCompleted) {
        if (uiState.authState is AuthState.Loading) return@LaunchedEffect

        when (uiState.authState) {
            AuthState.Authenticated -> {
                if (uiState.isOnboardingCompleted) {
                    onAuthenticated()
                } else {
                    onShowOnboarding()
                }
            }
            AuthState.Unauthenticated -> onUnauthenticated()
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Momentum",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Stay aware. Stay intentional.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
    }
}

