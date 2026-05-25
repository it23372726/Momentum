package com.example.projectpbd.data.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.data.datasource.InsightRemoteDataSource
import com.example.projectpbd.data.datasource.UserSessionProvider
import com.example.projectpbd.data.model.toDomain
import com.example.projectpbd.data.model.toEntity
import com.example.projectpbd.domain.model.FinancialInsight
import com.example.projectpbd.domain.repository.InsightRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Singleton
class InsightRepositoryImpl @Inject constructor(
    private val remote: InsightRemoteDataSource,
    private val sessionProvider: UserSessionProvider
) : InsightRepository {
    override fun observeInsights(): Flow<Resource<List<FinancialInsight>>> {
        val uid = sessionProvider.requireUserId()
        return remote.observeInsights(uid)
            .map { items -> Resource.Success(items.map { it.toDomain() }) as Resource<List<FinancialInsight>> }
            .onStart { emit(Resource.Loading) }
            .catch { error -> emit(Resource.Error(error.message ?: "Failed to load insights.", error)) }
    }

    override suspend fun addInsight(insight: FinancialInsight): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        remote.addInsight(uid, insight.toEntity())
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to add insight.", error)
    }

    override suspend fun deleteInsight(id: String): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        remote.deleteInsight(uid, id)
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to delete insight.", error)
    }
}
