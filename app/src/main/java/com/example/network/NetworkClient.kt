package com.example.network

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface BackendApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginData>>

    @POST("auth/register/send-otp")
    suspend fun sendRegistrationOtp(@Body request: RegisterRequest): Response<ApiResponse<Unit>>

    @POST("auth/register/verify-otp")
    suspend fun verifyRegistrationOtp(@Body request: VerifyOtpRequest): Response<ApiResponse<LoginData>>

    @POST("auth/register/verify-otp")
    suspend fun verifyLoginOtp(@Body request: VerifyLoginOtpRequest): Response<ApiResponse<LoginData>>

    @POST("auth/forgot-password/request")
    suspend fun requestPasswordReset(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>

    @POST("auth/forgot-password/reset")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Unit>>

    @POST("auth/forgot-username/request")
    suspend fun requestUsernameRecovery(
        @Body request: ForgotUsernameRequest
    ): Response<ApiResponse<Unit>>

    @POST("auth/forgot-username/change-via-otp")
    suspend fun changeUsernameByOtp(
        @Body request: ChangeUsernameByOtpRequest
    ): Response<ApiResponse<Unit>>

    @POST("auth/forgot-username/change-via-password")
    suspend fun changeUsernameByPassword(
        @Body request: ChangeUsernameByPasswordRequest
    ): Response<ApiResponse<Unit>>

    @GET("transactions")
    suspend fun getTransactions(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ApiResponse<List<ServerTransaction>>>

    @POST("transactions")
    suspend fun addTransaction(
        @Body transaction: ServerTransaction
    ): Response<ApiResponse<ServerTransaction>>

    @PUT("transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body transaction: ServerTransaction
    ): Response<ApiResponse<ServerTransaction>>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    @GET("wallet")
    suspend fun getWallets(): Response<ApiResponse<List<ServerWallet>>>

    @POST("wallet")
    suspend fun addWallet(
        @Body wallet: ServerWallet
    ): Response<ApiResponse<ServerWallet>>

    @PUT("wallet/{id}")
    suspend fun updateWallet(
        @Path("id") id: String,
        @Body wallet: ServerWallet
    ): Response<ApiResponse<ServerWallet>>

    @PATCH("wallet/{id}/add-money")
    suspend fun addMoneyToWallet(
        @Path("id") id: String,
        @Query("amount") amount: Double
    ): Response<ApiResponse<ServerWallet>>

    @DELETE("wallet/{id}")
    suspend fun deleteWallet(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    @POST("budget")
    suspend fun saveBudget(
        @Body budget: ServerBudget
    ): Response<ApiResponse<ServerBudget>>

    @GET("goals")
    suspend fun getGoals(
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<ApiResponse<List<ServerGoal>>>

    @POST("goals")
    suspend fun addGoal(
        @Body goal: ServerGoal
    ): Response<ApiResponse<ServerGoal>>

    @PUT("goals/{id}")
    suspend fun updateGoal(
        @Path("id") id: String,
        @Body goal: ServerGoal
    ): Response<ApiResponse<ServerGoal>>

    @PATCH("goals/{id}/status")
    suspend fun updateGoalStatus(
        @Path("id") id: String,
        @Query("completed") completed: Boolean
    ): Response<ApiResponse<ServerGoal>>

    @DELETE("goals/{id}")
    suspend fun deleteGoal(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(): Response<ApiResponse<ServerDashboardSummary>>

    @PUT("users/settings")
    suspend fun updateUserSettings(
        @Body settings: UserSettingsRequest
    ): Response<ApiResponse<UserProfile>>
}

object NetworkClient {
    const val BASE_URL = "https://expencetrack.onrender.com/api/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")

            TokenManager.getToken()
                ?.takeIf(String::isNotBlank)
                ?.let { token ->
                    val bearerToken =
                        if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
                    requestBuilder.header("Authorization", bearerToken)
                }

            chain.proceed(requestBuilder.build())
        }
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val apiService: BackendApiService = retrofit.create(BackendApiService::class.java)
}
