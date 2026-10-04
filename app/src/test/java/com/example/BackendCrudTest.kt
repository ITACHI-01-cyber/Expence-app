package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.model.TransactionEntity
import com.example.network.ApiResponse
import com.example.network.BackendApiService
import com.example.network.ServerBudget
import com.example.network.ServerDashboardSummary
import com.example.network.ServerGoal
import com.example.network.ServerTransaction
import com.example.network.ServerWallet
import com.example.network.TopUpWalletRequest
import com.example.repository.ExpenseRepository
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    // Mock API service for unit testing CRUD responses
    private val mockApiService = object : BackendApiService {
        override suspend fun getHealth(): Response<ResponseBody> =
            Response.success(ResponseBody.create(null, "{\"status\":\"ok\"}"))

        override suspend fun login(request: com.example.network.LoginRequest) =
            Response.error<ApiResponse<com.example.network.LoginData>>(403, ResponseBody.create(null, "Forbidden"))

        override suspend fun register(request: com.example.network.RegisterRequest) =
            Response.error<ApiResponse<com.example.network.LoginData>>(403, ResponseBody.create(null, "Forbidden"))

        override suspend fun signup(request: com.example.network.RegisterRequest) =
            Response.error<ApiResponse<com.example.network.LoginData>>(403, ResponseBody.create(null, "Forbidden"))

        override suspend fun forgotPassword(request: com.example.network.ForgotPasswordRequest) =
            Response.error<ApiResponse<Unit>>(403, ResponseBody.create(null, "Forbidden"))

        override suspend fun resetPassword(request: com.example.network.ResetPasswordRequest) =
            Response.error<ApiResponse<Unit>>(403, ResponseBody.create(null, "Forbidden"))

        override suspend fun getProfile() =
            Response.error<ApiResponse<com.example.network.UserProfile>>(403, ResponseBody.create(null, "Forbidden"))

        // GET Transactions: returns empty list by default
        override suspend fun getTransactions(month: Int?, year: Int?): Response<ApiResponse<List<ServerTransaction>>> =
            Response.success(ApiResponse(success = true, data = emptyList()))

        // POST Add Transaction: simulates successful remote persistence
        override suspend fun addTransaction(transaction: ServerTransaction): Response<ApiResponse<ServerTransaction>> =
            Response.success(ApiResponse(success = true, data = transaction.copy(mongoId = "server_${transaction.id}")))

        // PUT Update Transaction: simulates successful remote update
        override suspend fun updateTransaction(id: String, transaction: ServerTransaction): Response<ApiResponse<ServerTransaction>> =
            Response.success(ApiResponse(success = true, data = transaction))

        // DELETE Transaction: simulates successful remote deletion
        override suspend fun deleteTransaction(id: String): Response<ResponseBody> =
            Response.success(ResponseBody.create(null, ""))

        override suspend fun getWallets(): Response<ApiResponse<List<ServerWallet>>> =
            Response.success(ApiResponse(success = true, data = emptyList()))

        override suspend fun addWallet(wallet: ServerWallet): Response<ApiResponse<ServerWallet>> =
            Response.success(ApiResponse(success = true, data = wallet))

        override suspend fun deleteWallet(id: String): Response<ResponseBody> =
            Response.success(ResponseBody.create(null, ""))

        override suspend fun topUpWallet(id: String, request: TopUpWalletRequest): Response<ApiResponse<ServerWallet>> =
            Response.success(ApiResponse(success = true, data = null))

        override suspend fun getBudget(month: Int, year: Int): Response<ApiResponse<ServerBudget>> =
            Response.success(ApiResponse(success = true, data = null))

        override suspend fun saveBudget(budget: ServerBudget): Response<ApiResponse<ServerBudget>> =
            Response.success(ApiResponse(success = true, data = budget))

        override suspend fun getGoals(month: Int, year: Int): Response<ApiResponse<List<ServerGoal>>> =
            Response.success(ApiResponse(success = true, data = emptyList()))

        override suspend fun addGoal(goal: ServerGoal): Response<ApiResponse<ServerGoal>> =
            Response.success(ApiResponse(success = true, data = goal))

        override suspend fun deleteGoal(id: String): Response<ResponseBody> =
            Response.success(ResponseBody.create(null, ""))

        override suspend fun getDashboardSummary(): Response<ApiResponse<ServerDashboardSummary>> =
            Response.success(ApiResponse(success = true, data = null))
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
    fun `test POST addTransaction retains transaction in Room and handles server response`() = runBlocking {
        val tx = TransactionEntity(
            id = "tx_post_1",
            type = "expense",
            amount = 150.0,
            category = "Dining",
            description = "Lunch with team",
            date = "2026-09-27 12:30",
            walletId = ""
        )

        repository.addTransaction(tx)

        // Verify transaction is persisted locally (with server ID mapping or original ID)
        val allTx = db.appDao().getAllTransactionsList()
        assertEquals(1, allTx.size)
        assertTrue(allTx.any { it.description == "Lunch with team" && it.amount == 150.0 })
    }

    @Test
    fun `test PUT updateTransaction updates transaction details in Room`() = runBlocking {
        val tx = TransactionEntity(
            id = "tx_put_1",
            type = "expense",
            amount = 200.0,
            category = "Transport",
            description = "Taxi ride",
            date = "2026-09-27 10:00"
        )
        db.appDao().insertTransaction(tx)

        val updated = tx.copy(amount = 250.0, description = "Airport Taxi")
        repository.updateTransaction(updated)

        val retrieved = db.appDao().getTransactionById("tx_put_1")
        assertNotNull(retrieved)
        assertEquals(250.0, retrieved!!.amount, 0.001)
        assertEquals("Airport Taxi", retrieved.description)
    }

    @Test
    fun `test DELETE deleteTransaction removes transaction from Room`() = runBlocking {
        val tx = TransactionEntity(
            id = "tx_del_1",
            type = "expense",
            amount = 50.0,
            category = "Coffee",
            description = "Espresso",
            date = "2026-09-27 08:30"
        )
        db.appDao().insertTransaction(tx)
        assertNotNull(db.appDao().getTransactionById("tx_del_1"))

        repository.deleteTransaction("tx_del_1")
        assertNull(db.appDao().getTransactionById("tx_del_1"))
    }

    @Test
    fun `test Safe Sync NEVER deletes newly added local transactions when server returns empty list`() = runBlocking {
        // User creates a transaction locally
        val userTx = TransactionEntity(
            id = "local_tx_new",
            type = "expense",
            amount = 500.0,
            category = "Groceries",
            description = "Supermarket visit",
            date = "2026-09-27"
        )
        db.appDao().insertTransaction(userTx)

        // Verify it exists in Room
        assertEquals(1, db.appDao().getAllTransactionsList().size)

        // Now simulate user tapping "Sync with Server" (where server returns empty list [])
        val syncResult = repository.syncTransactionsFromServer()
        assertTrue(syncResult.isSuccess)

        // CRITICAL CHECK: Transaction must NOT have been removed by sync!
        val remaining = db.appDao().getAllTransactionsList()
        assertEquals(1, remaining.size)
        assertEquals("Supermarket visit", remaining[0].description)
        assertEquals(500.0, remaining[0].amount, 0.001)
    }

    @Test
    fun `test CRUD Diagnostics runs successfully and checks all methods`() = runBlocking {
        val diagnostics = repository.runCrudDiagnostics()
        assertNotNull(diagnostics)
        assertTrue(diagnostics.isNotEmpty())

        val methods = diagnostics.map { it.method }
        assertTrue(methods.contains("GET"))
        assertTrue(methods.contains("POST"))
        assertTrue(methods.contains("PUT"))
        assertTrue(methods.contains("DELETE"))
        assertTrue(methods.contains("SYNC"))
    }
}
