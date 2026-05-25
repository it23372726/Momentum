package com.example.projectpbd.domain.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.Category
import com.example.projectpbd.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(type: CategoryType? = null): Flow<Resource<List<Category>>>
    suspend fun addCategory(category: Category): Resource<Unit>
    suspend fun updateCategory(category: Category): Resource<Unit>
    suspend fun deleteCategory(id: String): Resource<Unit>
}
