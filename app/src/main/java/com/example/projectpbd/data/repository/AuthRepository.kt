package com.example.projectpbd.data.repository

import com.example.projectpbd.presentation.auth.state.AuthState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun authState(): Flow<AuthState>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}

