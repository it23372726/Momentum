package com.example.projectpbd.domain.model

enum class WalletType {
    CASH,
    BANK,
    DIGITAL,
    CRYPTO,
    SAVINGS,
    CUSTOM
}

data class Wallet(
    val id: String,
    val name: String,
    val type: WalletType,
    val currentBalance: Double,
    val currency: String = "LKR",
    val iconKey: String? = null,
    val colorKey: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
