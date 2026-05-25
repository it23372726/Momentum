package com.example.projectpbd.domain.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.FinancialInsight
import kotlinx.coroutines.flow.Flow

interface InsightRepository {
    fun observeInsights(): Flow<Resource<List<FinancialInsight>>>
    suspend fun addInsight(insight: FinancialInsight): Resource<Unit>
    suspend fun deleteInsight(id: String): Resource<Unit>
}

