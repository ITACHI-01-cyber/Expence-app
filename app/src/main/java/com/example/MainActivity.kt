package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.SavingsGoalEntity
import com.example.model.TransactionEntity
import com.example.model.WalletEntity
import com.example.ui.components.AddEditGoalDialog
import com.example.ui.components.AddEditTransactionDialog
import com.example.ui.components.AddEditWalletDialog
import com.example.ui.components.BottomNavBar
import com.example.ui.components.EditBudgetDialog
import com.example.ui.components.TopBar
import com.example.ui.components.TopUpBalanceDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BudgetPlannerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.ExpenceTrackTheme
import com.example.viewmodel.ExpenseViewModel
import com.example.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            ExpenceTrackApp()
        }
    }
}

@Composable
fun ExpenceTrackApp(viewModel: ExpenseViewModel = viewModel()) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val wallets by viewModel.wallets.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val dashboardSummary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val categoryExpenses by viewModel.categoryExpenses.collectAsStateWithLifecycle()
    val dailyExpenses by viewModel.dailyExpenses.collectAsStateWithLifecycle()
    val budgetPlannerStats by viewModel.budgetPlannerStats.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val apiTestResults by viewModel.apiTestResults.collectAsStateWithLifecycle()
    val isRunningApiTests by viewModel.isRunningApiTests.collectAsStateWithLifecycle()

    val currencySymbol = userSettings?.currency ?: "₹"
    val accentColor = userSettings?.accentColor ?: "purple"

    val isDark = when (userSettings?.theme?.lowercase()) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    // Modal dialog states
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var selectedTransactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    var showAddWalletDialog by remember { mutableStateOf(false) }
    var topUpTargetWallet by remember { mutableStateOf<WalletEntity?>(null) }

    var showEditBudgetDialog by remember { mutableStateOf(false) }

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedGoalToEdit by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    ExpenceTrackTheme(darkTheme = isDark, accentColorName = accentColor) {
        if (!isAuthenticated) {
            AuthScreen(viewModel = viewModel)
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    TopBar(
                        userSettings = userSettings,
                        isSyncing = isSyncing,
                        onSyncClick = { viewModel.syncAllData() },
                        onAddTransactionClick = { showAddTransactionDialog = true },
                        onProfileClick = { viewModel.setTab(ScreenTab.SETTINGS) }
                    )
                },
                bottomBar = {
                    BottomNavBar(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                },
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Crossfade(targetState = currentTab, label = "screen_crossfade") { tab ->
                        when (tab) {
                            ScreenTab.DASHBOARD -> DashboardScreen(
                                summary = dashboardSummary,
                                wallets = wallets,
                                recentTransactions = allTransactions,
                                categoryExpenses = categoryExpenses,
                                dailyExpenses = dailyExpenses,
                                goals = goals,
                                currencySymbol = currencySymbol,
                                onAddCard = { showAddWalletDialog = true },
                                onTopUpCard = { topUpTargetWallet = it },
                                onEditBudget = { showEditBudgetDialog = true },
                                onViewAllTransactions = { viewModel.setTab(ScreenTab.TRANSACTIONS) },
                                onTransactionClick = { selectedTransactionToEdit = it },
                                onAddGoal = { showAddGoalDialog = true },
                                onToggleGoal = { goal, completed -> viewModel.toggleGoal(goal.id, completed) },
                                onEditGoal = { selectedGoalToEdit = it },
                                onDeleteGoal = { viewModel.deleteGoal(it) },
                                onAddTransactionClick = { showAddTransactionDialog = true }
                            )

                            ScreenTab.TRANSACTIONS -> TransactionsScreen(
                                transactions = allTransactions,
                                wallets = wallets,
                                currencySymbol = currencySymbol,
                                onAddTransaction = { showAddTransactionDialog = true },
                                onTransactionClick = { selectedTransactionToEdit = it }
                            )

                            ScreenTab.WALLET -> WalletScreen(
                                wallets = wallets,
                                transactions = allTransactions,
                                currencySymbol = currencySymbol,
                                onAddCard = { showAddWalletDialog = true },
                                onTopUpCard = { topUpTargetWallet = it },
                                onDeleteCard = { viewModel.deleteWallet(it) },
                                onTransactionClick = { selectedTransactionToEdit = it }
                            )

                            ScreenTab.BUDGET -> BudgetPlannerScreen(
                                stats = budgetPlannerStats,
                                categories = categoryExpenses,
                                recurringTransactions = allTransactions.filter { it.isRecurring && it.type == "expense" },
                                currencySymbol = currencySymbol
                            )

                            ScreenTab.SETTINGS -> SettingsScreen(
                                userSettings = userSettings,
                                onUpdateSettings = { name, email, currency, theme, accent ->
                                    viewModel.updateUserSettings(name, email, currency, theme, accent)
                                },
                                onResetDemoData = { viewModel.resetDemoData() },
                                onClearLocalData = { viewModel.clearAllLocalData() },
                                onSyncServer = { viewModel.syncAllData() },
                                onLogout = { viewModel.logout() },
                                apiTestResults = apiTestResults,
                                isRunningApiTests = isRunningApiTests,
                                onRunApiTests = { viewModel.runBackendCrudDiagnostics() }
                            )
                        }
                    }
                }
            }
        }

        // ── Dialogs ──
        if (showAddTransactionDialog) {
            AddEditTransactionDialog(
                initialTransaction = null,
                wallets = wallets,
                currencySymbol = currencySymbol,
                onDismiss = { showAddTransactionDialog = false },
                onSave = { type, amount, category, description, date, isRecurring, walletId ->
                    viewModel.addTransaction(type, amount, category, description, date, isRecurring, walletId)
                }
            )
        }

        if (selectedTransactionToEdit != null) {
            val tx = selectedTransactionToEdit!!
            AddEditTransactionDialog(
                initialTransaction = tx,
                wallets = wallets,
                currencySymbol = currencySymbol,
                onDismiss = { selectedTransactionToEdit = null },
                onSave = { type, amount, category, description, date, isRecurring, walletId ->
                    viewModel.updateTransaction(
                        tx.copy(
                            type = type,
                            amount = amount,
                            category = category,
                            description = description,
                            date = date,
                            isRecurring = isRecurring,
                            walletId = walletId
                        )
                    )
                    selectedTransactionToEdit = null
                },
                onDelete = {
                    viewModel.deleteTransaction(tx.id)
                    selectedTransactionToEdit = null
                }
            )
        }

        if (showAddWalletDialog) {
            AddEditWalletDialog(
                initialWallet = null,
                onDismiss = { showAddWalletDialog = false },
                onSave = { bankName, cardType, cardBrand, cardNumber, cardHolderName, expiryDate, balance, designId, primaryColor, secondaryColor ->
                    viewModel.addWallet(
                        bankName = bankName,
                        cardType = cardType,
                        cardBrand = cardBrand,
                        cardNumber = cardNumber,
                        cardHolderName = cardHolderName,
                        expiryDate = expiryDate,
                        initialBalance = balance,
                        designId = designId,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor
                    )
                }
            )
        }

        if (topUpTargetWallet != null) {
            val w = topUpTargetWallet!!
            TopUpBalanceDialog(
                wallet = w,
                currencySymbol = currencySymbol,
                onDismiss = { topUpTargetWallet = null },
                onConfirm = { amount ->
                    viewModel.topUpWallet(w.id, amount)
                    topUpTargetWallet = null
                }
            )
        }

        if (showEditBudgetDialog) {
            EditBudgetDialog(
                currentIncome = dashboardSummary.monthlyIncome,
                currentLimit = dashboardSummary.monthlyBudgetLimit,
                currencySymbol = currencySymbol,
                onDismiss = { showEditBudgetDialog = false },
                onSave = { income, limit ->
                    viewModel.updateBudgetLimits(income, limit)
                }
            )
        }

        if (showAddGoalDialog) {
            AddEditGoalDialog(
                initialGoal = null,
                currencySymbol = currencySymbol,
                onDismiss = { showAddGoalDialog = false },
                onSave = { title, amount, medium ->
                    viewModel.addGoal(title, amount, medium)
                }
            )
        }

        if (selectedGoalToEdit != null) {
            val g = selectedGoalToEdit!!
            AddEditGoalDialog(
                initialGoal = g,
                currencySymbol = currencySymbol,
                onDismiss = { selectedGoalToEdit = null },
                onSave = { title, amount, medium ->
                    viewModel.updateGoal(g.copy(title = title, amount = amount, medium = medium))
                    selectedGoalToEdit = null
                }
            )
        }
    }
}
