package com.example.projectpbd.domain.repository

import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(): Flow<Resource<List<Expense>>>
    suspend fun addExpense(expense: Expense): Resource<Unit>
    suspend fun updateExpense(expense: Expense): Resource<Unit>
    suspend fun deleteExpense(id: String): Resource<Unit>
    suspend fun getExpense(id: String): Resource<Expense>
}

