package com.example.projectpbd.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.projectpbd.core.notification.NotificationHelper
import com.example.projectpbd.core.state.Resource
import com.example.projectpbd.core.util.RecurrenceCalculator
import com.example.projectpbd.domain.model.Expense
import com.example.projectpbd.domain.model.Income
import com.example.projectpbd.domain.model.RepeatConfiguration
import com.example.projectpbd.domain.repository.ExpenseRepository
import com.example.projectpbd.domain.repository.IncomeRepository
import com.example.projectpbd.domain.repository.WalletRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@HiltWorker
class RecurringTransactionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val walletRepository: WalletRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)
        val currentTime = System.currentTimeMillis()
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        processExpenses(notificationHelper, currentTime, today, tomorrow)
        processIncomes(notificationHelper, currentTime, today, tomorrow)

        return Result.success()
    }

    private suspend fun processExpenses(
        notificationHelper: NotificationHelper,
        currentTime: Long,
        today: LocalDate,
        tomorrow: LocalDate
    ) {
        val expensesResource = expenseRepository.observeExpenses().first()
        if (expensesResource is Resource.Success) {
            expensesResource.data.filter { it.repeatConfig.isEnabled && it.repeatConfig.isActive }.forEach { expense ->
                val nextDateMillis = expense.repeatConfig.nextExecutionDate
                val nextDate = Instant.ofEpochMilli(nextDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()

                if (nextDateMillis > 0 && nextDateMillis <= currentTime) {
                    generateExpenseOccurrence(expense)
                    notificationHelper.showNotification(
                        "Recurring Expense Processed",
                        "New expense generated for ${expense.notes.ifBlank { "Recurring Transaction" }}",
                        expense.id.hashCode()
                    )
                } else if (nextDate == today || nextDate == tomorrow) {
                    val msg = if (nextDate == today) "Due today" else "Due tomorrow"
                    notificationHelper.showNotification(
                        "Upcoming Expense",
                        "${expense.notes.ifBlank { "Recurring Expense" }} is $msg",
                        expense.id.hashCode() + 1
                    )
                }
            }
        }
    }

    private suspend fun generateExpenseOccurrence(expense: Expense) {
        val nextOccurrence = RecurrenceCalculator.calculateNextOccurrence(
            expense.repeatConfig,
            expense.repeatConfig.nextExecutionDate
        )

        val newOccurrence = expense.copy(
            id = UUID.randomUUID().toString(),
            date = expense.repeatConfig.nextExecutionDate,
            repeatConfig = RepeatConfiguration(),
            generated = true,
            createdAt = System.currentTimeMillis()
        )
        
        val res = expenseRepository.addExpense(newOccurrence)
        if (res is Resource.Success) {
            walletRepository.updateBalance(expense.walletId, expense.amount, false)
        }

        val updatedMaster = expense.copy(
            repeatConfig = expense.repeatConfig.copy(
                lastExecutionDate = expense.repeatConfig.nextExecutionDate,
                nextExecutionDate = nextOccurrence,
                isInitialOccurrenceExecuted = true
            )
        )
        expenseRepository.updateExpense(updatedMaster)
    }

    private suspend fun processIncomes(
        notificationHelper: NotificationHelper,
        currentTime: Long,
        today: LocalDate,
        tomorrow: LocalDate
    ) {
        val incomesResource = incomeRepository.observeIncomes().first()
        if (incomesResource is Resource.Success) {
            incomesResource.data.filter { it.repeatConfig.isEnabled && it.repeatConfig.isActive }.forEach { income ->
                val nextDateMillis = income.repeatConfig.nextExecutionDate
                val nextDate = Instant.ofEpochMilli(nextDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()

                if (nextDateMillis > 0 && nextDateMillis <= currentTime) {
                    generateIncomeOccurrence(income)
                    notificationHelper.showNotification(
                        "Recurring Income Processed",
                        "New income entry generated for ${income.notes.ifBlank { "Recurring Transaction" }}",
                        income.id.hashCode()
                    )
                } else if (nextDate == today || nextDate == tomorrow) {
                    val msg = if (nextDate == today) "Expected today" else "Expected tomorrow"
                    notificationHelper.showNotification(
                        "Upcoming Income",
                        "${income.notes.ifBlank { "Recurring Income" }} is $msg",
                        income.id.hashCode() + 1
                    )
                }
            }
        }
    }

    private suspend fun generateIncomeOccurrence(income: Income) {
        val nextOccurrence = RecurrenceCalculator.calculateNextOccurrence(
            income.repeatConfig,
            income.repeatConfig.nextExecutionDate
        )

        val newOccurrence = income.copy(
            id = UUID.randomUUID().toString(),
            date = income.repeatConfig.nextExecutionDate,
            repeatConfig = RepeatConfiguration(),
            createdAt = System.currentTimeMillis()
        )
        
        val res = incomeRepository.addIncome(newOccurrence)
        if (res is Resource.Success) {
            val amount = if (income.convertedAmountLkr > 0) income.convertedAmountLkr else income.amount
            walletRepository.updateBalance(income.walletId, amount, true)
        }

        val updatedMaster = income.copy(
            repeatConfig = income.repeatConfig.copy(
                lastExecutionDate = income.repeatConfig.nextExecutionDate,
                nextExecutionDate = nextOccurrence,
                isInitialOccurrenceExecuted = true
            )
        )
        incomeRepository.updateIncome(updatedMaster)
    }
}
