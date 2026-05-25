package com.example.projectpbd.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.projectpbd.domain.model.AppSettings
import com.example.projectpbd.domain.model.AppTheme
import com.example.projectpbd.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val BASE_CURRENCY = stringPreferencesKey("base_currency")
        val DEFAULT_WALLET_ID = stringPreferencesKey("default_wallet_id")
        val LAST_EXCHANGE_UPDATE = longPreferencesKey("last_exchange_update")
        val THEME = stringPreferencesKey("theme")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    override fun getSettings(): Flow<AppSettings> {
        return context.dataStore.data.map { preferences ->
            AppSettings(
                baseCurrency = preferences[PreferencesKeys.BASE_CURRENCY] ?: "LKR",
                defaultWalletId = preferences[PreferencesKeys.DEFAULT_WALLET_ID],
                lastExchangeRateUpdate = preferences[PreferencesKeys.LAST_EXCHANGE_UPDATE] ?: 0L,
                theme = AppTheme.valueOf(preferences[PreferencesKeys.THEME] ?: AppTheme.SYSTEM.name)
            )
        }
    }

    override suspend fun updateBaseCurrency(currencyCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BASE_CURRENCY] = currencyCode
        }
    }

    override suspend fun updateDefaultWallet(walletId: String?) {
        context.dataStore.edit { preferences ->
            if (walletId == null) {
                preferences.remove(PreferencesKeys.DEFAULT_WALLET_ID)
            } else {
                preferences[PreferencesKeys.DEFAULT_WALLET_ID] = walletId
            }
        }
    }

    override suspend fun updateLastExchangeRateUpdate(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_EXCHANGE_UPDATE] = timestamp
        }
    }

    override suspend fun updateTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    override fun isOnboardingCompleted(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
}
