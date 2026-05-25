package com.example.projectpbd.domain.model

data class AppSettings(
    val baseCurrency: String = "LKR",
    val defaultWalletId: String? = null,
    val lastExchangeRateUpdate: Long = 0L,
    val theme: AppTheme = AppTheme.SYSTEM
)

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}
