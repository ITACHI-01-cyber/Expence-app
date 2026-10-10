package com.example

import com.example.analytics.calculateCreditCardSummary
import com.example.model.WalletEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CreditCardMathTest {
    @Test
    fun `available credit and utilization use configured limit and outstanding balance`() {
        val summary = requireNotNull(calculateCreditCardSummary(
            creditWallet(limit = 100_000.0, outstanding = 25_000.0)
        ))

        assertEquals(75_000.0, summary.availableCredit, 0.001)
        assertEquals(25.0, summary.utilizationPercent, 0.001)
    }

    @Test
    fun `zero balance has zero utilization and full credit available`() {
        val summary = requireNotNull(calculateCreditCardSummary(creditWallet(limit = 50_000.0, outstanding = 0.0)))

        assertEquals(50_000.0, summary.availableCredit, 0.001)
        assertEquals(0.0, summary.utilizationPercent, 0.001)
    }

    @Test
    fun `unconfigured or invalid terms do not infer credit from legacy wallet balance`() {
        assertNull(calculateCreditCardSummary(creditWallet(configured = false, limit = null, outstanding = null)))
        assertNull(calculateCreditCardSummary(creditWallet(limit = 0.0, outstanding = 0.0)))
    }

    @Test
    fun `refund credit balance increases available credit without negative utilization`() {
        val summary = requireNotNull(calculateCreditCardSummary(creditWallet(limit = 10_000.0, outstanding = -500.0)))

        assertEquals(10_500.0, summary.availableCredit, 0.001)
        assertEquals(0.0, summary.utilizationPercent, 0.001)
        assertEquals(500.0, summary.creditBalance, 0.001)
    }

    private fun creditWallet(
        configured: Boolean = true,
        limit: Double?,
        outstanding: Double?
    ) = WalletEntity(
        bankName = "Issuer",
        cardType = "credit",
        cardNumber = "1234",
        balance = 999.0,
        creditTermsConfigured = configured,
        creditLimit = limit,
        outstandingBalance = outstanding
    )
}
