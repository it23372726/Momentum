package com.example.projectpbd.domain.model

data class SavingsGoal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val walletId: String, // Dedicated wallet (Vault)
    val categoryId: String = "miscellaneous",
    val targetDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val description: String = "",
    val status: GoalStatus = GoalStatus.ACTIVE,
    val completedAt: Long? = null
)
