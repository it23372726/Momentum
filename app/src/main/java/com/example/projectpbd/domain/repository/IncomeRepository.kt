package com.example.projectpbd.domain.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.Income
import kotlinx.coroutines.flow.Flow

interface IncomeRepository {
    fun observeIncomes(): Flow<Resource<List<Income>>>
    suspend fun addIncome(income: Income): Resource<Unit>
    suspend fun updateIncome(income: Income): Resource<Unit>
    suspend fun deleteIncome(id: String): Resource<Unit>
    suspend fun getIncome(id: String): Resource<Income>
}

