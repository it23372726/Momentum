package com.example.projectpbd.domain.model

data class Transfer(
    val id: String,
    val sourceWalletId: String,
    val destinationWalletId: String,
    val amount: Double,
    val notes: String,
    val date: Long,
    val sourceCurrency: String = "",
    val targetCurrency: String = "",
    val targetAmount: Double = 0.0,
    val exchangeRate: Double = 1.0,
    val createdAt: Long = System.currentTimeMillis()
)
