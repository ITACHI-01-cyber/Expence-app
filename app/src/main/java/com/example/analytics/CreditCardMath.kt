package com.example.analytics

import com.example.model.WalletEntity

data class CreditCardSummary(
    val availableCredit: Double,
    val utilizationPercent: Double,
    val creditBalance: Double
)

fun calculateCreditCardSummary(wallet: WalletEntity): CreditCardSummary? {
    if (!wallet.cardType.equals("credit", ignoreCase = true) ||
        !wallet.creditTermsConfigured
    ) {
        return null
    }

    val limit = wallet.creditLimit ?: return null
    val outstanding = wallet.outstandingBalance ?: return null
    if (!limit.isFinite() || limit <= 0.0 || !outstanding.isFinite()) {
        return null
    }

    return CreditCardSummary(
        availableCredit = limit - outstanding,
        utilizationPercent = outstanding.coerceAtLeast(0.0) / limit * 100.0,
        creditBalance = (-outstanding).coerceAtLeast(0.0)
    )
}
