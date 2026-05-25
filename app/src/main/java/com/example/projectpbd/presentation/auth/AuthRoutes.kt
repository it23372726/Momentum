package com.example.projectpbd.presentation.auth

object AuthRoutes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Login = "login"
    const val Register = "register"
    const val Dashboard = "dashboard"
    const val AddExpense = "add_expense"
    const val AddIncome = "add_income"
    const val GoalDetails = "goal_details"
    const val GoalHistory = "goal_history"
    const val AddGoal = "add_goal"
    const val History = "history"
    const val Analytics = "analytics"
    const val Wallets = "wallets"
    const val AddWallet = "add_wallet"
    const val Transfer = "transfer"
    const val Settings = "settings"
    const val CategoryManagement = "category_management"

    fun addGoal(id: String? = null): String {
        return if (id != null) "$AddGoal?id=$id" else AddGoal
    }

    fun addWallet(id: String? = null): String {
        return if (id != null) "$AddWallet?id=$id" else AddWallet
    }

    fun addExpense(id: String? = null): String {
        return if (id != null) "$AddExpense?id=$id" else AddExpense
    }

    fun addIncome(id: String? = null): String {
        return if (id != null) "$AddIncome?id=$id" else AddIncome
    }
}
