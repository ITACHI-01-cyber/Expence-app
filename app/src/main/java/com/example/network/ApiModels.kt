package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "identifier") val identifier: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class ForgotPasswordRequest(
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class ResetPasswordRequest(
    @Json(name = "email") val email: String,
    @Json(name = "code") val code: String? = null,
    @Json(name = "token") val token: String? = null,
    @Json(name = "newPassword") val newPassword: String
)

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "status") val status: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null,
    @Json(name = "token") val token: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginData(
    @Json(name = "token") val token: String? = null,
    @Json(name = "theme") val theme: String? = null,
    @Json(name = "currency") val currency: String? = null,
    @Json(name = "gmailConnected") val gmailConnected: Boolean? = null,
    @Json(name = "accentColor") val accentColor: String? = null,
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val mongoId: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class UserProfile(
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val mongoId: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "email") val email: String? = null
)

@JsonClass(generateAdapter = true)
data class ServerTransaction(
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val mongoId: String? = null,
    @Json(name = "userId") val userId: String? = null,
    @Json(name = "type") val type: String = "expense",
    @Json(name = "category") val category: String = "Other",
    @Json(name = "amount") val amount: Double = 0.0,
    @Json(name = "description") val description: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "isRecurring") val isRecurring: Boolean = false,
    @Json(name = "walletId") val walletId: String? = null,
    @Json(name = "paymentMethod") val paymentMethod: String? = null
)

@JsonClass(generateAdapter = true)
data class ServerWallet(
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val mongoId: String? = null,
    @Json(name = "userId") val userId: String? = null,
    @Json(name = "cardNumber") val cardNumber: String = "",
    @Json(name = "cardType") val cardType: String = "debit",
    @Json(name = "cardBrand") val cardBrand: String? = null,
    @Json(name = "expiryDate") val expiryDate: String? = null,
    @Json(name = "cardHolderName") val cardHolderName: String = "",
    @Json(name = "balance") val balance: Double = 0.0,
    @Json(name = "bankName") val bankName: String = "Bank",
    @Json(name = "designPreset") val designPreset: String? = null,
    @Json(name = "primaryColor") val primaryColor: String? = null,
    @Json(name = "secondaryColor") val secondaryColor: String? = null,
    @Json(name = "textColor") val textColor: String? = null,
    @Json(name = "cardIcon") val cardIcon: String? = null
)

@JsonClass(generateAdapter = true)
data class TopUpWalletRequest(
    @Json(name = "amount") val amount: Double
)

@JsonClass(generateAdapter = true)
data class ServerBudget(
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val mongoId: String? = null,
    @Json(name = "monthlyIncome") val monthlyIncome: Double = 0.0,
    @Json(name = "budgetLimit") val budgetLimit: Double = 0.0,
    @Json(name = "month") val month: Int = 1,
    @Json(name = "year") val year: Int = 2026
)

@JsonClass(generateAdapter = true)
data class ServerGoal(
    @Json(name = "id") val id: String? = null,
    @Json(name = "_id") val mongoId: String? = null,
    @Json(name = "title") val title: String = "",
    @Json(name = "amount") val amount: Double = 0.0,
    @Json(name = "medium") val medium: String = "Bank Account",
    @Json(name = "completed") val completed: Boolean = false,
    @Json(name = "month") val month: Int = 1,
    @Json(name = "year") val year: Int = 2026
)

@JsonClass(generateAdapter = true)
data class ServerDashboardSummary(
    @Json(name = "availableBalance") val availableBalance: Double = 0.0,
    @Json(name = "monthlyIncome") val monthlyIncome: Double = 0.0,
    @Json(name = "monthlyBudgetLimit") val monthlyBudgetLimit: Double = 0.0,
    @Json(name = "monthlySpent") val monthlySpent: Double = 0.0
)
