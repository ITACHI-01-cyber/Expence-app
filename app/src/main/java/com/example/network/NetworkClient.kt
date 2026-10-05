package com.example.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface BackendApiService {

    // ── Auth Endpoints ──
    @GET("api/auth/health")
    suspend fun getHealth(): Response<ResponseBody>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginData>>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<LoginData>>

    @POST("api/auth/signup")
    suspend fun signup(
        @Body request: RegisterRequest
    ): Response<ApiResponse<LoginData>>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ApiResponse<Unit>>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<ApiResponse<Unit>>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<Unit>>

    @GET("api/auth/me")
    suspend fun getProfile(): Response<ApiResponse<UserProfile>>

    // ── Transactions Endpoints (Real backend route: /api/transactions) ──
    @GET("api/transactions")
    suspend fun getTransactions(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<ApiResponse<List<ServerTransaction>>>

    @POST("api/transactions")
    suspend fun addTransaction(
        @Body transaction: ServerTransaction
    ): Response<ApiResponse<ServerTransaction>>

    @PUT("api/transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body transaction: ServerTransaction
    ): Response<ApiResponse<ServerTransaction>>

    @DELETE("api/transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: String
    ): Response<ResponseBody>

    // ── Wallet Endpoints (Real backend route: /api/wallet) ──
    @GET("api/wallet")
    suspend fun getWallets(): Response<ApiResponse<List<ServerWallet>>>

    @POST("api/wallet")
    suspend fun addWallet(
        @Body wallet: ServerWallet
    ): Response<ApiResponse<ServerWallet>>

    @PUT("api/wallet/{id}")
    suspend fun updateWallet(
        @Path("id") id: String,
        @Body wallet: ServerWallet
    ): Response<ApiResponse<ServerWallet>>

    @PUT("api/wallet/{id}/customization")
    suspend fun updateCardCustomization(
        @Path("id") id: String,
        @Body customization: CardCustomizationRequest
    ): Response<ApiResponse<ServerWallet>>

    @DELETE("api/wallet/{id}")
    suspend fun deleteWallet(
        @Path("id") id: String
    ): Response<ResponseBody>

    @POST("api/wallet/{id}/topup")
    suspend fun topUpWallet(
        @Path("id") id: String,
        @Body request: TopUpWalletRequest
    ): Response<ApiResponse<ServerWallet>>

    // ── Budget & Goal Endpoints ──
    @GET("api/budget")
    suspend fun getBudget(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<ApiResponse<ServerBudget>>

    @POST("api/budget")
    suspend fun saveBudget(
        @Body budget: ServerBudget
    ): Response<ApiResponse<ServerBudget>>

    @GET("api/goals")
    suspend fun getGoals(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<ApiResponse<List<ServerGoal>>>

    @POST("api/goals")
    suspend fun addGoal(
        @Body goal: ServerGoal
    ): Response<ApiResponse<ServerGoal>>

    @DELETE("api/goals/{id}")
    suspend fun deleteGoal(
        @Path("id") id: String
    ): Response<ResponseBody>

    // ── Dashboard Summary Endpoint ──
    @GET("api/dashboard/summary")
    suspend fun getDashboardSummary(): Response<ApiResponse<ServerDashboardSummary>>
}

object NetworkClient {
    // The exact verified Render backend
    const val BASE_URL = "https://expencetrack.onrender.com/"
    const val WEB_ORIGIN = "https://expence-track-nu.vercel.app"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        // Automatic JWT Token & Header Attachment Interceptor
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Origin", WEB_ORIGIN)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")

            // Attach JWT Bearer Token if user is logged in
            val token = TokenManager.getToken()
            if (!token.isNullOrBlank() && original.header("Authorization") == null) {
                val bearerHeader = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
                requestBuilder.header("Authorization", bearerHeader)
            }

            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(loggingInterceptor)
        // Generous timeouts to comfortably accommodate Render cold boot
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: BackendApiService = retrofit.create(BackendApiService::class.java)
}
