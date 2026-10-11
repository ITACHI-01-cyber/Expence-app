package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analytics.DailyFinancialPoint
import com.example.analytics.FinancialAnalytics
import com.example.analytics.calculateFinancialAnalytics
import com.example.model.TransactionEntity
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class ChartMetricType(val label: String) {
    EXPENSES("Expenses"),
    INCOME("Income"),
    NET_FLOW("Net Flow")
}

// Chart color definitions matching premium dark fintech aesthetic
private val GlowingPurple = Color(0xFFA855F7)
private val PurpleGlowSoft = Color(0xFF8B5CF6)
private val ChartBackgroundDark = Color(0xFF0F1118)
private val TooltipBackgroundDark = Color(0xFF1E1B2E)
private val GridlineColor = Color(0xFF2D3142)

@Composable
fun ExpenseStatsChart(
    transactions: List<TransactionEntity>,
    currencySymbol: String,
    isLoading: Boolean,
    loadError: String?,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf("7d") }
    var selectedMetric by remember { mutableStateOf(ChartMetricType.EXPENSES) }

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
            // Header Row: Title & Time Period Pills
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
                // Period Selector
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
                                    if (selected) GlowingPurple else Color.Transparent,
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
                                color = if (selected) Color.White else colors.textMutedLavender
                            )
                        }
                    }
                }
            }

            // Metric Switcher Tabs (Expenses, Income, Net Flow)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChartMetricType.entries.forEach { metric ->
                    val selected = selectedMetric == metric
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selected) GlowingPurple.copy(alpha = 0.22f) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedMetric = metric }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = metric.label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) GlowingPurple else colors.textMutedLavender
                        )
                    }
                }
            }

            when {
                isLoading && transactions.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GlowingPurple)
                    }
                }

                transactions.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
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

                    // Main Premium Smooth Purple Area Chart
                    SmoothFinancialAreaChart(
                        analytics = analytics,
                        currencySymbol = currencySymbol,
                        selectedMetric = selectedMetric,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Summary Totals
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
private fun SmoothFinancialAreaChart(
    analytics: FinancialAnalytics,
    currencySymbol: String,
    selectedMetric: ChartMetricType,
    modifier: Modifier = Modifier
) {
    val points = analytics.points
    if (points.isEmpty()) return

    val textColor = AppTheme.colors.textMutedLavender

    // Extract values based on metric
    val values = remember(points, selectedMetric) {
        points.map { point ->
            when (selectedMetric) {
                ChartMetricType.EXPENSES -> point.expenses
                ChartMetricType.INCOME -> point.income
                ChartMetricType.NET_FLOW -> point.netCashFlow
            }
        }
    }

    val totalSum = remember(values, selectedMetric) {
        when (selectedMetric) {
            ChartMetricType.EXPENSES -> analytics.totalExpenses
            ChartMetricType.INCOME -> analytics.totalIncome
            ChartMetricType.NET_FLOW -> abs(analytics.netCashFlow)
        }
    }

    val maxVal = remember(values) {
        values.maxOrNull()?.takeIf { abs(it) > 0.0 } ?: 100.0
    }
    val minVal = remember(values) {
        values.minOrNull()?.takeIf { it < 0.0 } ?: 0.0
    }

    // Determine scale limits
    val axisMax = if (maxVal > 0) maxVal * 1.18 else 100.0
    val axisMin = if (minVal < 0) minVal * 1.15 else 0.0
    val axisRange = (axisMax - axisMin).coerceAtLeast(1.0)

    val axisLabels = listOf(
        axisMax,
        axisMin + axisRange * 0.5,
        axisMin
    )

    // Selection State for interactive hover/tap
    var selectedIndex by remember(points, selectedMetric) {
        // Default to the point with maximum value or latest point
        val defaultIdx = values.indexOfMax().coerceIn(0, points.lastIndex)
        mutableIntStateOf(defaultIdx)
    }

    // Path Animation
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedMetric, analytics.points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    var chartContainerWidthPx by remember { mutableStateOf(1f) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(ChartBackgroundDark, RoundedCornerShape(20.dp))
                .padding(start = 14.dp, end = 12.dp, top = 16.dp, bottom = 12.dp)
                .onGloballyPositioned { chartContainerWidthPx = it.size.width.toFloat() }
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Chart Plot Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("financial_analytics_plot")
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
                        val top = 36.dp.toPx() // Room for floating tooltip
                        val bottom = size.height - 8.dp.toPx()
                        val chartWidth = (right - left).coerceAtLeast(1f)
                        val chartHeight = (bottom - top).coerceAtLeast(1f)
                        val count = points.size

                        // 1. Draw Subtle Horizontal Grid Lines
                        val horizontalFractions = listOf(0f, 0.5f, 1f)
                        horizontalFractions.forEach { frac ->
                            val y = top + chartHeight * frac
                            drawLine(
                                color = GridlineColor.copy(alpha = 0.5f),
                                start = Offset(left, y),
                                end = Offset(right, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Calculate Point Screen Coordinates
                        val pointCoords = List(count) { i ->
                            val x = if (count == 1) left + chartWidth / 2f
                            else left + chartWidth * i / (count - 1)
                            val normValue = ((values[i] - axisMin) / axisRange).toFloat()
                            val y = bottom - (normValue * chartHeight)
                            Offset(x, y)
                        }

                        // 2. Draw Faint Dotted Vertical Gridlines
                        pointCoords.forEach { pt ->
                            drawLine(
                                color = GridlineColor.copy(alpha = 0.35f),
                                start = Offset(pt.x, top),
                                end = Offset(pt.x, bottom),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        }

                        if (count > 0) {
                            // 3. Build Smooth Bezier Line Path
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

                            // 4. Build Gradient Area Fill Path
                            val areaPath = Path()
                            areaPath.addPath(linePath)
                            areaPath.lineTo(pointCoords.last().x, bottom)
                            areaPath.lineTo(pointCoords.first().x, bottom)
                            areaPath.close()

                            val areaGradient = Brush.verticalGradient(
                                colors = listOf(
                                    GlowingPurple.copy(alpha = 0.38f),
                                    GlowingPurple.copy(alpha = 0.12f),
                                    Color.Transparent
                                ),
                                startY = top,
                                endY = bottom
                            )

                            // Apply Animation Clip
                            clipRect(
                                left = 0f,
                                top = 0f,
                                right = left + (chartWidth + 12.dp.toPx()) * animProgress.value,
                                bottom = size.height
                            ) {
                                // Draw Area Fill
                                drawPath(path = areaPath, brush = areaGradient)

                                // Draw Outer Soft Glow Line
                                drawPath(
                                    path = linePath,
                                    color = PurpleGlowSoft.copy(alpha = 0.30f),
                                    style = Stroke(
                                        width = 6.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )

                                // Draw Main Smooth Line Stroke
                                drawPath(
                                    path = linePath,
                                    color = GlowingPurple,
                                    style = Stroke(
                                        width = 2.8.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }

                            // 5. Draw Crosshair & Selected Highlighted Point Marker
                            val safeIndex = selectedIndex.coerceIn(0, count - 1)
                            val selPt = pointCoords[safeIndex]

                            // Vertical Crosshair Line
                            drawLine(
                                color = GlowingPurple.copy(alpha = 0.55f),
                                start = Offset(selPt.x, top),
                                end = Offset(selPt.x, bottom),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                            )

                            // Outer Pulse Circle
                            drawCircle(
                                color = GlowingPurple.copy(alpha = 0.25f),
                                radius = 11.dp.toPx(),
                                center = selPt
                            )

                            // Outer White Ring
                            drawCircle(
                                color = Color.White,
                                radius = 6.5.dp.toPx(),
                                center = selPt
                            )

                            // Center Bright Purple Dot
                            drawCircle(
                                color = GlowingPurple,
                                radius = 4.5.dp.toPx(),
                                center = selPt
                            )
                        }
                    }

                    // Floating Tooltip Above Selected Point
                    val safeIdx = selectedIndex.coerceIn(0, points.lastIndex)
                    val activePoint = points[safeIdx]
                    val activeVal = values[safeIdx]

                    val formattedVal = formatFinancialAmount(activeVal, currencySymbol)
                    val percentageStr = if (totalSum > 0.0 && activeVal > 0.0) {
                        val pct = (activeVal / totalSum) * 100.0
                        "${String.format(Locale.getDefault(), "%.1f", pct)}%"
                    } else null

                    // Tooltip Position Calculation
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
                                    IntOffset(
                                        x = ((pointFraction - 0.5f) * 160).roundToInt(),
                                        y = 0
                                    )
                                } else {
                                    IntOffset(0, 0)
                                }
                            }
                            .background(
                                TooltipBackgroundDark,
                                RoundedCornerShape(12.dp)
                            )
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
                            Text(
                                text = "•",
                                color = GlowingPurple,
                                fontSize = 11.sp
                            )
                            Text(
                                text = formattedVal,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (percentageStr != null) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            GlowingPurple.copy(alpha = 0.25f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = percentageStr,
                                        color = GlowingPurple,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Y-Axis Labels Aligned on the RIGHT Side
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

        // X-Axis Date Labels Below Chart
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
                    color = if (index == selectedIndex) GlowingPurple else textColor,
                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
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

private fun formatFinancialAmount(amount: Double, currencySymbol: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "$currencySymbol${formatter.format(amount)}"
}

private fun formatAxisAmount(amount: Double, currencySymbol: String): String {
    val absAmt = abs(amount)
    return when {
        absAmt >= 1_000_000 -> "$currencySymbol${String.format(Locale.getDefault(), "%.1fM", amount / 1_000_000.0)}"
        absAmt >= 1000 -> "$currencySymbol${String.format(Locale.getDefault(), "%.1fk", amount / 1000.0)}"
        else -> "$currencySymbol${amount.toInt()}"
    }
}

