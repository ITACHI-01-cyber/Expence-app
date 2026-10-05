package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryExpense
import com.example.model.TransactionEntity
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.BudgetPlannerStats
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun BudgetPlannerScreen(
    stats: BudgetPlannerStats,
    categories: List<CategoryExpense>,
    recurringTransactions: List<TransactionEntity>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time)

    // Calculate allocation ratios
    val totalAllocation = (stats.savings + stats.totalBills + stats.regularExpense).coerceAtLeast(1.0)
    val savingsRatio = (stats.savings / totalAllocation).toFloat()
    val billsRatio = (stats.totalBills / totalAllocation).toFloat()
    val regularRatio = (stats.regularExpense / totalAllocation).toFloat()

    val savingsColor = Color(0xFF10B981)
    val billsColor = Color(0xFF6366F1)
    val regularColor = Color(0xFFF59E0B)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 90.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.surfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Financial Analytics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textCrispWhite,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = "Monthly Spending & Allocation",
                            fontSize = 12.sp,
                            color = colors.textMutedLavender
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.surfaceBorder)
                    ) {
                        Text(
                            text = monthName,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textCrispWhite,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 3 KPI Cards: Expenses, Income, Bills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Expenses
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Expenses", fontSize = 11.sp, color = colors.textMutedLavender)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatCurrency(stats.totalExpense, currencySymbol),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                        Text(text = "Actual Spent", fontSize = 10.sp, color = colors.textMutedLavender)
                    }
                }

                // Income
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Income", fontSize = 11.sp, color = colors.textMutedLavender)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatCurrency(stats.totalIncome, currencySymbol),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(text = "Actual Earned", fontSize = 10.sp, color = colors.textMutedLavender)
                    }
                }

                // Bills
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = billsColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Bills", fontSize = 11.sp, color = colors.textMutedLavender)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatCurrency(stats.totalBills, currencySymbol),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = billsColor
                        )
                        Text(text = "Recurring", fontSize = 10.sp, color = colors.textMutedLavender)
                    }
                }
            }
        }

        // Allocation Donut / Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.surfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Cash Allocation",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textCrispWhite,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "How your monthly income is distributed",
                        fontSize = 12.sp,
                        color = colors.textMutedLavender
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Donut Chart Canvas
                        Box(
                            modifier = Modifier.size(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 18f
                                val diameter = size.minDimension - strokeWidth
                                val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)
                                val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f)

                                var startAngle = -90f
                                val savingsSweep = savingsRatio * 360f
                                val billsSweep = billsRatio * 360f
                                val regularSweep = regularRatio * 360f

                                // Draw Savings
                                drawArc(
                                    color = savingsColor,
                                    startAngle = startAngle,
                                    sweepAngle = savingsSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += savingsSweep

                                // Draw Bills
                                drawArc(
                                    color = billsColor,
                                    startAngle = startAngle,
                                    sweepAngle = billsSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += billsSweep

                                // Draw Regular
                                drawArc(
                                    color = regularColor,
                                    startAngle = startAngle,
                                    sweepAngle = regularSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${(savingsRatio * 100).toInt()}%",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = savingsColor
                                )
                                Text(
                                    text = "Saved",
                                    fontSize = 10.sp,
                                    color = colors.textMutedLavender
                                )
                            }
                        }

                        // Legend Details
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Savings
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(savingsColor))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "Savings: ${formatCurrency(stats.savings, currencySymbol)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textCrispWhite)
                                    Text(text = "${(savingsRatio * 100).toInt()}% of income", fontSize = 10.sp, color = colors.textMutedLavender)
                                }
                            }

                            // Bills
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(billsColor))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "Recurring Bills: ${formatCurrency(stats.totalBills, currencySymbol)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textCrispWhite)
                                    Text(text = "${(billsRatio * 100).toInt()}% of income", fontSize = 10.sp, color = colors.textMutedLavender)
                                }
                            }

                            // Regular Expenses
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(regularColor))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "Regular Expenses: ${formatCurrency(stats.regularExpense, currencySymbol)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textCrispWhite)
                                    Text(text = "${(regularRatio * 100).toInt()}% of income", fontSize = 10.sp, color = colors.textMutedLavender)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bill Summary (Recurring Subscriptions & Bills)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.surfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bill Summary (${recurringTransactions.size})",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textCrispWhite,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            text = "Total: ${formatCurrency(stats.totalBills, currencySymbol)}/mo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = billsColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (recurringTransactions.isEmpty()) {
                        Text(
                            text = "No recurring bills found. Toggle 'Recurring' when adding a transaction.",
                            fontSize = 12.sp,
                            color = colors.textMutedLavender
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            recurringTransactions.forEachIndexed { index, tx ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = billsColor.copy(alpha = 0.12f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Repeat,
                                                    contentDescription = null,
                                                    tint = billsColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = tx.description.ifBlank { tx.category },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = colors.textCrispWhite
                                            )
                                            Text(
                                                text = tx.category,
                                                fontSize = 11.sp,
                                                color = colors.textMutedLavender
                                            )
                                        }
                                    }

                                    Text(
                                        text = formatCurrency(tx.amount, currencySymbol),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = DangerRed
                                    )
                                }
                                if (index < recurringTransactions.size - 1) {
                                    HorizontalDivider(color = colors.surfaceBorder, thickness = 0.8.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
