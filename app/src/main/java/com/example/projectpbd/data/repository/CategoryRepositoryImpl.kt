package com.example.projectpbd.data.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.data.datasource.CategoryRemoteDataSource
import com.example.projectpbd.data.datasource.UserSessionProvider
import com.example.projectpbd.data.model.toDomain
import com.example.projectpbd.data.model.toEntity
import com.example.projectpbd.domain.model.Category
import com.example.projectpbd.domain.model.CategorySource
import com.example.projectpbd.domain.model.CategoryType
import com.example.projectpbd.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val remoteDataSource: CategoryRemoteDataSource,
    private val sessionProvider: UserSessionProvider
) : CategoryRepository {

    override fun observeCategories(type: CategoryType?): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Loading)
        val uid = sessionProvider.currentUserId()
        if (uid == null) {
            emit(Resource.Error("Sign in to view categories."))
            return@flow
        }
        emitAll(
            remoteDataSource.observeCategories(uid)
                .map { entities ->
                    val userCategories = entities.map { it.toDomain() }
                    val allCategories = getDefaultCategories() + userCategories
                    val filtered = if (type != null) {
                        allCategories.filter { it.type == type }
                    } else {
                        allCategories
                    }
                    Resource.Success(filtered.sortedBy { it.name }) as Resource<List<Category>>
                }
                .catch { error -> emit(Resource.Error(error.message ?: "Failed to load categories.", error)) }
        )
    }

    override suspend fun addCategory(category: Category): Resource<Unit> {
        return try {
            val uid = sessionProvider.requireUserId()
            remoteDataSource.addCategory(uid, category.toEntity())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add category")
        }
    }

    override suspend fun updateCategory(category: Category): Resource<Unit> {
        return try {
            val uid = sessionProvider.requireUserId()
            remoteDataSource.addCategory(uid, category.toEntity())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update category")
        }
    }

    override suspend fun deleteCategory(id: String): Resource<Unit> {
        val systemIds = getDefaultCategories().map { it.id }
        if (id in systemIds) {
            return Resource.Error("Cannot delete system default categories.")
        }
        return try {
            val uid = sessionProvider.requireUserId()
            remoteDataSource.deleteCategory(uid, id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete category")
        }
    }

    private fun getDefaultCategories(): List<Category> {
        return listOf(
            // Expense Defaults
            Category("survival", "Survival", CategoryType.EXPENSE, CategorySource.SYSTEM, isDefault = true),
            Category("convenience", "Convenience", CategoryType.EXPENSE, CategorySource.SYSTEM, isDefault = true),
            Category("impulse", "Impulse", CategoryType.EXPENSE, CategorySource.SYSTEM, isDefault = true),
            Category("lifestyle", "Lifestyle", CategoryType.EXPENSE, CategorySource.SYSTEM, isDefault = true),
            Category("growth", "Growth", CategoryType.EXPENSE, CategorySource.SYSTEM, isDefault = true),
            Category("food", "Food", CategoryType.EXPENSE, CategorySource.SYSTEM, isDefault = true),
            Category("transport", "Transport", CategoryType.EXPENSE, CategorySource.SYSTEM, isDefault = true),
            Category("utilities", "Utilities", CategoryType.EXPENSE, CategorySource.SYSTEM, isDefault = true),
            Category("subscriptions", "Subscriptions", CategoryType.EXPENSE, CategorySource.SYSTEM, isDefault = true),
            
            // Income Defaults
            Category("salary", "Salary", CategoryType.INCOME, CategorySource.SYSTEM, isDefault = true),
            Category("freelance", "Freelance", CategoryType.INCOME, CategorySource.SYSTEM, isDefault = true),
            Category("adsense", "AdSense", CategoryType.INCOME, CategorySource.SYSTEM, isDefault = true),
            Category("crypto", "Crypto", CategoryType.INCOME, CategorySource.SYSTEM, isDefault = true),
            Category("other", "Other", CategoryType.INCOME, CategorySource.SYSTEM, isDefault = true)
        )
    }
}
