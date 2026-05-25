package com.example.projectpbd.domain.manager

import com.example.projectpbd.domain.model.Expense
import com.example.projectpbd.domain.model.Income
import com.example.projectpbd.domain.repository.WalletRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletReconciliationManager @Inject constructor(
    private val walletRepository: WalletRepository
) {
    /**
     * Wallet balances are stored in their own currency.
     * Transactions also store their original amount in that currency.
     * We use original [amount] for all reconciliation.
     */
    suspend fun reconcileExpenseUpdate(oldExpense: Expense, newExpense: Expense) {
        // 1. Undo old effect (Refund original amount)
        if (oldExpense.walletId.isNotBlank()) {
            walletRepository.updateBalance(oldExpense.walletId, oldExpense.amount, isAddition = true)
        }
        // 2. Apply new effect (Subtract new original amount)
        if (newExpense.walletId.isNotBlank()) {
            walletRepository.updateBalance(newExpense.walletId, newExpense.amount, isAddition = false)
        }
    }

    suspend fun reconcileIncomeUpdate(oldIncome: Income, newIncome: Income) {
        // 1. Undo old effect (Subtract original)
        if (oldIncome.walletId.isNotBlank()) {
            walletRepository.updateBalance(oldIncome.walletId, oldIncome.amount, isAddition = false)
        }
        // 2. Apply new effect (Add original)
        if (newIncome.walletId.isNotBlank()) {
            walletRepository.updateBalance(newIncome.walletId, newIncome.amount, isAddition = true)
        }
    }

    suspend fun reconcileExpenseDelete(expense: Expense) {
        if (expense.walletId.isNotBlank()) {
            walletRepository.updateBalance(expense.walletId, expense.amount, isAddition = true)
        }
    }

    suspend fun reconcileIncomeDelete(income: Income) {
        if (income.walletId.isNotBlank()) {
            walletRepository.updateBalance(income.walletId, income.amount, isAddition = false)
        }
    }
}
