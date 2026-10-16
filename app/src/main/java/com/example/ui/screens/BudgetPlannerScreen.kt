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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import com.example.analytics.AnalyticsPeriod
import com.example.ui.components.ChartMetricType
import kotlin.math.abs
import kotlin.math.roundToInt
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
    val creditLiability = creditCards.sumOf { it.outstandingBalance ?: 0.0 }
    val totalCreditLimit = creditCards.sumOf { it.creditLimit ?: 0.0 }
    val creditRepaymentsInPeriod = creditCards.sumOf { card ->
        creditActivity[card.id].orEmpty()
            .filter { it.transactionKind.equals("repayment", ignoreCase = true) }
            .sumOf { item ->
                val activityMillis = com.example.analytics.parseDateStringToMillis(item.date)
                if (activityMillis != null && activityMillis in dateRange.startDateMillis..dateRange.endDateMillis) {
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
    val dateFormatter = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    val rangeLabel = "${dateFormatter.format(dateRange.startDateMillis)} – ${dateFormatter.format(dateRange.endDateMillis)}"
    val expenseColor = DangerRed
    val incomeColor = SuccessGreen
    val accent = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
                            AnalyticsPeriod.LAST_12_DAYS to "12 days",
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
    if (points.isEmpty()) return

    val textColor = AppTheme.colors.textMutedLavender
    var selectedMetric by remember { mutableStateOf(ChartMetricType.EXPENSES) }

    val values = remember(points, selectedMetric) {
        points.map { point ->
            when (selectedMetric) {
                ChartMetricType.EXPENSES -> point.expenses
                ChartMetricType.INCOME -> point.income
                ChartMetricType.NET_FLOW -> point.netCashFlow
            }
        }
    }

    val maxVal = remember(values) {
        values.maxOrNull()?.takeIf { abs(it) > 0.0 } ?: 100.0
    }
    val minVal = remember(values) {
        values.minOrNull()?.takeIf { it < 0.0 } ?: 0.0
    }

    val axisMax = if (maxVal > 0) maxVal * 1.18 else 100.0
    val axisMin = if (minVal < 0) minVal * 1.15 else 0.0
    val axisRange = (axisMax - axisMin).coerceAtLeast(1.0)

    val axisLabels = listOf(
        axisMax,
        axisMin + axisRange * 0.5,
        axisMin
    )

    var selectedIndex by remember(points, selectedMetric) {
        val defaultIdx = values.indexOfMax().coerceIn(0, points.lastIndex)
        mutableIntStateOf(defaultIdx)
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedMetric, analytics.points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    var chartWidthPx by remember { mutableFloatStateOf(1f) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Metric Switcher Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    RoundedCornerShape(12.dp)
                )
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ChartMetricType.entries.forEach { metric ->
                val selected = selectedMetric == metric
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (selected) Color(0xFFA855F7).copy(alpha = 0.22f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedMetric = metric }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = metric.label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color(0xFFA855F7) else textColor
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(Color(0xFF0F1118), RoundedCornerShape(20.dp))
                .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 12.dp)
                .onGloballyPositioned { chartWidthPx = it.size.width.toFloat() }
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(points, selectedMetric) {
                                detectTapGestures { offset ->
                                    selectedIndex = calculateClosestIndex(
                                        xPx = offset.x,
                                        widthPx = size.width.toFloat(),
                                        count = points.size
                                    )
                                }
                            }
                            .pointerInput(points, selectedMetric) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    selectedIndex = calculateClosestIndex(
                                        xPx = change.position.x,
                                        widthPx = size.width.toFloat(),
                                        count = points.size
                                    )
                                }
                            }
                    ) {
                        val left = 6.dp.toPx()
                        val right = size.width - 6.dp.toPx()
                        val top = 34.dp.toPx()
                        val bottom = size.height - 8.dp.toPx()
                        val chartWidth = (right - left).coerceAtLeast(1f)
                        val chartHeight = (bottom - top).coerceAtLeast(1f)
                        val count = points.size

                        // Gridlines
                        listOf(0f, 0.5f, 1f).forEach { frac ->
                            val y = top + chartHeight * frac
                            drawLine(
                                color = Color(0xFF2D3142).copy(alpha = 0.5f),
                                start = Offset(left, y),
                                end = Offset(right, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val pointCoords = List(count) { i ->
                            val x = if (count == 1) left + chartWidth / 2f
                            else left + chartWidth * i / (count - 1)
                            val normValue = ((values[i] - axisMin) / axisRange).toFloat()
                            val y = bottom - (normValue * chartHeight)
                            Offset(x, y)
                        }

                        // Vertical Gridlines
                        pointCoords.forEach { pt ->
                            drawLine(
                                color = Color(0xFF2D3142).copy(alpha = 0.35f),
                                start = Offset(pt.x, top),
                                end = Offset(pt.x, bottom),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        }

                        if (count > 0) {
                            val linePath = Path()
                            linePath.moveTo(pointCoords[0].x, pointCoords[0].y)

                            if (count == 1) {
                                linePath.lineTo(pointCoords[0].x + 1f, pointCoords[0].y)
                            } else {
                                for (i in 0 until count - 1) {
                                    val p0 = pointCoords[i]
                                    val p1 = pointCoords[i + 1]
                                    val dx = p1.x - p0.x
                                    val ctrl1 = Offset(p0.x + dx * 0.45f, p0.y)
                                    val ctrl2 = Offset(p0.x + dx * 0.55f, p1.y)
                                    linePath.cubicTo(
                                        ctrl1.x, ctrl1.y,
                                        ctrl2.x, ctrl2.y,
                                        p1.x, p1.y
                                    )
                                }
                            }

                            val areaPath = Path()
                            areaPath.addPath(linePath)
                            areaPath.lineTo(pointCoords.last().x, bottom)
                            areaPath.lineTo(pointCoords.first().x, bottom)
                            areaPath.close()

                            val areaGradient = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFA855F7).copy(alpha = 0.38f),
                                    Color(0xFFA855F7).copy(alpha = 0.12f),
                                    Color.Transparent
                                ),
                                startY = top,
                                endY = bottom
                            )

                            clipRect(
                                left = 0f,
                                top = 0f,
                                right = left + (chartWidth + 12.dp.toPx()) * animProgress.value,
                                bottom = size.height
                            ) {
                                drawPath(path = areaPath, brush = areaGradient)

                                drawPath(
                                    path = linePath,
                                    color = Color(0xFF8B5CF6).copy(alpha = 0.30f),
                                    style = Stroke(
                                        width = 6.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )

                                drawPath(
                                    path = linePath,
                                    color = Color(0xFFA855F7),
                                    style = Stroke(
                                        width = 2.8.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }

                            val safeIndex = selectedIndex.coerceIn(0, count - 1)
                            val selPt = pointCoords[safeIndex]

                            // Crosshair
                            drawLine(
                                color = Color(0xFFA855F7).copy(alpha = 0.55f),
                                start = Offset(selPt.x, top),
                                end = Offset(selPt.x, bottom),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                            )

                            // Point Marker
                            drawCircle(Color(0xFFA855F7).copy(alpha = 0.25f), 11.dp.toPx(), selPt)
                            drawCircle(Color.White, 6.5.dp.toPx(), selPt)
                            drawCircle(Color(0xFFA855F7), 4.5.dp.toPx(), selPt)
                        }
                    }

                    // Floating Tooltip Above Point
                    val safeIdx = selectedIndex.coerceIn(0, points.lastIndex)
                    val activePoint = points[safeIdx]
                    val activeVal = values[safeIdx]

                    val formattedVal = formatAnalyticsAmount(activeVal, currencySymbol)
                    val pointFraction = if (points.size > 1) safeIdx.toFloat() / (points.size - 1) else 0.5f

                    Box(
                        modifier = Modifier
                            .align(
                                when {
                                    pointFraction < 0.25f -> Alignment.TopStart
                                    pointFraction > 0.75f -> Alignment.TopEnd
                                    else -> Alignment.TopCenter
                                }
                            )
                            .offset {
                                if (pointFraction >= 0.25f && pointFraction <= 0.75f) {
                                    IntOffset(((pointFraction - 0.5f) * 160).roundToInt(), 0)
                                } else {
                                    IntOffset(0, 0)
                                }
                            }
                            .background(Color(0xFF1E1B2E), RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = activePoint.dateLabel,
                                color = AppTheme.colors.textMutedLavender,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text("•", color = Color(0xFFA855F7), fontSize = 11.sp)
                            Text(
                                text = "${selectedMetric.label}: $formattedVal",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Y-Axis Right Aligned
                Column(
                    modifier = Modifier
                        .width(52.dp)
                        .fillMaxHeight()
                        .padding(top = 28.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    axisLabels.forEach { amount ->
                        Text(
                            text = formatAxisAmount(amount, currencySymbol),
                            color = textColor.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val step = when {
                points.size > 20 -> 6
                points.size > 10 -> 3
                else -> 2
            }
            val displayIndices = (points.indices step step).toList()
                .let { list -> if (points.lastIndex !in list) list + points.lastIndex else list }

            displayIndices.forEach { index ->
                Text(
                    text = points[index].dateLabel,
                    color = if (index == selectedIndex) Color(0xFFA855F7) else textColor,
                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp
                )
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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                color = AppTheme.colors.textCrispWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$amount  (${percentage.toOneDecimal()}%)",
                color = AppTheme.colors.textMutedLavender,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppTheme.colors.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((percentage / 100.0).toFloat().coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

private fun calculateClosestIndex(xPx: Float, widthPx: Float, count: Int): Int {
    if (count <= 1 || widthPx <= 0f) return 0
    val fraction = (xPx / widthPx).coerceIn(0f, 1f)
    return (fraction * (count - 1)).roundToInt().coerceIn(0, count - 1)
}

private fun List<Double>.indexOfMax(): Int {
    if (isEmpty()) return 0
    var maxIdx = 0
    var maxVal = this[0]
    for (i in 1 until size) {
        if (this[i] > maxVal) {
            maxVal = this[i]
            maxIdx = i
        }
    }
    return maxIdx
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
