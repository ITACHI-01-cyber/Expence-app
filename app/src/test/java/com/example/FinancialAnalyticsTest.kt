package com.example

import com.example.analytics.calculateFinancialAnalytics
import com.example.analytics.AnalyticsPeriod
import com.example.analytics.resolveAnalyticsDateRange
import com.example.model.TransactionEntity
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialAnalyticsTest {
    private val today = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.OCTOBER, 10, 15, 30)
    }

    @Test
    fun `aggregates same-day expenses and income separately and fills empty days`() {
        val transactions = listOf(
            transaction("expense", 40.0, day = 10, hour = 9),
            transaction("expense", 25.0, day = 10, hour = 21),
            transaction("income", 200.0, day = 10, hour = 12),
            transaction("expense", 12.0, day = 8, hour = 18)
        )

        val result = calculateFinancialAnalytics(transactions, 7, today)

        assertEquals(7, result.points.size)
        assertEquals(77.0, result.totalExpenses, 0.001)
        assertEquals(200.0, result.totalIncome, 0.001)
        assertEquals(123.0, result.netCashFlow, 0.001)
        assertEquals(65.0, result.points.last().expenses, 0.001)
        assertEquals(200.0, result.points.last().income, 0.001)
        assertEquals(4, result.transactionCount)
        assertEquals(1, result.categories.size)
        assertEquals("Test", result.categories.single().category)
        assertEquals(100.0, result.categories.single().percentage, 0.001)
        assertEquals(0.0, result.points[0].expenses, 0.001)
        assertEquals(0.0, result.points[0].income, 0.001)
    }

    @Test
    fun `period lengths include today and exclude transactions outside selected dates`() {
        val transactions = listOf(
            transaction("expense", 1.0, day = 10, hour = 1),
            transaction("income", 2.0, day = 9, hour = 1),
            transaction("expense", 4.0, day = 3, hour = 1),
            transaction("income", 8.0, day = 20, month = Calendar.SEPTEMBER, year = 2026)
        )

        val sevenDays = calculateFinancialAnalytics(transactions, 7, today)
        val twelveDays = calculateFinancialAnalytics(transactions, 12, today)
        val thirtyDays = calculateFinancialAnalytics(transactions, 30, today)

        assertEquals(7, sevenDays.points.size)
        assertEquals(1.0, sevenDays.totalExpenses, 0.001)
        assertEquals(2.0, sevenDays.totalIncome, 0.001)
        assertEquals(12, twelveDays.points.size)
        assertEquals(5.0, twelveDays.totalExpenses, 0.001)
        assertEquals(2.0, twelveDays.totalIncome, 0.001)
        assertEquals(30, thirtyDays.points.size)
        assertEquals(5.0, thirtyDays.totalExpenses, 0.001)
        assertEquals(10.0, thirtyDays.totalIncome, 0.001)
        assertEquals(4, thirtyDays.transactionCount)
    }

    @Test
    fun `custom range includes both local boundary dates and calculates category shares`() {
        val transactions = listOf(
            transaction("expense", 60.0, day = 8, hour = 23, category = "Food"),
            transaction("expense", 30.0, day = 9, hour = 0, category = "Food"),
            transaction("expense", 10.0, day = 10, hour = 23, category = "Travel"),
            transaction("income", 250.0, day = 11, hour = 0, category = "Salary")
        )
        val start = localDay(year = 2026, month = Calendar.OCTOBER, day = 9)
        val end = localDay(year = 2026, month = Calendar.OCTOBER, day = 10)

        val result = calculateFinancialAnalytics(transactions, start, end)

        assertEquals(2, result.points.size)
        assertEquals(40.0, result.totalExpenses, 0.001)
        assertEquals(0.0, result.totalIncome, 0.001)
        assertEquals(2, result.transactionCount)
        assertEquals(75.0, result.categories.first { it.category == "Food" }.percentage, 0.001)
        assertEquals(25.0, result.categories.first { it.category == "Travel" }.percentage, 0.001)
    }

    @Test
    fun `preset ranges resolve to inclusive local calendar periods`() {
        val sevenDays = resolveAnalyticsDateRange(AnalyticsPeriod.LAST_7_DAYS, today)
        val thirtyDays = resolveAnalyticsDateRange(AnalyticsPeriod.LAST_30_DAYS, today)
        val sixMonths = resolveAnalyticsDateRange(AnalyticsPeriod.LAST_6_MONTHS, today)

        assertEquals(7, calculateFinancialAnalytics(emptyList(), sevenDays.startDateMillis, sevenDays.endDateMillis).points.size)
        assertEquals(30, calculateFinancialAnalytics(emptyList(), thirtyDays.startDateMillis, thirtyDays.endDateMillis).points.size)
        assertEquals(Calendar.MAY, Calendar.getInstance().apply {
            timeInMillis = sixMonths.startDateMillis
        }.get(Calendar.MONTH))
        assertEquals(1, Calendar.getInstance().apply {
            timeInMillis = sixMonths.startDateMillis
        }.get(Calendar.DAY_OF_MONTH))
        assertEquals(163, calculateFinancialAnalytics(emptyList(), sixMonths.startDateMillis, sixMonths.endDateMillis).points.size)
    }

    @Test
    fun `recurring list includes only in-range expenses marked recurring`() {
        val transactions = listOf(
            transaction("expense", 75.0, day = 10, recurring = true),
            transaction("income", 500.0, day = 10, recurring = true),
            transaction("expense", 25.0, day = 9),
            transaction("expense", 100.0, day = 3, recurring = true)
        )

        val result = calculateFinancialAnalytics(transactions, 7, today)

        assertEquals(1, result.recurringTransactions.size)
        assertEquals(75.0, result.recurringTransactions.single().amount, 0.001)
    }

    @Test
    fun `unknown transaction types and invalid amounts are ignored`() {
        val transactions = listOf(
            transaction("transfer", 500.0, day = 10),
            transaction("expense", Double.NaN, day = 10),
            transaction("income", -1.0, day = 10),
            transaction("income", 0.0, day = 10)
        )

        val result = calculateFinancialAnalytics(transactions, 7, today)

        assertTrue(result.points.all { it.expenses == 0.0 && it.income == 0.0 })
        assertEquals(0.0, result.totalExpenses, 0.001)
        assertEquals(0.0, result.totalIncome, 0.001)
        assertEquals(1, result.transactionCount)
        val emptyResult = calculateFinancialAnalytics(emptyList(), 7, today)
        assertEquals(0, emptyResult.transactionCount)
        assertTrue(emptyResult.points.all {
            it.expenses == 0.0 && it.income == 0.0
        })
    }

    @Test
    fun `credit refunds reduce expenses without being counted as income`() {
        val purchase = transaction("expense", 100.0, day = 10, category = "Travel")
            .copy(transactionKind = "purchase")
        val refund = transaction("expense", -35.0, day = 10, category = "Travel")
            .copy(transactionKind = "refund", relatedTransactionId = "purchase-id")

        val result = calculateFinancialAnalytics(listOf(purchase, refund), 7, today)

        assertEquals(65.0, result.totalExpenses, 0.001)
        assertEquals(0.0, result.totalIncome, 0.001)
        assertEquals(-65.0, result.netCashFlow, 0.001)
        assertEquals(65.0, result.categories.single().amount, 0.001)
        assertEquals(2, result.transactionCount)
    }

    private fun transaction(
        type: String,
        amount: Double,
        day: Int,
        hour: Int = 12,
        category: String = "Test",
        recurring: Boolean = false,
        month: Int = Calendar.OCTOBER,
        year: Int = 2026
    ) = TransactionEntity(
        type = type,
        amount = amount,
        category = category,
        description = "Analytics test",
        isRecurring = recurring,
        date = "test",
        timestamp = Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, 0)
        }.timeInMillis
    )

    private fun localDay(year: Int, month: Int, day: Int) = Calendar.getInstance().apply {
        clear()
        set(year, month, day)
    }.timeInMillis
}
