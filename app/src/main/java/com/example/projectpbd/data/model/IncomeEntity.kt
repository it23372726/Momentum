package com.example.projectpbd.data.model

data class IncomeEntity(
    val id: String = "",
    val amount: Double = 0.0,
    val currency: String = "LKR",
    val categoryId: String = "other",
    val date: Long = 0L,
    val notes: String = "",
    val walletId: String = "",
    val exchangeRate: Double = 1.0,
    val convertedAmountLkr: Double = 0.0,
    val repeatFrequency: String = "NONE",
    val repeatInterval: Int = 1,
    val repeatStartDate: Long = 0L,
    val repeatNextExecutionDate: Long = 0L,
    val repeatLastExecutionDate: Long = 0L,
    val repeatIsActive: Boolean = true,
    val repeatIsInitialExecuted: Boolean = false,
    val createdAt: Long = 0L
)

