package com.example.analytics

import com.example.model.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DailyFinancialPoint(
    val startOfDayMillis: Long,
    val dateLabel: String,
    val expenses: Double,
    val income: Double
)

data class FinancialAnalytics(
    val points: List<DailyFinancialPoint>,
    val totalExpenses: Double,
    val totalIncome: Double,
    val transactionCount: Int,
    val categories: List<FinancialCategoryTotal>,
    val recentTransactions: List<TransactionEntity>,
    val recurringTransactions: List<TransactionEntity>
) {
    val netCashFlow: Double
        get() = totalIncome - totalExpenses
}

data class FinancialCategoryTotal(
    val category: String,
    val amount: Double,
    val percentage: Double
)

enum class AnalyticsPeriod {
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_6_MONTHS,
    CUSTOM
}

data class AnalyticsDateRange(
    val startDateMillis: Long,
    val endDateMillis: Long
)

fun resolveAnalyticsDateRange(
    period: AnalyticsPeriod,
    today: Calendar = Calendar.getInstance(),
    customStartMillis: Long? = null,
    customEndMillis: Long? = null
): AnalyticsDateRange {
    val endDay = localStartOfDay(today)
    val startDay = endDay.clone() as Calendar
    when (period) {
        AnalyticsPeriod.LAST_7_DAYS -> startDay.add(Calendar.DAY_OF_YEAR, -6)
        AnalyticsPeriod.LAST_30_DAYS -> startDay.add(Calendar.DAY_OF_YEAR, -29)
        AnalyticsPeriod.LAST_6_MONTHS -> {
            startDay.set(Calendar.DAY_OF_MONTH, 1)
            startDay.add(Calendar.MONTH, -5)
        }
        AnalyticsPeriod.CUSTOM -> {
            val customStart = requireNotNull(customStartMillis) { "A custom start date is required." }
            val customEnd = requireNotNull(customEndMillis) { "A custom end date is required." }
            val normalizedStart = localStartOfDay(Calendar.getInstance().apply { timeInMillis = customStart })
            val normalizedEnd = localStartOfDay(Calendar.getInstance().apply { timeInMillis = customEnd })
            require(normalizedStart.timeInMillis <= normalizedEnd.timeInMillis) {
                "The custom start date must not be after its end date."
            }
            return AnalyticsDateRange(normalizedStart.timeInMillis, normalizedEnd.timeInMillis)
        }
    }
    return AnalyticsDateRange(startDay.timeInMillis, endDay.timeInMillis)
}

fun calculateFinancialAnalytics(
    transactions: List<TransactionEntity>,
    numberOfDays: Int,
    today: Calendar = Calendar.getInstance()
): FinancialAnalytics {
    require(numberOfDays > 0) { "The analytics period must contain at least one day." }

    val endDay = (today.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startDay = (endDay.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, -(numberOfDays - 1))
    }
    return calculateFinancialAnalytics(
        transactions,
        startDay.timeInMillis,
        endDay.timeInMillis
    )
}

fun calculateFinancialAnalytics(
    transactions: List<TransactionEntity>,
    startDateMillis: Long,
    endDateMillis: Long
): FinancialAnalytics {
    val firstDay = localStartOfDay(Calendar.getInstance().apply { timeInMillis = startDateMillis })
    val lastDayInclusive = localStartOfDay(Calendar.getInstance().apply { timeInMillis = endDateMillis })
    require(firstDay.timeInMillis <= lastDayInclusive.timeInMillis) {
        "The analytics start date must not be after its end date."
    }

    val dayTotals = mutableMapOf<Long, Pair<Double, Double>>()
    val firstDayMillis = firstDay.timeInMillis
    val lastDayExclusive = (lastDayInclusive.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, 1)
    }
    var numberOfDays = 0
    val dayCounter = firstDay.clone() as Calendar
    while (dayCounter.timeInMillis <= lastDayInclusive.timeInMillis) {
        numberOfDays++
        dayCounter.add(Calendar.DAY_OF_YEAR, 1)
    }
    val dayCalendar = Calendar.getInstance()
    val labelFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    val categoryTotals = mutableMapOf<String, Double>()
    val inRangeTransactions = mutableListOf<TransactionEntity>()

    transactions.forEach { transaction ->
        val isRefund = transaction.transactionKind.equals("refund", ignoreCase = true)
        if (!transaction.amount.isFinite() || (transaction.amount < 0.0 && !isRefund)) return@forEach
        if (isRefund && (!transaction.type.equals("expense", ignoreCase = true) || transaction.amount > 0.0)) {
            return@forEach
        }
        if (!transaction.type.equals("expense", ignoreCase = true) &&
            !transaction.type.equals("income", ignoreCase = true)
        ) {
            return@forEach
        }

        dayCalendar.timeInMillis = transaction.timestamp
        dayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        dayCalendar.set(Calendar.MINUTE, 0)
        dayCalendar.set(Calendar.SECOND, 0)
        dayCalendar.set(Calendar.MILLISECOND, 0)
        val dayMillis = dayCalendar.timeInMillis
        if (dayMillis < firstDayMillis || dayMillis >= lastDayExclusive.timeInMillis) return@forEach

        val (expenses, income) = dayTotals[dayMillis] ?: (0.0 to 0.0)
        dayTotals[dayMillis] = if (transaction.type.equals("expense", ignoreCase = true)) {
            (expenses + transaction.amount) to income
        } else {
            expenses to (income + transaction.amount)
        }
        inRangeTransactions += transaction
        if (transaction.type.equals("expense", ignoreCase = true)) {
            val category = transaction.category.trim().ifEmpty { "Uncategorized" }
            categoryTotals[category] = (categoryTotals[category] ?: 0.0) + transaction.amount
        }
    }

    val points = buildList(numberOfDays) {
        val currentDay = firstDay.clone() as Calendar
        repeat(numberOfDays) {
            val startOfDay = currentDay.timeInMillis
            val (expenses, income) = dayTotals[startOfDay] ?: (0.0 to 0.0)
            add(
                DailyFinancialPoint(
                    startOfDayMillis = startOfDay,
                    dateLabel = labelFormat.format(currentDay.time),
                    expenses = expenses,
                    income = income
                )
            )
            currentDay.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    val totalExpenses = points.sumOf { it.expenses }
    return FinancialAnalytics(
        points = points,
        totalExpenses = totalExpenses,
        totalIncome = points.sumOf { it.income },
        transactionCount = inRangeTransactions.size,
        categories = categoryTotals.map { (category, amount) ->
            FinancialCategoryTotal(
                category = category,
                amount = amount,
                percentage = if (totalExpenses > 0.0) {
                    amount / totalExpenses * 100.0
                } else {
                    0.0
                }
            )
        }.sortedByDescending { it.amount },
        recentTransactions = inRangeTransactions.sortedByDescending { it.timestamp }.take(5),
        recurringTransactions = inRangeTransactions
            .filter { it.isRecurring && it.type.equals("expense", ignoreCase = true) }
            .sortedByDescending { it.timestamp }
    )
}

private fun localStartOfDay(calendar: Calendar): Calendar = (calendar.clone() as Calendar).apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}
