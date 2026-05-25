package com.example.projectpbd.data.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.data.datasource.IncomeRemoteDataSource
import com.example.projectpbd.data.datasource.UserSessionProvider
import com.example.projectpbd.data.model.toDomain
import com.example.projectpbd.data.model.toEntity
import com.example.projectpbd.domain.model.Income
import com.example.projectpbd.domain.repository.IncomeRepository
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
class IncomeRepositoryImpl @Inject constructor(
    private val remote: IncomeRemoteDataSource,
    private val sessionProvider: UserSessionProvider,
    private val reconciliationManager: WalletReconciliationManager
) : IncomeRepository {
    override fun observeIncomes(): Flow<Resource<List<Income>>> = flow {
        emit(Resource.Loading)
        val uid = sessionProvider.currentUserId()
        if (uid == null) {
            emit(Resource.Error("Sign in to view income."))
            return@flow
        }
        emitAll(
            remote.observeIncomes(uid)
                .map { items -> Resource.Success(items.map { it.toDomain() }) as Resource<List<Income>> }
                .catch { error -> emit(Resource.Error(error.message ?: "Failed to load incomes.", error)) }
        )
    }

    override suspend fun addIncome(income: Income): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        remote.addIncome(uid, income.toEntity())
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to add income.", error)
    }

    override suspend fun updateIncome(income: Income): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        val oldIncome = remote.getIncome(uid, income.id)?.toDomain()
        
        remote.updateIncome(uid, income.toEntity())
        
        if (oldIncome != null) {
            reconciliationManager.reconcileIncomeUpdate(oldIncome, income)
        }
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to update income.", error)
    }

    override suspend fun deleteIncome(id: String): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        val oldIncome = remote.getIncome(uid, id)?.toDomain()
        
        remote.deleteIncome(uid, id)
        
        if (oldIncome != null) {
            reconciliationManager.reconcileIncomeDelete(oldIncome)
        }
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to delete income.", error)
    }

    override suspend fun getIncome(id: String): Resource<Income> = try {
        val uid = sessionProvider.requireUserId()
        val income = remote.getIncome(uid, id)?.toDomain()
            ?: return Resource.Error("Income not found.")
        Resource.Success(income)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to load income.", error)
    }
}
