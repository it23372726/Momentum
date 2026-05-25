package com.example.projectpbd.presentation.auth.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectpbd.data.repository.AuthRepository
import com.example.projectpbd.presentation.auth.state.AuthState
import com.example.projectpbd.presentation.auth.state.AuthUiState
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val settingsRepository: com.example.projectpbd.domain.repository.SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
        observeOnboardingState()
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun updateConfirmPassword(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null) }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun observeAuthState() {
        viewModelScope.launch {
            repository.authState().collect { state ->
                _uiState.update { it.copy(authState = state, isLoading = state is AuthState.Loading) }
            }
        }
    }

    private fun observeOnboardingState() {
        viewModelScope.launch {
            settingsRepository.isOnboardingCompleted().collect { completed ->
                _uiState.update { it.copy(isOnboardingCompleted = completed) }
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
        }
    }

    fun login() {
        if (!validateLogin()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, snackbarMessage = null, toastMessage = null) }
            val result = repository.login(_uiState.value.email.trim(), _uiState.value.password)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authState = AuthState.Authenticated,
                            toastMessage = "Welcome back."
                        )
                    }
                },
                onFailure = { error ->
                    val message = mapAuthError(error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authState = AuthState.Error(message),
                            snackbarMessage = message,
                            toastMessage = message
                        )
                    }
                }
            )
        }
    }

    fun register() {
        if (!validateRegister()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, snackbarMessage = null, toastMessage = null) }
            val result = repository.register(
                name = _uiState.value.name.trim(),
                email = _uiState.value.email.trim(),
                password = _uiState.value.password
            )
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authState = AuthState.Authenticated,
                            toastMessage = "Account created."
                        )
                    }
                },
                onFailure = { error ->
                    val message = mapAuthError(error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authState = AuthState.Error(message),
                            snackbarMessage = message,
                            toastMessage = message
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, snackbarMessage = null) }
            val result = repository.logout()
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, authState = AuthState.Unauthenticated) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            authState = AuthState.Error(mapAuthError(error)),
                            snackbarMessage = mapAuthError(error)
                        )
                    }
                }
            )
        }
    }

    private fun validateLogin(): Boolean {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        var isValid = true

        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email is required.") }
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Enter a valid email.") }
            isValid = false
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password is required.") }
            isValid = false
        } else if (password.length < 8) {
            _uiState.update { it.copy(passwordError = "Use at least 8 characters.") }
            isValid = false
        }
        return isValid
    }

    private fun validateRegister(): Boolean {
        val name = _uiState.value.name.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword
        var isValid = true

        if (name.isEmpty()) {
            _uiState.update { it.copy(nameError = "Enter your name.") }
            isValid = false
        }
        if (email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email is required.") }
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(emailError = "Enter a valid email.") }
            isValid = false
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password is required.") }
            isValid = false
        } else if (password.length < 8) {
            _uiState.update { it.copy(passwordError = "Use at least 8 characters.") }
            isValid = false
        }
        if (confirmPassword.isBlank()) {
            _uiState.update { it.copy(confirmPasswordError = "Confirm your password.") }
            isValid = false
        } else if (password != confirmPassword) {
            _uiState.update { it.copy(confirmPasswordError = "Passwords do not match.") }
            isValid = false
        }
        return isValid
    }

    private fun mapAuthError(throwable: Throwable): String {
        return when (throwable) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
            is FirebaseAuthUserCollisionException -> "An account with this email already exists."
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Use at least 8 characters."
            else -> throwable.message ?: "Authentication failed. Please try again."
        }
    }
}
