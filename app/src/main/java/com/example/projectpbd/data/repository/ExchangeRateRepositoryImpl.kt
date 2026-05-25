package com.example.projectpbd.data.repository

import android.content.Context
import android.util.Log
import com.example.projectpbd.data.remote.ExchangeRateService
import com.example.projectpbd.domain.repository.ExchangeRateRepository
import com.example.projectpbd.domain.repository.SettingsRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class ExchangeRateRepositoryImpl @Inject constructor(
    private val service: ExchangeRateService,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ExchangeRateRepository {

    private val TAG = "EXCHANGE_DEBUG"
    private val prefs = context.getSharedPreferences("exchange_rates", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _rates = MutableStateFlow<Map<String, Double>>(emptyMap())
    private val refreshMutex = Mutex()

    init {
        val cached = loadCachedRates()
        if (cached.isNotEmpty()) {
            _rates.value = cached
            Log.d(TAG, "Initial rates loaded from local cache: ${cached.size} currencies")
        }
    }

    override suspend fun getRate(from: String, to: String): Double {
        return try {
            if (from == to) return 1.0
            
            var currentRates = _rates.value
            
            if (currentRates.isEmpty()) {
                Log.d(TAG, "Rates are empty in memory, attempting immediate refresh for $from to $to")
                try {
                    refreshRates()
                    currentRates = _rates.value
                } catch (e: Exception) {
                    Log.e(TAG, "Refresh failed during getRate, using hardcoded fallback", e)
                    return getHardcodedFallback(from, to)
                }
            }

            val rateToLkrFrom = if (from == "LKR") 1.0 else (currentRates[from] ?: 0.0)
            val rateToLkrTo = if (to == "LKR") 1.0 else (currentRates[to] ?: 0.0)
            
            if (rateToLkrFrom <= 0.0 || rateToLkrTo <= 0.0) {
                Log.w(TAG, "Rate missing or invalid for $from (rate=$rateToLkrFrom) or $to (rate=$rateToLkrTo). Using fallback.")
                return getHardcodedFallback(from, to)
            }
            
            // Logic: Base is LKR. 
            // 1 LKR = rateToLkrFrom [FROM]
            // 1 LKR = rateToLkrTo [TO]
            // Therefore, [TO] = (rateToLkrTo / rateToLkrFrom) * [FROM]
            val finalRate = rateToLkrTo / rateToLkrFrom
            Log.d(TAG, "Conversion check: 1 $from = $finalRate $to (Rate source: API/Cache)")
            
            finalRate
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in getRate for $from to $to", e)
            getHardcodedFallback(from, to)
        }
    }

    override suspend fun convert(amount: Double, from: String, to: String): Double {
        return try {
            val rate = getRate(from, to)
            val converted = amount * rate
            Log.d(TAG, "Conversion Result: $amount $from -> $converted $to (Rate: $rate)")
            converted
        } catch (e: Exception) {
            Log.e(TAG, "Conversion failed in convert()", e)
            amount
        }
    }

    override suspend fun refreshRates() {
        refreshMutex.withLock {
            try {
                Log.d(TAG, "Fetching latest rates from API for LKR base...")
                val response = service.getLatestRates(base = "LKR")
                
                Log.d(TAG, "API Response: result=${response.result}, base_code=${response.baseCode}, rates_count=${response.rates?.size ?: 0}")
                
                val apiRates = response.rates
                if (response.result == "success" && apiRates != null) {
                    _rates.value = apiRates
                    cacheRates(apiRates)
                    val now = System.currentTimeMillis()
                    settingsRepository.updateLastExchangeRateUpdate(now)
                    Log.d(TAG, "Rates refreshed successfully: ${apiRates.size} currencies processed.")
                    Log.d(TAG, "Live Rate Check: 1 LKR = ${apiRates["USD"]} USD")
                } else {
                    Log.e(TAG, "API failed. Result: ${response.result}, Rates Null: ${apiRates == null}")
                    useLocalCacheIfEmpty()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network or Parsing error during refreshRates()", e)
                useLocalCacheIfEmpty()
            }
        }
    }

    private fun useLocalCacheIfEmpty() {
        if (_rates.value.isEmpty()) {
            val cached = loadCachedRates()
            if (cached.isNotEmpty()) {
                Log.d(TAG, "Falling back to local cache: ${cached.size} rates restored.")
                _rates.value = cached
            } else {
                Log.w(TAG, "No local cache available for fallback.")
            }
        }
    }

    override fun observeRates(): Flow<Map<String, Double>> = _rates.asStateFlow()

    override suspend fun clearCache() {
        refreshMutex.withLock {
            _rates.value = emptyMap()
            prefs.edit().remove("latest_rates").apply()
            settingsRepository.updateLastExchangeRateUpdate(0L)
            Log.d(TAG, "Exchange rate cache cleared.")
        }
    }

    private fun cacheRates(rates: Map<String, Double>) {
        try {
            val json = gson.toJson(rates)
            prefs.edit().putString("latest_rates", json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write rates to SharedPreferences", e)
        }
    }

    private fun loadCachedRates(): Map<String, Double> {
        val json = prefs.getString("latest_rates", null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            gson.fromJson<Map<String, Double>>(json, type) ?: emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse cached rates from JSON", e)
            emptyMap()
        }
    }

    private fun getHardcodedFallback(from: String, to: String): Double {
        Log.d(TAG, "Using HARDCODED Fallback for $from -> $to")
        // 1 LKR = X target
        val fallbacks = mapOf(
            "USD" to 0.0033,
            "EUR" to 0.0031,
            "GBP" to 0.0026,
            "INR" to 0.28,
            "AED" to 0.012
        )
        
        val fromRate = if (from == "LKR") 1.0 else (fallbacks[from] ?: 1.0)
        val toRate = if (to == "LKR") 1.0 else (fallbacks[to] ?: 1.0)
        
        val rate = toRate / fromRate
        Log.d(TAG, "Fallback Rate: $rate")
        return rate
    }
}
