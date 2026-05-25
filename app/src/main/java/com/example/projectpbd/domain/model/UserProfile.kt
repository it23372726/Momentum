package com.example.projectpbd.domain.model

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val createdAt: Long,
    val preferredCurrency: String = "LKR",
    val monthlyTargetSaving: Double,
    val financialAwarenessScore: Int,
    val savingsBalance: Double
)

