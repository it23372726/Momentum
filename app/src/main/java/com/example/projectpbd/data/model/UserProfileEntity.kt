package com.example.projectpbd.data.model

data class UserProfileEntity(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = 0L,
    val preferredCurrency: String = "LKR",
    val monthlyTargetSaving: Double = 0.0,
    val financialAwarenessScore: Int = 0,
    val savingsBalance: Double = 0.0
)

