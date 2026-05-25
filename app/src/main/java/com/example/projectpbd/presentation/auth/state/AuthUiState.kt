package com.example.projectpbd.presentation.auth.state

data class AuthUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val snackbarMessage: String? = null,
    val toastMessage: String? = null,
    val authState: AuthState = AuthState.Loading,
    val isOnboardingCompleted: Boolean = true
)
