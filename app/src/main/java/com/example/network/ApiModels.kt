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
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class ForgotPasswordRequest(
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class ForgotUsernameRequest(
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class ChangeUsernameByOtpRequest(
    @Json(name = "email") val email: String,
    @Json(name = "code") val code: String,
    @Json(name = "newUsername") val newUsername: String
)

@JsonClass(generateAdapter = true)
data class ChangeUsernameByPasswordRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "newUsername") val newUsername: String
)

@JsonClass(generateAdapter = true)
data class ResetPasswordRequest(
    @Json(name = "email") val email: String,
    @Json(name = "code") val code: String,
    @Json(name = "newPassword") val newPassword: String
)

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean = false,
    @Json(name = "status") val status: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null
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
    @Json(name = "email") val email: String? = null,
    @Json(name = "profilePicture") val profilePicture: String? = null
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

fun formatToIsoLocalDateTime(dateStr: String?, timestamp: Long = System.currentTimeMillis()): String {
    if (dateStr != null && dateStr.contains("T") && dateStr.length >= 19) {
        return dateStr.substring(0, 19)
    }
    return try {
        val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        if (dateStr.isNullOrBlank()) {
            isoFormat.format(java.util.Date(timestamp))
        } else if (dateStr.contains(" ") && dateStr.length >= 16) {
            val spaceFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
            val d = spaceFormat.parse(dateStr)
            isoFormat.format(d ?: java.util.Date(timestamp))
        } else if (dateStr.length == 10 && dateStr.contains("-")) {
            val dayFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val d = dayFormat.parse(dateStr)
            isoFormat.format(d ?: java.util.Date(timestamp))
        } else {
            isoFormat.format(java.util.Date(timestamp))
        }
    } catch (e: Exception) {
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(java.util.Date(timestamp))
    }
}

fun formatDisplayDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    return dateStr.replace("T", " ").take(16)
}

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
    @Json(name = "cardIcon") val cardIcon: String? = null,
    @Json(name = "backSignatureText") val backSignatureText: String? = null,
    @Json(name = "backContactInfo") val backContactInfo: String? = null
)

@JsonClass(generateAdapter = true)
data class CardCustomizationRequest(
    @Json(name = "cardTheme") val cardTheme: String,
    @Json(name = "primaryColor") val primaryColor: String,
    @Json(name = "secondaryColor") val secondaryColor: String,
    @Json(name = "accentColor") val accentColor: String,
    @Json(name = "artwork") val artwork: String,
    @Json(name = "cardStyle") val cardStyle: String
)

@JsonClass(generateAdapter = true)
data class VerifyOtpRequest(
    @Json(name = "email") val email: String,
    @Json(name = "code") val code: String
)

@JsonClass(generateAdapter = true)
data class VerifyLoginOtpRequest(
    @Json(name = "otp") val otp: String,
    @Json(name = "email") val email: String
)

@JsonClass(generateAdapter = true)
data class UserSettingsRequest(
    @Json(name = "name") val name: String? = null,
    @Json(name = "profilePicture") val profilePicture: String? = null,
    @Json(name = "accentColor") val accentColor: String? = null,
    @Json(name = "currency") val currency: String? = null,
    @Json(name = "gmailConnected") val gmailConnected: Boolean? = null
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
