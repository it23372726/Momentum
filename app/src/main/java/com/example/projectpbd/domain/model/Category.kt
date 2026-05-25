package com.example.projectpbd.domain.model

data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val source: CategorySource,
    val iconKey: String? = null,
    val colorKey: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class CategoryType {
    EXPENSE, INCOME
}

enum class CategorySource {
    SYSTEM, USER
}
