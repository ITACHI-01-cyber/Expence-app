package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.model.CardDisplayBalanceEntity
import com.example.model.TransactionEntity
import com.example.model.WalletEntity
import com.example.network.ApiActionResponse
import com.example.network.ApiResponse
import com.example.network.BackendApiService
import com.example.network.CreditActivityItem
import com.example.network.CreditRefundRequest
import com.example.network.CreditRepaymentRequest
import com.example.network.CreditRepaymentResponse
import com.example.network.CreditStatement
import com.example.network.CreditStatementRequest
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
import com.example.ui.components.parseCardBalanceInput
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
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
    private var deletedTransactionId: String? = null
    private var updatedTransactionId: String? = null
    private var updatedTransactionBody: ServerTransaction? = null

    private val mockApiService = object : BackendApiService {
        override suspend fun login(request: LoginRequest) =
            Response.success(ApiResponse<LoginData>(success = true, data = LoginData(token = "test-token")))

        override suspend fun sendRegistrationOtp(request: RegisterRequest) =
            Response.success(ApiActionResponse(success = true))

        override suspend fun verifyRegistrationOtp(request: VerifyOtpRequest) =
            Response.success(ApiResponse<LoginData>(success = true, data = LoginData(token = "test-token")))

        override suspend fun verifyLoginOtp(request: VerifyLoginOtpRequest) =
            Response.success(ApiResponse<LoginData>(success = true, data = LoginData(token = "test-token")))

        override suspend fun requestPasswordReset(request: ForgotPasswordRequest) =
            Response.success(ApiActionResponse(success = true))

        override suspend fun resetPassword(request: ResetPasswordRequest) =
            Response.success(ApiActionResponse(success = true))

        override suspend fun requestUsernameRecovery(request: ForgotUsernameRequest) =
            Response.success(ApiActionResponse(success = true))

        override suspend fun changeUsernameByOtp(request: ChangeUsernameByOtpRequest) =
            Response.success(ApiActionResponse(success = true))

        override suspend fun changeUsernameByPassword(request: ChangeUsernameByPasswordRequest) =
            Response.success(ApiActionResponse(success = true))

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

        override suspend fun updateTransaction(id: String, transaction: ServerTransaction): Response<ApiResponse<ServerTransaction>> {
            updatedTransactionId = id
            updatedTransactionBody = transaction
            return Response.success(
                ApiResponse(success = true, data = transaction.copy(id = id))
            )
        }

        override suspend fun deleteTransaction(id: String): Response<ApiActionResponse> {
            deletedTransactionId = id
            return Response.success(ApiActionResponse(success = true))
        }

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
            Response.success(ApiActionResponse(success = true))

        override suspend fun recordCreditCardRepayment(
            cardId: String,
            request: CreditRepaymentRequest
        ) = Response.success(
            ApiResponse(
                success = true,
                data = CreditRepaymentResponse(
                    id = "repayment-id",
                    walletId = cardId,
                    sourceWalletId = request.sourceWalletId,
                    statementId = request.statementId,
                    amount = request.amount,
                    date = request.date ?: "2026-10-10T12:30:00",
                    transactionKind = "repayment",
                    idempotencyKey = request.idempotencyKey,
                    outstandingBalance = 0.0,
                    sourceWalletBalance = 0.0,
                    replayed = false
                )
            )
        )

        override suspend fun getCreditCardStatements(cardId: String) =
            Response.success(ApiResponse(success = true, data = emptyList<CreditStatement>()))

        override suspend fun generateCreditCardStatement(cardId: String, request: CreditStatementRequest) =
            Response.success(
                ApiResponse(
                    success = true,
                    data = CreditStatement(
                        id = "statement-id",
                        userId = "user-id",
                        walletId = cardId,
                        periodStart = "2026-09-01",
                        periodEnd = "2026-09-30",
                        generatedAt = "2026-10-01T00:00:00",
                        timezone = "Asia/Kolkata",
                        statementClosingDay = 30,
                        dueDate = "2026-10-21",
                        statementBalance = 0.0
                    )
                )
            )

        override suspend fun getCreditCardActivity(cardId: String) =
            Response.success(ApiResponse(success = true, data = emptyList<CreditActivityItem>()))

        override suspend fun recordCreditRefund(
            purchaseId: String,
            request: CreditRefundRequest
        ) = Response.success(
            ApiResponse(
                success = true,
                data = ServerTransaction(
                    id = "refund-id",
                    type = "expense",
                    amount = -request.amount,
                    transactionKind = "refund",
                    idempotencyKey = request.idempotencyKey,
                    relatedTransactionId = purchaseId
                )
            )
        )

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
            Response.success(ApiActionResponse(success = true))

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
        assertEquals("server-tx-id", updatedTransactionId)
        assertEquals(250.0, updatedTransactionBody?.amount ?: 0.0, 0.001)
        assertEquals("Airport Taxi", updatedTransactionBody?.description)
        assertEquals(null, updatedTransactionBody?.id)
        assertEquals("2026-09-27T10:00", updatedTransactionBody?.date)
    }

    @Test
    fun `rejected transaction update keeps cached record unchanged`() = runBlocking {
        val original = TransactionEntity(
            id = "server-tx-id",
            type = "expense",
            amount = 200.0,
            category = "Transport",
            description = "Taxi ride",
            date = "2026-09-27 10:00"
        )
        db.appDao().insertTransaction(original)
        val failingApi = object : BackendApiService by mockApiService {
            override suspend fun updateTransaction(id: String, transaction: ServerTransaction) =
                Response.success(
                    ApiResponse<ServerTransaction>(success = false, message = "Update failed")
                )
        }

        val result = runCatching {
            ExpenseRepository(db.appDao(), failingApi).updateTransaction(
                original.copy(amount = 250.0, description = "Airport Taxi")
            )
        }

        assertTrue(result.isFailure)
        assertEquals(original, db.appDao().getTransactionById(original.id))
    }

    @Test
    fun `deleted transaction is removed from cache only after backend confirmation`() = runBlocking {
        val transaction = TransactionEntity(
            id = "mongo-transaction-id",
            type = "expense",
            amount = 80.0,
            category = "Food & Dining",
            description = "Coffee",
            date = "2026-09-27 10:00"
        )
        db.appDao().insertTransaction(transaction)

        repository.deleteTransaction(transaction.id)

        assertEquals(transaction.id, deletedTransactionId)
        assertEquals(null, db.appDao().getTransactionById(transaction.id))
    }

    @Test
    fun `failed transaction delete keeps cached record`() = runBlocking {
        val transaction = TransactionEntity(
            id = "server-tx-id",
            type = "expense",
            amount = 80.0,
            category = "Food & Dining",
            description = "Coffee",
            date = "2026-09-27 10:00"
        )
        db.appDao().insertTransaction(transaction)
        val failingApi = object : BackendApiService by mockApiService {
            override suspend fun deleteTransaction(id: String) =
                Response.success(ApiActionResponse(success = false, message = "Delete failed"))
        }

        val result = runCatching {
            ExpenseRepository(db.appDao(), failingApi).deleteTransaction(transaction.id)
        }

        assertTrue(result.isFailure)
        assertEquals(transaction, db.appDao().getTransactionById(transaction.id))
    }

    @Test
    fun `http error transaction delete keeps cached record`() = runBlocking {
        val transaction = TransactionEntity(
            id = "server-tx-id",
            type = "expense",
            amount = 80.0,
            category = "Food & Dining",
            description = "Coffee",
            date = "2026-09-27 10:00"
        )
        db.appDao().insertTransaction(transaction)
        val failingApi = object : BackendApiService by mockApiService {
            override suspend fun deleteTransaction(id: String) =
                Response.error<ApiActionResponse>(404, "Not found".toResponseBody())
        }

        val result = runCatching {
            ExpenseRepository(db.appDao(), failingApi).deleteTransaction(transaction.id)
        }

        assertTrue(result.isFailure)
        assertEquals(transaction, db.appDao().getTransactionById(transaction.id))
    }

    @Test
    fun `empty transaction delete response is not treated as confirmation`() = runBlocking {
        val transaction = TransactionEntity(
            id = "server-tx-id",
            type = "expense",
            amount = 80.0,
            category = "Food & Dining",
            description = "Coffee",
            date = "2026-09-27 10:00"
        )
        db.appDao().insertTransaction(transaction)
        val emptyResponseApi = object : BackendApiService by mockApiService {
            override suspend fun deleteTransaction(id: String): Response<ApiActionResponse> =
                Response.success<ApiActionResponse>(204, null)
        }

        val result = runCatching {
            ExpenseRepository(db.appDao(), emptyResponseApi).deleteTransaction(transaction.id)
        }

        assertTrue(result.isFailure)
        assertEquals(transaction, db.appDao().getTransactionById(transaction.id))
    }

    @Test
    fun `action response parses backend success envelope with additional data`() {
        val response = Moshi.Builder()
            .build()
            .adapter(ApiActionResponse::class.java)
            .fromJson("""{"success":true,"data":{"id":"server-tx-id"},"message":"Deleted"}""")

        assertNotNull(response)
        assertTrue(response!!.success)
    }

    @Test
    fun `display balance persists per wallet without changing actual wallet funds`() = runBlocking {
        val firstWallet = WalletEntity(
            id = "wallet-one",
            bankName = "First Bank",
            cardType = "debit",
            cardNumber = "1111",
            balance = 900.0
        )
        val secondWallet = WalletEntity(
            id = "wallet-two",
            bankName = "Second Bank",
            cardType = "credit",
            cardNumber = "2222",
            balance = 5000.0
        )
        db.appDao().insertWallets(listOf(firstWallet, secondWallet))

        repository.saveCardDisplayBalance(firstWallet.id, 160000.0)

        assertEquals(
            CardDisplayBalanceEntity(firstWallet.id, 160000.0),
            db.appDao().getCardDisplayBalance(firstWallet.id)
        )
        assertEquals(null, db.appDao().getCardDisplayBalance(secondWallet.id))
        assertEquals(900.0, db.appDao().getWalletById(firstWallet.id)!!.balance, 0.001)
        assertEquals(5000.0, db.appDao().getWalletById(secondWallet.id)!!.balance, 0.001)
    }

    @Test
    fun `invalid display balances are rejected without saving`() = runBlocking {
        val wallet = WalletEntity(
            id = "wallet-one",
            bankName = "First Bank",
            cardType = "debit",
            cardNumber = "1111",
            balance = 900.0
        )
        db.appDao().insertWallet(wallet)

        val negativeResult = runCatching {
            repository.saveCardDisplayBalance(wallet.id, -1.0)
        }
        val infiniteResult = runCatching {
            repository.saveCardDisplayBalance(wallet.id, Double.POSITIVE_INFINITY)
        }

        assertTrue(negativeResult.isFailure)
        assertTrue(infiniteResult.isFailure)
        assertEquals(null, db.appDao().getCardDisplayBalance(wallet.id))
    }

    @Test
    fun `display balance accepts Indian rupee examples and zero`() {
        assertEquals(50.0, parseCardBalanceInput("50") ?: -1.0, 0.001)
        assertEquals(10000.0, parseCardBalanceInput("10,000") ?: -1.0, 0.001)
        assertEquals(160000.0, parseCardBalanceInput("1,60,000") ?: -1.0, 0.001)
        assertEquals(0.0, parseCardBalanceInput("0") ?: -1.0, 0.001)
        assertEquals(null, parseCardBalanceInput(""))
        assertEquals(null, parseCardBalanceInput("-50"))
        assertEquals(null, parseCardBalanceInput("1,,000"))
        assertEquals(null, parseCardBalanceInput("NaN"))
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
