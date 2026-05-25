package com.example.projectpbd.domain.model

data class FinancialInsight(
    val id: String,
    val title: String,
    val description: String,
    val type: InsightType,
    val createdAt: Long,
    val severity: Int
)

