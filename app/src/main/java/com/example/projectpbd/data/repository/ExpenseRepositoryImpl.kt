package com.example.projectpbd.data.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.data.datasource.ExpenseRemoteDataSource
import com.example.projectpbd.data.datasource.UserSessionProvider
import com.example.projectpbd.data.model.toDomain
import com.example.projectpbd.data.model.toEntity
import com.example.projectpbd.domain.model.Expense
import com.example.projectpbd.domain.repository.ExpenseRepository
import com.example.projectpbd.domain.manager.WalletReconciliationManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val remote: ExpenseRemoteDataSource,
    private val sessionProvider: UserSessionProvider,
    private val reconciliationManager: WalletReconciliationManager
) : ExpenseRepository {
    override fun observeExpenses(): Flow<Resource<List<Expense>>> = flow {
        emit(Resource.Loading)
        val uid = sessionProvider.currentUserId()
        if (uid == null) {
            emit(Resource.Error("Sign in to view expenses."))
            return@flow
        }
        emitAll(
            remote.observeExpenses(uid)
                .map { items -> Resource.Success(items.map { it.toDomain() }) as Resource<List<Expense>> }
                .catch { error -> emit(Resource.Error(error.message ?: "Failed to load expenses.", error)) }
        )
    }

    override suspend fun addExpense(expense: Expense): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        remote.addExpense(uid, expense.toEntity())
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to add expense.", error)
    }

    override suspend fun updateExpense(expense: Expense): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        val oldExpense = remote.getExpense(uid, expense.id)?.toDomain()
        
        remote.updateExpense(uid, expense.toEntity())
        
        if (oldExpense != null) {
            reconciliationManager.reconcileExpenseUpdate(oldExpense, expense)
        }
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to update expense.", error)
    }

    override suspend fun deleteExpense(id: String): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        val oldExpense = remote.getExpense(uid, id)?.toDomain()
        
        remote.deleteExpense(uid, id)
        
        if (oldExpense != null) {
            reconciliationManager.reconcileExpenseDelete(oldExpense)
        }
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to delete expense.", error)
    }

    override suspend fun getExpense(id: String): Resource<Expense> = try {
        val uid = sessionProvider.requireUserId()
        val expense = remote.getExpense(uid, id)?.toDomain()
            ?: return Resource.Error("Expense not found.")
        Resource.Success(expense)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to load expense.", error)
    }
}
