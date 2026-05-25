package com.example.projectpbd.data.model

data class MonthlySummaryEntity(
    val month: String = "",
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val discretionaryExpenses: Double = 0.0,
    val committedExpenses: Double = 0.0,
    val savingsAmount: Double = 0.0,
    val savingsRate: Double = 0.0,
    val topCategoryId: String = "survival"
)

