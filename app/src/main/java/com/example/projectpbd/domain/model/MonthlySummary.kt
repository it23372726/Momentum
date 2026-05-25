package com.example.projectpbd.domain.model

data class MonthlySummary(
    val month: String,
    val totalIncome: Double,
    val totalExpenses: Double,
    val discretionaryExpenses: Double,
    val committedExpenses: Double,
    val savingsAmount: Double,
    val savingsRate: Double,
    val topCategoryId: String
)

