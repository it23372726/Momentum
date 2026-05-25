package com.example.projectpbd.domain.repository

import kotlinx.coroutines.flow.Flow

interface ExchangeRateRepository {
    suspend fun getRate(from: String, to: String): Double
    suspend fun convert(amount: Double, from: String, to: String): Double
    suspend fun refreshRates()
    fun observeRates(): Flow<Map<String, Double>>
    suspend fun clearCache()
}
