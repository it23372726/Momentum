package com.example.projectpbd.data.model

data class WalletEntity(
    val id: String = "",
    val name: String = "",
    val type: String = "CASH",
    val currentBalance: Double = 0.0,
    val currency: String = "LKR",
    val iconKey: String? = null,
    val colorKey: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = 0L
)
