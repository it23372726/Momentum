package com.example.projectpbd.auth

import com.example.projectpbd.presentation.auth.state.AuthState
import com.example.projectpbd.presentation.auth.state.AuthUiState
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUiStateTest {
    @Test
    fun defaultState_isLoading() {
        val state = AuthUiState()
        assertTrue(state.authState is AuthState.Loading)
    }
}

