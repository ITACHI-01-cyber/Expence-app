package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.model.TransactionEntity
import com.example.network.ApiResponse
import com.example.network.BackendApiService
import com.example.network.ChangeUsernameByOtpRequest
import com.example.network.ChangeUsernameByPasswordRequest
import com.example.network.ForgotPasswordRequest
import com.example.network.ForgotUsernameRequest
import com.example.network.LoginData
import com.example.network.LoginRequest
import com.example.network.RegisterRequest
import com.example.network.ResetPasswordRequest
import com.example.network.ServerBudget
import com.example.network.ServerDashboardSummary
import com.example.network.ServerGoal
import com.example.network.ServerTransaction
import com.example.network.ServerWallet
import com.example.network.UserProfile
import com.example.network.UserSettingsRequest
import com.example.network.VerifyOtpRequest
import com.example.network.VerifyLoginOtpRequest
import com.example.repository.ExpenseRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackendCrudTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: ExpenseRepository

    private val mockApiService = object : BackendApiService {
        override suspend fun login(request: LoginRequest) =
            Response.success(ApiResponse<LoginData>(success = true, data = LoginData(token = "test-token")))

        override suspend fun sendRegistrationOtp(request: RegisterRequest) =
            Response.success(ApiResponse<Unit>(success = true))

        override suspend fun verifyRegistrationOtp(request: VerifyOtpRequest) =
            Response.success(ApiResponse<LoginData>(success = true, data = LoginData(token = "test-token")))

        override suspend fun verifyLoginOtp(request: VerifyLoginOtpRequest) =
            Response.success(ApiResponse<LoginData>(success = true, data = LoginData(token = "test-token")))

        override suspend fun requestPasswordReset(request: ForgotPasswordRequest) =
            Response.success(ApiResponse<Unit>(success = true))

        override suspend fun resetPassword(request: ResetPasswordRequest) =
            Response.success(ApiResponse<Unit>(success = true))

        override suspend fun requestUsernameRecovery(request: ForgotUsernameRequest) =
            Response.success(ApiResponse<Unit>(success = true))

        override suspend fun changeUsernameByOtp(request: ChangeUsernameByOtpRequest) =
            Response.success(ApiResponse<Unit>(success = true))

        override suspend fun changeUsernameByPassword(request: ChangeUsernameByPasswordRequest) =
            Response.success(ApiResponse<Unit>(success = true))

        override suspend fun getTransactions(
            month: Int?,
            year: Int?,
            startDate: String?,
            endDate: String?
        ) = Response.success(ApiResponse(success = true, data = emptyList<ServerTransaction>()))

        override suspend fun addTransaction(transaction: ServerTransaction) =
            Response.success(
                ApiResponse(
                    success = true,
                    data = transaction.copy(id = "server-transaction-id")
                )
            )

        override suspend fun updateTransaction(id: String, transaction: ServerTransaction) =
            Response.success(
                ApiResponse(
                    success = true,
                    data = transaction.copy(id = id)
                )
            )

        override suspend fun deleteTransaction(id: String) =
            Response.success(ApiResponse<Unit>(success = true))

        override suspend fun getWallets() =
            Response.success(ApiResponse(success = true, data = emptyList<ServerWallet>()))

        override suspend fun addWallet(wallet: ServerWallet) =
            Response.success(ApiResponse(success = true, data = wallet.copy(id = "server-wallet-id")))

        override suspend fun updateWallet(id: String, wallet: ServerWallet) =
            Response.success(ApiResponse(success = true, data = wallet.copy(id = id)))

        override suspend fun addMoneyToWallet(id: String, amount: Double) =
            Response.success(
                ApiResponse(
                    success = true,
                    data = ServerWallet(
                        id = id,
                        balance = amount
                    )
                )
            )

        override suspend fun deleteWallet(id: String) =
            Response.success(ApiResponse<Unit>(success = true))

        override suspend fun saveBudget(budget: ServerBudget) =
            Response.success(ApiResponse(success = true, data = budget))

        override suspend fun getGoals(month: Int, year: Int) =
            Response.success(ApiResponse(success = true, data = emptyList<ServerGoal>()))

        override suspend fun addGoal(goal: ServerGoal) =
            Response.success(ApiResponse(success = true, data = goal.copy(id = "server-goal-id")))

        override suspend fun updateGoal(id: String, goal: ServerGoal) =
            Response.success(ApiResponse(success = true, data = goal.copy(id = id)))

        override suspend fun updateGoalStatus(id: String, completed: Boolean) =
            Response.success(
                ApiResponse(
                    success = true,
                    data = ServerGoal(id = id, completed = completed)
                )
            )

        override suspend fun deleteGoal(id: String) =
            Response.success(ApiResponse<Unit>(success = true))

        override suspend fun getDashboardSummary() =
            Response.success(
                ApiResponse(
                    success = true,
                    data = ServerDashboardSummary(
                        availableBalance = 0.0,
                        monthlyIncome = 0.0,
                        monthlyBudgetLimit = 0.0,
                        monthlySpent = 0.0
                    )
                )
            )

        override suspend fun updateUserSettings(settings: UserSettingsRequest) =
            Response.success(
                ApiResponse(
                    success = true,
                    data = UserProfile(name = settings.name)
                )
            )
    }

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ExpenseRepository(db.appDao(), mockApiService)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `created transaction is cached only after backend returns its record`() = runBlocking {
        val transaction = TransactionEntity(
            id = "local-id",
            type = "expense",
            amount = 150.0,
            category = "Dining",
            description = "Lunch with team",
            date = "2026-09-27 12:30"
        )

        repository.addTransaction(transaction)

        val cached = db.appDao().getAllTransactionsList()
        assertEquals(1, cached.size)
        assertEquals("server-transaction-id", cached.single().id)
        assertEquals("Lunch with team", cached.single().description)
        assertEquals(150.0, cached.single().amount, 0.001)
    }

    @Test
    fun `updated transaction is cached from backend response`() = runBlocking {
        val original = TransactionEntity(
            id = "server-tx-id",
            type = "expense",
            amount = 200.0,
            category = "Transport",
            description = "Taxi ride",
            date = "2026-09-27 10:00"
        )
        db.appDao().insertTransaction(original)

        repository.updateTransaction(original.copy(amount = 250.0, description = "Airport Taxi"))

        val cached = db.appDao().getTransactionById("server-tx-id")
        assertNotNull(cached)
        assertEquals(250.0, cached!!.amount, 0.001)
        assertEquals("Airport Taxi", cached.description)
    }

    @Test
    fun `failed backend transaction response does not create a local record`() = runBlocking {
        val failingApi = object : BackendApiService by mockApiService {
            override suspend fun addTransaction(transaction: ServerTransaction) =
                Response.success(ApiResponse<ServerTransaction>(success = false, message = "Rejected"))
        }
        val failingRepository = ExpenseRepository(db.appDao(), failingApi)

        val result = runCatching {
            failingRepository.addTransaction(
                TransactionEntity(
                    id = "local-id",
                    type = "expense",
                    amount = 150.0,
                    category = "Dining",
                    description = "Lunch",
                    date = "2026-09-27 12:30"
                )
            )
        }

        assertTrue(result.isFailure)
        assertTrue(db.appDao().getAllTransactionsList().isEmpty())
    }
}
