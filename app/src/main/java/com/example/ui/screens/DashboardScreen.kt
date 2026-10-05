package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryExpense
import com.example.model.DashboardSummary
import com.example.model.SavingsGoalEntity
import com.example.model.TransactionEntity
import com.example.model.WalletEntity
import com.example.ui.components.BudgetProgressCard
import com.example.ui.components.ExpenseStatsChart
import com.example.ui.components.FintechBalanceHeader
import com.example.ui.components.FintechDigitalCard
import com.example.ui.components.MonthlyExpenseGrid
import com.example.ui.components.RecentPayments
import com.example.ui.components.SavingsGoalsSection
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextCrispWhite
import com.example.viewmodel.DailyExpensePoint

@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    wallets: List<WalletEntity>,
    recentTransactions: List<TransactionEntity>,
    categoryExpenses: List<CategoryExpense>,
    dailyExpenses: List<DailyExpensePoint>,
    goals: List<SavingsGoalEntity>,
    currencySymbol: String,
    onAddCard: () -> Unit,
    onTopUpCard: (WalletEntity) -> Unit,
    onEditBudget: () -> Unit,
    onViewAllTransactions: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onAddGoal: () -> Unit,
    onToggleGoal: (SavingsGoalEntity, Boolean) -> Unit,
    onEditGoal: (SavingsGoalEntity) -> Unit,
    onDeleteGoal: (String) -> Unit,
    onAddTransactionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var categoryFilterType by remember { mutableStateOf("month") }
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // 1. TOTAL BALANCE & Income / Spent Indicators
        FintechBalanceHeader(
            summary = summary,
            currencySymbol = currencySymbol
        )

        // 2. DIGITAL PAYMENT CARD & Card Actions ([Card Details] [Freeze Card] [More])
        FintechDigitalCard(
            wallets = wallets,
            currencySymbol = currencySymbol,
            onAddCard = onAddCard,
            onTopUp = onTopUpCard
        )

        // 3. PAYMENT / TRANSACTION HISTORY (Grouped by Today, Yesterday, Earlier)
        RecentPayments(
            transactions = recentTransactions,
            wallets = wallets,
            currencySymbol = currencySymbol,
            onViewAllClick = onViewAllTransactions,
            onTransactionClick = onTransactionClick
        )

        // 4. Monthly Budget Progress Card & Income Limit
        BudgetProgressCard(
            summary = summary,
            currencySymbol = currencySymbol,
            onEditBudgetClick = onEditBudget
        )

        // 5. Expense Statistics Trend Chart
        ExpenseStatsChart(
            points = dailyExpenses,
            currencySymbol = currencySymbol,
            onViewDetailsClick = onViewAllTransactions
        )

        // 6. Category Breakdown with Time Tabs
        MonthlyExpenseGrid(
            categories = categoryExpenses,
            currencySymbol = currencySymbol,
            filterType = categoryFilterType,
            onFilterChange = { categoryFilterType = it }
        )

        // 7. Savings Goals Section
        SavingsGoalsSection(
            goals = goals,
            currencySymbol = currencySymbol,
            onAddGoalClick = onAddGoal,
            onToggleGoal = onToggleGoal,
            onEditGoal = onEditGoal,
            onDeleteGoal = onDeleteGoal
        )

        Spacer(modifier = Modifier.height(100.dp)) // Extra padding so floating bottom nav doesn't obscure content
    }
}

/**
 * Tactile Hero Card using the Warm Peach / Sand and Deep Blue palette from reference
 */
@Composable
fun HeroFinancialBalanceCard(
    summary: DashboardSummary,
    currencySymbol: String,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = colors.primaryAccent,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .testTag("hero_financial_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Header Row: Label + Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.onPrimaryAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                             imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = colors.onPrimaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TOTAL BALANCE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onPrimaryAccent.copy(alpha = 0.75f),
                        letterSpacing = 0.8.sp
                    )
                }

                // Quick Add Transaction CTA inside Hero
                Button(
                    onClick = onAddTransactionClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.ctaButton,
                        contentColor = colors.onCtaButton
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("hero_add_expense_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = colors.onCtaButton,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onCtaButton
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Hero Number
            Text(
                text = "$currencySymbol ${String.format("%,.2f", summary.availableBalance)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onPrimaryAccent,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.testTag("hero_balance_value")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Income & Expense Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Income Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Income",
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Income",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onPrimaryAccent.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$currencySymbol${String.format("%,.0f", summary.monthlyIncome)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onPrimaryAccent
                            )
                        }
                    }
                }

                // Spent Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(DangerRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Spent",
                                tint = Color(0xFFB91C1C),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Spent",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onPrimaryAccent.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$currencySymbol${String.format("%,.0f", summary.monthlySpent)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.onPrimaryAccent
                            )
                        }
                    }
                }
            }
        }
    }
}
