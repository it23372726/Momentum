package com.example.projectpbd.domain.manager

import com.example.projectpbd.domain.model.Transfer
import com.example.projectpbd.domain.repository.ExchangeRateRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.round

@Singleton
class CurrencyConversionManager @Inject constructor(
    private val exchangeRateRepository: ExchangeRateRepository
) {
    suspend fun convert(amount: Double, from: String, to: String): Double {
        if (from == to) return amount
        return exchangeRateRepository.convert(amount, from, to)
    }

    suspend fun getRate(from: String, to: String): Double {
        return exchangeRateRepository.getRate(from, to)
    }

    suspend fun enrichTransfer(transfer: Transfer, sourceCurrency: String, targetCurrency: String): Transfer {
        val rate = getRate(sourceCurrency, targetCurrency)
        val targetAmount = roundAmount(transfer.amount * rate)
        return transfer.copy(
            sourceCurrency = sourceCurrency,
            targetCurrency = targetCurrency,
            targetAmount = targetAmount,
            exchangeRate = rate
        )
    }

    /**
     * Formats an amount to 2 decimal places for financial accuracy.
     */
    fun roundAmount(amount: Double): Double {
        return round(amount * 100.0) / 100.0
    }
}
