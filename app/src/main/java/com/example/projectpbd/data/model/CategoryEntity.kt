package com.example.projectpbd.data.model

data class CategoryEntity(
    val id: String = "",
    val name: String = "",
    val type: String = "EXPENSE",
    val source: String = "USER",
    val iconKey: String? = null,
    val colorKey: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
