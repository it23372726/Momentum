package com.example.projectpbd.domain.model

data class Income(
    val id: String,
    val amount: Double,
    val currency: String,
    val categoryId: String,
    val date: Long,
    val notes: String,
    val walletId: String = "",
    val exchangeRate: Double,
    val convertedAmountLkr: Double,
    val repeatConfig: RepeatConfiguration = RepeatConfiguration(),
    val createdAt: Long
)

