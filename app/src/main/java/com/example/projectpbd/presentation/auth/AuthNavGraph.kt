package com.example.projectpbd.presentation.auth

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projectpbd.presentation.auth.screens.LoginScreen
import com.example.projectpbd.presentation.auth.screens.RegisterScreen
import com.example.projectpbd.presentation.auth.screens.SplashScreen
import com.example.projectpbd.presentation.dashboard.DashboardRoute
import com.example.projectpbd.presentation.expenses.AddExpenseRoute
import com.example.projectpbd.presentation.income.AddIncomeRoute
import com.example.projectpbd.presentation.goals.AddGoalRoute
import com.example.projectpbd.presentation.onboarding.OnboardingScreen
import com.example.projectpbd.presentation.goals.GoalHomeRoute
import com.example.projectpbd.presentation.goals.GoalDetailsRoute
import com.example.projectpbd.presentation.history.HistoryRoute
import com.example.projectpbd.presentation.analytics.AnalyticsRoute
import com.example.projectpbd.presentation.wallets.*
import com.example.projectpbd.presentation.wallets.state.WalletEvent
import com.example.projectpbd.presentation.main.MainScreen
import com.example.projectpbd.presentation.settings.*
import com.example.projectpbd.presentation.auth.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue

@Composable
fun MomentumNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = AuthRoutes.Splash) {
        composable(AuthRoutes.Splash) {
            SplashScreen(
                onAuthenticated = {
                    navController.navigate("main") {
                        popUpTo(AuthRoutes.Splash) { inclusive = true }
                    }
                },
                onUnauthenticated = {
                    navController.navigate(AuthRoutes.Login) {
                        popUpTo(AuthRoutes.Splash) { inclusive = true }
                    }
                },
                onShowOnboarding = {
                    navController.navigate(AuthRoutes.Onboarding) {
                        popUpTo(AuthRoutes.Splash) { inclusive = true }
                    }
                }
            )
        }
        composable(AuthRoutes.Onboarding) {
            val authViewModel: AuthViewModel = hiltViewModel()
            OnboardingScreen(
                onFinished = {
                    authViewModel.completeOnboarding()
                    navController.navigate("main") {
                        popUpTo(AuthRoutes.Onboarding) { inclusive = true }
                    }
                }
            )
        }
        composable(AuthRoutes.Login) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.uiState.collectAsStateWithLifecycle()

            LoginScreen(
                onRegister = { navController.navigate(AuthRoutes.Register) },
                onAuthenticated = {
                    if (authState.isOnboardingCompleted) {
                        navController.navigate("main") {
                            popUpTo(AuthRoutes.Login) { inclusive = true }
                        }
                    } else {
                        navController.navigate(AuthRoutes.Onboarding) {
                            popUpTo(AuthRoutes.Login) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(AuthRoutes.Register) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authState by authViewModel.uiState.collectAsStateWithLifecycle()

            RegisterScreen(
                onLogin = { navController.navigate(AuthRoutes.Login) },
                onAuthenticated = {
                    if (authState.isOnboardingCompleted) {
                        navController.navigate("main") {
                            popUpTo(AuthRoutes.Register) { inclusive = true }
                        }
                    } else {
                        navController.navigate(AuthRoutes.Onboarding) {
                            popUpTo(AuthRoutes.Register) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("main") {
            val childNavController = rememberNavController()
            MainScreen(
                navController = childNavController,
                onAddExpense = { navController.navigate(AuthRoutes.AddExpense) },
                onAddIncome = { navController.navigate(AuthRoutes.AddIncome) }
            ) { paddingValues ->
                NavHost(
                    navController = childNavController,
                    startDestination = AuthRoutes.Dashboard,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(AuthRoutes.Dashboard) {
                        DashboardRoute(
                            navController = childNavController,
                            onNavigateToHistory = { childNavController.navigate(AuthRoutes.History) },
                            onEditActivity = { id, isIncome ->
                                val route = if (isIncome) AuthRoutes.addIncome(id) else AuthRoutes.addExpense(id)
                                navController.navigate(route)
                            },
                            onSettingsClick = { childNavController.navigate(AuthRoutes.Settings) }
                        )
                    }
                    composable(AuthRoutes.History) {
                        HistoryRoute(
                            onBack = { childNavController.popBackStack() },
                            onEditActivity = { id, isIncome ->
                                val route = if (isIncome) AuthRoutes.addIncome(id) else AuthRoutes.addExpense(id)
                                navController.navigate(route)
                            },
                            onEditRecurringExpense = { id -> navController.navigate(AuthRoutes.addExpense(id)) },
                            onEditRecurringIncome = { id -> navController.navigate(AuthRoutes.addIncome(id)) }
                        )
                    }
                    composable(AuthRoutes.Analytics) {
                        AnalyticsRoute(
                            onBack = { childNavController.popBackStack() }
                        )
                    }
                    composable(AuthRoutes.GoalDetails) {
                        GoalHomeRoute(
                            onAddGoal = { navController.navigate(AuthRoutes.AddGoal) },
                            onGoalClick = { goalId -> childNavController.navigate("goal_vault/$goalId") },
                            onNavigateToHistory = { childNavController.navigate(AuthRoutes.History) }
                        )
                    }
                    composable("goal_vault/{goalId}") { backStackEntry ->
                        val goalId = backStackEntry.arguments?.getString("goalId") ?: return@composable
                        GoalDetailsRoute(
                            goalId = goalId,
                            onBack = { childNavController.popBackStack() },
                            onEdit = { id -> navController.navigate(AuthRoutes.addGoal(id)) }
                        )
                    }
                    composable(AuthRoutes.Wallets) {
                        val walletViewModel: WalletViewModel = hiltViewModel()
                        val walletState by walletViewModel.uiState.collectAsStateWithLifecycle()
                        WalletsScreen(
                            state = walletState,
                            onEvent = walletViewModel::onEvent,
                            onAddWallet = { childNavController.navigate(AuthRoutes.AddWallet) },
                            onTransfer = { childNavController.navigate(AuthRoutes.Transfer) },
                            onWalletClick = { id -> childNavController.navigate(AuthRoutes.addWallet(id)) }
                        )
                    }
                    composable(
                        route = AuthRoutes.AddWallet + "?id={id}",
                        arguments = listOf(navArgument("id") { nullable = true; defaultValue = null })
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id")
                        val walletViewModel: WalletViewModel = hiltViewModel()
                        
                        LaunchedEffect(id) {
                            if (id != null) {
                                walletViewModel.onEvent(WalletEvent.LoadWallet(id))
                            }
                        }

                        val addWalletState by walletViewModel.uiState.collectAsStateWithLifecycle()
                        AddWalletScreen(
                            state = addWalletState,
                            onEvent = walletViewModel::onEvent,
                            onBack = { childNavController.popBackStack() }
                        )
                    }
                    composable(AuthRoutes.Transfer) {
                        val transferViewModel: TransferViewModel = hiltViewModel()
                        val transferState by transferViewModel.uiState.collectAsStateWithLifecycle()
                        TransferScreen(
                            state = transferState,
                            onEvent = transferViewModel::onEvent,
                            onBack = { childNavController.popBackStack() }
                        )
                    }
                    composable(AuthRoutes.Settings) {
                        val settingsViewModel: SettingsViewModel = hiltViewModel()
                        val state by settingsViewModel.uiState.collectAsStateWithLifecycle()
                        SettingsScreen(
                            state = state,
                            onBack = { childNavController.popBackStack() },
                            onUpdateBaseCurrency = settingsViewModel::updateBaseCurrency,
                            onUpdateDefaultWallet = settingsViewModel::updateDefaultWallet,
                            onRefreshRates = settingsViewModel::refreshExchangeRates,
                            onManageCategories = { childNavController.navigate(AuthRoutes.CategoryManagement) },
                            onClearCache = settingsViewModel::clearCache,
                            onLogout = {
                                settingsViewModel.logout {
                                    navController.navigate(AuthRoutes.Login) {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            },
                            onClearMessages = settingsViewModel::clearMessages
                        )
                    }
                    composable(AuthRoutes.CategoryManagement) {
                        val viewModel: CategoryManagementViewModel = hiltViewModel()
                        val state by viewModel.state.collectAsStateWithLifecycle()
                        CategoryManagementScreen(
                            state = state,
                            onBack = { childNavController.popBackStack() },
                            onTypeSelected = viewModel::setType,
                            onAddCategory = viewModel::addCategory,
                            onUpdateCategory = viewModel::updateCategory,
                            onDeleteCategory = viewModel::deleteCategory,
                            onClearMessages = viewModel::clearMessages
                        )
                    }
                }
            }
        }

        composable(
            route = AuthRoutes.AddExpense + "?id={id}",
            arguments = listOf(navArgument("id") { nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            AddExpenseRoute(
                expenseId = id,
                onBack = { navController.popBackStack() },
                onExpenseSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = AuthRoutes.AddIncome + "?id={id}",
            arguments = listOf(navArgument("id") { nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            AddIncomeRoute(
                incomeId = id,
                onBack = { navController.popBackStack() },
                onIncomeSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = AuthRoutes.AddGoal + "?id={id}",
            arguments = listOf(navArgument("id") { nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            AddGoalRoute(
                goalId = id,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}
