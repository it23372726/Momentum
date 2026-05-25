package com.example.projectpbd.data.model

data class FinancialInsightEntity(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "NEUTRAL",
    val createdAt: Long = 0L,
    val severity: Int = 0
)
