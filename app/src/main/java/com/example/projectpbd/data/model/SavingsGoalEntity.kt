package com.example.projectpbd.data.model

data class SavingsGoalEntity(
    val id: String = "",
    val title: String = "",
    val targetAmount: Double = 0.0,
    val walletId: String = "",
    val categoryId: String = "miscellaneous",
    val targetDate: Long = 0L,
    val createdAt: Long = 0L,
    val description: String = "",
    val status: String = "ACTIVE",
    val completedAt: Long? = null
)
