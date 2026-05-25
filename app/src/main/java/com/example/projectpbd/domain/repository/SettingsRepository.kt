package com.example.projectpbd.domain.repository

import com.example.projectpbd.domain.model.AppSettings
import com.example.projectpbd.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateBaseCurrency(currencyCode: String)
    suspend fun updateDefaultWallet(walletId: String?)
    suspend fun updateLastExchangeRateUpdate(timestamp: Long)
    suspend fun updateTheme(theme: AppTheme)
    
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
}
