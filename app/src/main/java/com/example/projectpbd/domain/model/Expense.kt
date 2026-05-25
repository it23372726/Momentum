package com.example.projectpbd.domain.model

data class Expense(
    val id: String,
    val amount: Double,
    val currency: String = "LKR",
    val categoryId: String,
    val paymentMethod: PaymentMethod,
    val date: Long,
    val notes: String,
    val walletId: String = "",
    val exchangeRate: Double = 1.0,
    val convertedAmountLkr: Double = 0.0,
    val repeatConfig: RepeatConfiguration = RepeatConfiguration(),
    val isDiscretionary: Boolean,
    val createdAt: Long,
    val transactionType: TransactionType = TransactionType.REGULAR,
    val goalId: String? = null,
    val generated: Boolean = false
)
