package com.example.projectpbd.domain.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeGoals(): Flow<Resource<List<SavingsGoal>>>
    suspend fun addGoal(goal: SavingsGoal): Resource<Unit>
    suspend fun updateGoal(goal: SavingsGoal): Resource<Unit>
    suspend fun deleteGoal(id: String): Resource<Unit>
    suspend fun getGoal(id: String): Resource<SavingsGoal>
}

