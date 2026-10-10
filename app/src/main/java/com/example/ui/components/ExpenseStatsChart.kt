package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.FinancialAnalytics
import com.example.analytics.calculateFinancialAnalytics
import com.example.model.TransactionEntity
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ExpenseStatsChart(
    transactions: List<TransactionEntity>,
    currencySymbol: String,
    isLoading: Boolean,
    loadError: String?,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf("7d") }
    val numberOfDays = when (selectedPeriod) {
        "12d" -> 12
        "30d" -> 30
        else -> 7
    }
    val analytics = calculateFinancialAnalytics(transactions, numberOfDays)
    val colors = AppTheme.colors
    val cardBackground = MaterialTheme.colorScheme.surface
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("expense_stats_card"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Financial analytics",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textCrispWhite
                )
                Row(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            RoundedCornerShape(100)
                        )
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf("7d", "12d", "30d").forEach { period ->
                        val selected = selectedPeriod == period
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selected) cardBackground else Color.Transparent,
                                    RoundedCornerShape(100)
                                )
                                .clickable { selectedPeriod = period }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) colors.textCrispWhite else colors.textMutedLavender
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                ChartLegend(color = DangerRed, label = "Expenses")
                ChartLegend(color = SuccessGreen, label = "Income")
            }

            when {
                isLoading && transactions.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primaryAccent)
                    }
                }

                transactions.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (loadError != null) "Transactions unavailable" else "No transactions yet",
                            color = colors.textCrispWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = loadError
                                ?: "Add an income or expense to see your financial activity here.",
                            color = colors.textMutedLavender,
                            fontSize = 12.sp
                        )
                    }
                }

                else -> {
                    if (loadError != null) {
                        Text(
                            text = "Showing saved transactions; latest sync failed.",
                            color = colors.textMutedLavender,
                            fontSize = 12.sp
                        )
                    }
                    DailyFinancialChart(
                        analytics = analytics,
                        currencySymbol = currencySymbol,
                        expenseColor = DangerRed,
                        incomeColor = SuccessGreen
                    )
                    if (analytics.totalExpenses == 0.0 && analytics.totalIncome == 0.0) {
                        Text(
                            text = "No income or expenses in this period.",
                            color = colors.textMutedLavender,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (transactions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FinancialSummaryRow(
                        label = "Total expenses",
                        amount = analytics.totalExpenses,
                        currencySymbol = currencySymbol,
                        color = DangerRed
                    )
                    FinancialSummaryRow(
                        label = "Total income",
                        amount = analytics.totalIncome,
                        currencySymbol = currencySymbol,
                        color = SuccessGreen
                    )
                    FinancialSummaryRow(
                        label = "Net cash flow",
                        amount = analytics.netCashFlow,
                        currencySymbol = currencySymbol,
                        color = if (analytics.netCashFlow < 0) DangerRed else colors.textCrispWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            color = AppTheme.colors.textMutedLavender,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun DailyFinancialChart(
    analytics: FinancialAnalytics,
    currencySymbol: String,
    expenseColor: Color,
    incomeColor: Color
) {
    val textColor = AppTheme.colors.textMutedLavender
    val maximum = analytics.points
        .flatMap { listOf(it.expenses, it.income) }
        .maxOrNull()
        ?.takeIf { it > 0.0 }
        ?: 1.0
    val axisMaximum = maximum * 1.15
    val axisLabels = listOf(axisMaximum, axisMaximum / 2.0, 0.0)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(52.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                axisLabels.forEach { amount ->
                    Text(
                        text = formatAxisAmount(amount, currencySymbol),
                        color = textColor,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .testTag("financial_analytics_plot")
            ) {
                val left = 2.dp.toPx()
                val right = size.width - 2.dp.toPx()
                val top = 8.dp.toPx()
                val bottom = size.height - 8.dp.toPx()
                val chartHeight = bottom - top
                val chartWidth = right - left
                val rowCount = analytics.points.size
                if (rowCount == 0) return@Canvas

                for (fraction in listOf(0f, 0.5f, 1f)) {
                    val y = top + chartHeight * fraction
                    drawLine(
                        color = textColor.copy(alpha = 0.18f),
                        start = Offset(left, y),
                        end = Offset(right, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                fun drawSeries(valueAt: (Int) -> Double, color: Color) {
                    val path = Path()
                    analytics.points.forEachIndexed { index, point ->
                        val x = if (rowCount == 1) left + chartWidth / 2f
                        else left + chartWidth * index / (rowCount - 1)
                        val y = bottom - (valueAt(index) / axisMaximum).toFloat() * chartHeight
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                drawSeries({ analytics.points[it].expenses }, expenseColor)
                drawSeries({ analytics.points[it].income }, incomeColor)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val indices = listOf(0, analytics.points.size / 2, analytics.points.lastIndex).distinct()
            indices.forEach { index ->
                Text(
                    text = analytics.points[index].dateLabel,
                    color = textColor,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun FinancialSummaryRow(
    label: String,
    amount: Double,
    currencySymbol: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AppTheme.colors.textMutedLavender, fontSize = 13.sp)
        Text(
            text = formatFinancialAmount(amount, currencySymbol),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

private fun formatFinancialAmount(amount: Double, currencySymbol: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "$currencySymbol${formatter.format(amount)}"
}

private fun formatAxisAmount(amount: Double, currencySymbol: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
        maximumFractionDigits = 0
    }
    return "$currencySymbol${formatter.format(amount)}"
}
