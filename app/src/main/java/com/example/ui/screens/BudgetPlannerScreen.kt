package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.AnalyticsPeriod
import com.example.analytics.FinancialAnalytics
import com.example.analytics.calculateFinancialAnalytics
import com.example.analytics.resolveAnalyticsDateRange
import com.example.model.TransactionEntity
import com.example.model.WalletEntity
import com.example.network.CreditActivityItem
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun BudgetPlannerScreen(
    transactions: List<TransactionEntity>,
    creditCards: List<WalletEntity> = emptyList(),
    creditActivity: Map<String, List<CreditActivityItem>> = emptyMap(),
    creditDataLoading: Boolean = false,
    onLoadCreditCardsData: (List<String>) -> Unit = {},
    currencySymbol: String,
    isLoading: Boolean,
    loadError: String?,
    onViewAllTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val todayMillis = remember { startOfLocalDay(Calendar.getInstance()).timeInMillis }
    var selectedRange by remember { mutableStateOf(AnalyticsPeriod.LAST_30_DAYS) }
    var customStartMillis by remember {
        mutableLongStateOf(
            startOfLocalDay(Calendar.getInstance()).apply {
                add(Calendar.DAY_OF_YEAR, -29)
            }.timeInMillis
        )
    }
    var customEndMillis by remember { mutableLongStateOf(todayMillis) }
    val dateRange = resolveAnalyticsDateRange(
        period = selectedRange,
        today = localDayFromMillis(todayMillis),
        customStartMillis = customStartMillis,
        customEndMillis = customEndMillis
    )
    val creditCardIds = creditCards.map { it.id }
    androidx.compose.runtime.LaunchedEffect(creditCardIds) {
        if (creditCardIds.isNotEmpty()) onLoadCreditCardsData(creditCardIds)
    }
    val localZone = ZoneId.systemDefault()
    val rangeStart = Instant.ofEpochMilli(dateRange.startDateMillis).atZone(localZone).toLocalDate()
    val rangeEnd = Instant.ofEpochMilli(dateRange.endDateMillis).atZone(localZone).toLocalDate()
    val creditLiability = creditCards.sumOf { it.outstandingBalance ?: 0.0 }
    val totalCreditLimit = creditCards.sumOf { it.creditLimit ?: 0.0 }
    val creditRepaymentsInPeriod = creditCards.sumOf { card ->
        creditActivity[card.id].orEmpty()
            .filter { it.transactionKind.equals("repayment", ignoreCase = true) }
            .sumOf { item ->
                val activityDate = runCatching {
                    LocalDateTime.parse(item.date)
                        .atZone(ZoneId.of(card.timezone))
                        .withZoneSameInstant(localZone)
                        .toLocalDate()
                }.getOrNull()
                if (activityDate != null && !activityDate.isBefore(rangeStart) && !activityDate.isAfter(rangeEnd)) {
                    item.amount
                } else {
                    0.0
                }
            }
    }
    val analytics = calculateFinancialAnalytics(
        transactions,
        dateRange.startDateMillis,
        dateRange.endDateMillis
    )
    val recurringTransactions = analytics.recurringTransactions
    val dateFormatter = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    val rangeLabel = "${dateFormatter.format(dateRange.startDateMillis)} – ${dateFormatter.format(dateRange.endDateMillis)}"
    val expenseColor = DangerRed
    val incomeColor = SuccessGreen
    val accent = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.dp, colors.surfaceBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Financial Analytics", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textCrispWhite)
                            Text("Activity from your saved transactions", fontSize = 12.sp, color = colors.textMutedLavender)
                        }
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Selected date range", tint = accent)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AnalyticsPeriod.LAST_7_DAYS to "7 days",
                            AnalyticsPeriod.LAST_30_DAYS to "30 days",
                            AnalyticsPeriod.LAST_6_MONTHS to "6 months",
                            AnalyticsPeriod.CUSTOM to "Custom"
                        ).forEach { (period, label) ->
                            RangeChip(label = label, selected = selectedRange == period) {
                                selectedRange = period
                            }
                        }
                    }
                    if (selectedRange == AnalyticsPeriod.CUSTOM) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DateRangeButton(
                                label = "From ${dateFormatter.format(customStartMillis)}",
                                modifier = Modifier.weight(1f)
                            ) {
                                val initial = localDayFromMillis(customStartMillis)
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val selected = Calendar.getInstance().apply {
                                            clear()
                                            set(year, month, day)
                                        }.timeInMillis
                                        customStartMillis = selected
                                        if (selected > customEndMillis) customEndMillis = selected
                                    },
                                    initial.get(Calendar.YEAR),
                                    initial.get(Calendar.MONTH),
                                    initial.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            DateRangeButton(
                                label = "To ${dateFormatter.format(customEndMillis)}",
                                modifier = Modifier.weight(1f)
                            ) {
                                val initial = localDayFromMillis(customEndMillis)
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val selected = Calendar.getInstance().apply {
                                            clear()
                                            set(year, month, day)
                                        }.timeInMillis
                                        customEndMillis = selected
                                        if (selected < customStartMillis) customStartMillis = selected
                                    },
                                    initial.get(Calendar.YEAR),
                                    initial.get(Calendar.MONTH),
                                    initial.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                        }
                    }
                    Text(rangeLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMutedLavender)
                }
            }
        }

        if (isLoading && transactions.isEmpty()) {
            item {
                CardPanel {
                    Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = accent)
                    }
                }
            }
        } else {
            if (loadError != null) {
                item {
                    Text(
                        text = "Showing saved transactions. Latest sync failed: $loadError",
                        color = colors.textMutedLavender,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OverviewCard(
                            title = "Expenses",
                            value = formatAnalyticsAmount(analytics.totalExpenses, currencySymbol),
                            icon = { Icon(Icons.Default.ArrowUpward, null, tint = expenseColor, modifier = Modifier.size(16.dp)) },
                            valueColor = expenseColor,
                            modifier = Modifier.weight(1f)
                        )
                        OverviewCard(
                            title = "Income",
                            value = formatAnalyticsAmount(analytics.totalIncome, currencySymbol),
                            icon = { Icon(Icons.Default.ArrowDownward, null, tint = incomeColor, modifier = Modifier.size(16.dp)) },
                            valueColor = incomeColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OverviewCard(
                            title = "Net cash flow",
                            value = formatAnalyticsAmount(analytics.netCashFlow, currencySymbol),
                            subtitle = "Not account balance or savings",
                            valueColor = if (analytics.netCashFlow >= 0.0) incomeColor else expenseColor,
                            modifier = Modifier.weight(1f)
                        )
                        OverviewCard(
                            title = "Transactions",
                            value = analytics.transactionCount.toString(),
                            subtitle = "In selected period",
                            valueColor = colors.textCrispWhite,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (creditCards.isNotEmpty()) {
                item {
                    CardPanel {
                        SectionHeading(
                            "Credit liabilities",
                            "Liability is separate from available cash; repayment transfers are not counted again as expenses."
                        )
                        Spacer(Modifier.height(10.dp))
                        OverviewCard(
                            title = "Outstanding credit",
                            value = formatAnalyticsAmount(creditLiability, currencySymbol),
                            subtitle = "Current liability",
                            valueColor = DangerRed,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OverviewCard(
                            title = "Credit limit",
                            value = formatAnalyticsAmount(totalCreditLimit, currencySymbol),
                            subtitle = "Not available cash",
                            valueColor = colors.textCrispWhite,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OverviewCard(
                            title = "Debt payments",
                            value = if (creditDataLoading && creditCards.any { !creditActivity.containsKey(it.id) }) {
                                "Loading…"
                            } else {
                                formatAnalyticsAmount(creditRepaymentsInPeriod, currencySymbol)
                            },
                            subtitle = "Internal transfers · selected period",
                            valueColor = incomeColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                CardPanel {
                    SectionHeading(title = "Spending & income trends", subtitle = "Daily totals · ${currencySymbol}")
                    Spacer(Modifier.height(14.dp))
                    if (analytics.transactionCount == 0) {
                        EmptyMessage(
                            title = "No transactions in this period",
                            message = "Choose another date range or add a transaction to see the trend."
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            ChartLegend(color = expenseColor, label = "Expenses")
                            ChartLegend(color = incomeColor, label = "Income")
                        }
                        Spacer(Modifier.height(10.dp))
                        TrendChart(analytics, currencySymbol)
                    }
                }
            }

            item {
                CardPanel {
                    SectionHeading("Expense categories", "Share of recorded expenses in this period")
                    Spacer(Modifier.height(14.dp))
                    when {
                        analytics.totalExpenses <= 0.0 -> EmptyMessage(
                            title = "No expenses recorded",
                            message = "Category totals are available when expense transactions are recorded."
                        )
                        else -> analytics.categories.forEachIndexed { index, category ->
                            CategoryRow(
                                category = category.category,
                                amount = formatAnalyticsAmount(category.amount, currencySymbol),
                                percentage = category.percentage,
                                color = categoryColor(index),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            }

            item {
                CardPanel {
                    SectionHeading("Income & savings", "Cash flow is not the same as savings or wallet balance")
                    Spacer(Modifier.height(10.dp))
                    if (analytics.totalIncome == 0.0) {
                        Text("No income recorded for this period", color = colors.textMutedLavender, fontSize = 13.sp)
                        Text("Income-dependent metrics are unavailable. Actual savings cannot be calculated from transactions alone.", color = colors.textMutedLavender, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
                    } else {
                        Text(
                            "Net cash flow: ${formatAnalyticsAmount(analytics.netCashFlow, currencySymbol)}",
                            color = if (analytics.netCashFlow >= 0) incomeColor else expenseColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text("This is income minus expenses for the selected period; it does not account for transfers, opening balances, or other savings.", color = colors.textMutedLavender, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
                    }
                }
            }

            item {
                CardPanel {
                    SectionHeading(
                        "Recurring transactions",
                        "Only expenses marked as monthly are shown with their recorded amount. Due dates and payment status are not stored."
                    )
                    Spacer(Modifier.height(12.dp))
                    if (recurringTransactions.isEmpty()) {
                        EmptyMessage("No marked recurring transactions", "Mark a monthly bill or subscription as recurring when recording it.")
                    } else {
                        recurringTransactions.forEachIndexed { index, transaction ->
                            TransactionSummaryRow(
                                transaction = transaction,
                                currencySymbol = currencySymbol,
                                dateFormatter = dateFormatter,
                                recurring = true
                            )
                            if (index < recurringTransactions.lastIndex) {
                                HorizontalDivider(color = colors.surfaceBorder, modifier = Modifier.padding(vertical = 9.dp))
                            }
                        }
                    }
                }
            }

            item {
                CardPanel {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeading("Recent activity", "Latest transactions in the selected period")
                        Text(
                            "View all",
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable(onClick = onViewAllTransactions)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    if (analytics.recentTransactions.isEmpty()) {
                        EmptyMessage("No recent activity", "Transactions within this period will appear here.")
                    } else {
                        analytics.recentTransactions.forEachIndexed { index, transaction ->
                            TransactionSummaryRow(
                                transaction = transaction,
                                currencySymbol = currencySymbol,
                                dateFormatter = dateFormatter,
                                recurring = false
                            )
                            if (index < analytics.recentTransactions.lastIndex) {
                                HorizontalDivider(color = colors.surfaceBorder, modifier = Modifier.padding(vertical = 9.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardPanel(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    val colors = AppTheme.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.surfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), content = content)
    }
}

@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textCrispWhite)
        Text(subtitle, fontSize = 11.sp, color = AppTheme.colors.textMutedLavender, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun OverviewCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: (@Composable () -> Unit)? = null
) {
    val colors = AppTheme.colors
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.surfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    icon()
                    Spacer(Modifier.width(5.dp))
                }
                Text(title, fontSize = 11.sp, color = colors.textMutedLavender)
            }
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1)
            if (subtitle != null) {
                Text(subtitle, fontSize = 9.sp, color = colors.textMutedLavender, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

@Composable
private fun RangeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else colors.surfaceVariant,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else colors.surfaceBorder)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = if (selected) Color.White else colors.textMutedLavender,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun DateRangeButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = AppTheme.colors.surfaceVariant,
        border = BorderStroke(1.dp, AppTheme.colors.surfaceBorder)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp), color = AppTheme.colors.textCrispWhite, fontSize = 11.sp)
    }
}

@Composable
private fun EmptyMessage(title: String, message: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = AppTheme.colors.textCrispWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(message, color = AppTheme.colors.textMutedLavender, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun ChartLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, color = AppTheme.colors.textMutedLavender, fontSize = 11.sp)
    }
}

@Composable
private fun TrendChart(analytics: FinancialAnalytics, currencySymbol: String) {
    val points = analytics.points
    val textColor = AppTheme.colors.textMutedLavender
    val maximum = points.flatMap { listOf(it.expenses, it.income) }.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
    val axisMaximum = maximum * 1.15
    Column {
        Row(Modifier.fillMaxWidth().height(170.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(
                Modifier.width(58.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                listOf(axisMaximum, axisMaximum / 2.0, 0.0).forEach {
                    Text(formatAxisAmount(it, currencySymbol), color = textColor, fontSize = 9.sp, maxLines = 1)
                }
            }
            Canvas(Modifier.weight(1f).fillMaxSize()) {
                val left = 2.dp.toPx()
                val right = size.width - 2.dp.toPx()
                val top = 8.dp.toPx()
                val bottom = size.height - 8.dp.toPx()
                val width = right - left
                val height = bottom - top
                if (points.isEmpty()) return@Canvas
                listOf(0f, 0.5f, 1f).forEach { fraction ->
                    val y = top + height * fraction
                    drawLine(textColor.copy(alpha = 0.2f), Offset(left, y), Offset(right, y), 1.dp.toPx())
                }
                fun plot(selector: (Int) -> Double, color: Color) {
                    val path = Path()
                    points.forEachIndexed { index, point ->
                        val x = if (points.size == 1) left + width / 2f else left + width * index / (points.lastIndex)
                        val y = bottom - (selector(index) / axisMaximum).toFloat() * height
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                plot({ points[it].expenses }, DangerRed)
                plot({ points[it].income }, SuccessGreen)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(0, points.size / 2, points.lastIndex).distinct().forEach { index ->
                Text(points[index].dateLabel, color = textColor, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: String,
    amount: String,
    percentage: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(category, color = AppTheme.colors.textCrispWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("$amount  (${percentage.toOneDecimal()}%)", color = AppTheme.colors.textMutedLavender, fontSize = 11.sp)
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(AppTheme.colors.surfaceVariant)) {
            Box(Modifier.fillMaxWidth((percentage / 100.0).toFloat().coerceIn(0f, 1f)).height(6.dp).clip(CircleShape).background(color))
        }
    }
}

@Composable
private fun TransactionSummaryRow(
    transaction: TransactionEntity,
    currencySymbol: String,
    dateFormatter: SimpleDateFormat,
    recurring: Boolean
) {
    val isIncome = transaction.type.equals("income", ignoreCase = true)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (recurring) Icons.Default.Repeat else Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(
                transaction.description.ifBlank { transaction.category.ifBlank { "Transaction" } },
                color = AppTheme.colors.textCrispWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1
            )
            Text(
                "${transaction.category} · ${dateFormatter.format(transaction.timestamp)}",
                color = AppTheme.colors.textMutedLavender,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
        Text(
            "${if (isIncome) "+" else "-"}${formatAnalyticsAmount(transaction.amount, currencySymbol)}${if (recurring) " /mo" else ""}",
            color = if (isIncome) SuccessGreen else DangerRed,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

private fun categoryColor(index: Int): Color = listOf(
    Color(0xFF9B6DFF),
    Color(0xFF38BDF8),
    Color(0xFFF59E0B),
    Color(0xFF10B981),
    Color(0xFFEC4899),
    Color(0xFF8B9AAF)
)[index % 6]

private fun formatAnalyticsAmount(amount: Double, currencySymbol: String): String {
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

private fun Double.toOneDecimal(): String = String.format(Locale.getDefault(), "%.1f", this)

private fun startOfLocalDay(calendar: Calendar): Calendar = (calendar.clone() as Calendar).apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

private fun localDayFromMillis(millis: Long): Calendar = startOfLocalDay(Calendar.getInstance().apply {
    timeInMillis = millis
})
