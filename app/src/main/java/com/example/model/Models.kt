package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bankName: String,
    val cardType: String, // "debit", "credit", "upi", "cash"
    val cardBrand: String = "Visa", // "Visa", "Mastercard", "RuPay", "Amex", ""
    val cardNumber: String,
    val cardHolderName: String = "",
    val expiryDate: String = "",
    val balance: Double = 0.0,
    val primaryColor: String = "#1A1A2E",
    val secondaryColor: String = "#16213E",
    val designId: String = "midnight",
    val cardTheme: String = "midnight",
    val accentColor: String = "#38BDF8",
    val artwork: String = "waves",
    val cardStyle: String = "illustrated", // "classic", "premium", "gradient", "illustrated", "glass", "neon", "minimal", "dark"
    val layoutVariant: String = "classic",
    val textColor: String = "#FFFFFF",
    val cardIcon: String = "none",
    val backSignatureText: String = "Not Valid without Authorized Signature",
    val backContactInfo: String = "support@bank.com | 1-800-555-0199"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String, // "expense" or "income"
    val amount: Double,
    val category: String,
    val description: String,
    val date: String, // yyyy-MM-dd HH:mm
    val timestamp: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val walletId: String = ""
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val medium: String = "Bank Account",
    val completed: Boolean = false,
    val month: Int,
    val year: Int
)

@Entity(tableName = "budget_config")
data class BudgetConfigEntity(
    @PrimaryKey val id: Int = 1,
    val monthlyIncome: Double = 0.0,
    val budgetLimit: Double = 0.0,
    val month: Int = 0,
    val year: Int = 0,
    val availableBalance: Double? = null,
    val monthlySpent: Double? = null
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val email: String = "",
    val currency: String = "₹",
    val currencyCode: String = "INR",
    val theme: String = "dark", // "dark", "light", "system"
    val accentColor: String = "purple", // "purple", "blue", "emerald", "rose"
    val isGuest: Boolean = true,
    val gmailConnected: Boolean = false
)

data class DashboardSummary(
    val monthlyIncome: Double,
    val monthlyBudgetLimit: Double,
    val monthlySpent: Double,
    val availableBalance: Double
)

data class CategoryExpense(
    val category: String,
    val amount: Double,
    val percentage: Float = 0f
)

data class ApiTestCaseResult(
    val testName: String,
    val method: String, // "GET", "POST", "PUT", "DELETE"
    val endpoint: String,
    val statusCode: Int,
    val latencyMs: Long,
    val isSuccess: Boolean,
    val detailMessage: String
)
