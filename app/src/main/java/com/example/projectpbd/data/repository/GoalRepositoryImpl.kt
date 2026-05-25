package com.example.projectpbd.data.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.data.datasource.GoalRemoteDataSource
import com.example.projectpbd.data.datasource.UserSessionProvider
import com.example.projectpbd.data.model.toDomain
import com.example.projectpbd.data.model.toEntity
import com.example.projectpbd.domain.model.SavingsGoal
import com.example.projectpbd.domain.repository.GoalRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val remote: GoalRemoteDataSource,
    private val sessionProvider: UserSessionProvider
) : GoalRepository {
    override fun observeGoals(): Flow<Resource<List<SavingsGoal>>> = flow {
        emit(Resource.Loading)
        val uid = sessionProvider.currentUserId()
        if (uid == null) {
            emit(Resource.Error("Sign in to view goals."))
            return@flow
        }
        emitAll(
            remote.observeGoals(uid)
                .map { items -> Resource.Success(items.map { it.toDomain() }) as Resource<List<SavingsGoal>> }
                .catch { error -> emit(Resource.Error(error.message ?: "Failed to load goals.", error)) }
        )
    }

    override suspend fun addGoal(goal: SavingsGoal): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        remote.addGoal(uid, goal.toEntity())
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to add goal.", error)
    }

    override suspend fun updateGoal(goal: SavingsGoal): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        remote.updateGoal(uid, goal.toEntity())
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to update goal.", error)
    }

    override suspend fun deleteGoal(id: String): Resource<Unit> = try {
        val uid = sessionProvider.requireUserId()
        remote.deleteGoal(uid, id)
        Resource.Success(Unit)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to delete goal.", error)
    }

    override suspend fun getGoal(id: String): Resource<SavingsGoal> = try {
        val uid = sessionProvider.requireUserId()
        val goal = remote.getGoal(uid, id)?.toDomain()
            ?: return Resource.Error("Goal not found.")
        Resource.Success(goal)
    } catch (error: Exception) {
        Resource.Error(error.message ?: "Failed to load goal.", error)
    }
}
