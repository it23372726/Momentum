package com.example.projectpbd.core.util

import com.example.projectpbd.domain.model.CurrencyRegistry
import java.text.NumberFormat
import java.util.*

object CurrencyFormatter {
    fun format(amount: Double, currencyCode: String): String {
        val currencyInfo = CurrencyRegistry.getByCode(currencyCode)
        
        val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = if (amount % 1 == 0.0) 0 else 2
            maximumFractionDigits = 2
        }
        
        val formattedNumber = numberFormat.format(Math.abs(amount))
        return "${currencyInfo.symbol} $formattedNumber"
    }

    fun formatWithSign(amount: Double, currencyCode: String, isIncome: Boolean? = null): String {
        val sign = when {
            isIncome == true -> "+"
            isIncome == false -> "-"
            amount > 0 -> "+"
            amount < 0 -> "-"
            else -> ""
        }
        val formatted = format(amount, currencyCode)
        return if (sign.isNotEmpty()) "$sign $formatted" else formatted
    }

    fun formatLkr(amount: Double): String {
        return format(amount, "LKR")
    }

    fun formatCompact(amount: Double, currencyCode: String): String {
        val currencyInfo = CurrencyRegistry.getByCode(currencyCode)
        val absAmount = Math.abs(amount)
        val value = when {
            absAmount >= 1_000_000 -> "${"%.1f".format(absAmount / 1_000_000)}M"
            absAmount >= 1_000 -> "${"%.1f".format(absAmount / 1_000)}K"
            else -> "%,.0f".format(absAmount)
        }
        val sign = if (amount < 0) "-" else ""
        return "$sign${currencyInfo.symbol} $value"
    }
}
