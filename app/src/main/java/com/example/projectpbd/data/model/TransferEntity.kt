package com.example.projectpbd.data.model

data class TransferEntity(
    val id: String = "",
    val sourceWalletId: String = "",
    val destinationWalletId: String = "",
    val amount: Double = 0.0,
    val notes: String = "",
    val date: Long = 0L,
    val sourceCurrency: String = "",
    val targetCurrency: String = "",
    val targetAmount: Double = 0.0,
    val exchangeRate: Double = 1.0,
    val createdAt: Long = 0L
)
