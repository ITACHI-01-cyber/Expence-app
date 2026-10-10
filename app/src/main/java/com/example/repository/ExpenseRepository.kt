package com.example.repository

import com.example.data.AppDao
import com.example.model.ApiTestCaseResult
import com.example.model.BudgetConfigEntity
import com.example.model.CardDisplayBalanceEntity
import com.example.model.SavingsGoalEntity
import com.example.model.TransactionEntity
import com.example.model.UserSettingsEntity
import com.example.model.WalletEntity
import com.example.network.ApiActionResponse
import com.example.network.ApiResponse
import com.example.network.BackendApiService
import com.example.network.CreditActivityItem
import com.example.network.CreditRepaymentRequest
import com.example.network.CreditRepaymentResponse
import com.example.network.CreditRefundRequest
import com.example.network.CreditStatement
import com.example.network.CreditStatementRequest
import com.example.network.NetworkClient
import com.example.network.ServerBudget
import com.example.network.ServerDashboardSummary
import com.example.network.ServerGoal
import com.example.network.ServerTransaction
import com.example.network.ServerWallet
import com.example.network.UserSettingsRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import retrofit2.Response

class ExpenseRepository(
    private val dao: AppDao,
    private val apiService: BackendApiService = NetworkClient.apiService
) {
    val wallets: Flow<List<WalletEntity>> = dao.getAllWallets()
    val cardDisplayBalances: Flow<List<CardDisplayBalanceEntity>> = dao.getCardDisplayBalances()
    val transactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val goals: Flow<List<SavingsGoalEntity>> = dao.getAllGoals()
    val budgetConfig: Flow<BudgetConfigEntity?> = dao.getBudgetConfig()
    val userSettings: Flow<UserSettingsEntity?> = dao.getUserSettings()

    suspend fun syncAllFromServer(
        onTransactionSync: (Result<List<TransactionEntity>>) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val transactionResult = syncTransactionsFromServer()
        onTransactionSync(transactionResult)
        val results = listOf(
            transactionResult,
            syncWalletsFromServer(),
            syncBudgetFromServer(),
            syncGoalsFromServer()
        )
        val failure = results.firstOrNull { it.isFailure }?.exceptionOrNull()
        if (failure == null) Result.success(Unit) else Result.failure(failure)
    }

    suspend fun syncTransactionsFromServer(): Result<List<TransactionEntity>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTransactions()
            val items = response.requireData("transactions")
            val parsed = items.map { item ->
                val id = item.mongoId ?: item.id
                    ?: throw IllegalStateException("Transaction response did not include an id")
                val serverDate = item.date?.takeIf(String::isNotBlank)
                    ?: throw IllegalStateException("Transaction response did not include a date")
                val timestamp = parseTimestamp(serverDate)
                    ?: throw IllegalStateException("Transaction response did not include a valid date")
                TransactionEntity(
                    id = id,
                    type = item.type,
                    amount = item.amount,
                    category = item.category,
                    description = item.description ?: item.title.orEmpty(),
                    date = serverDate.replace("T", " ").take(16),
                    timestamp = timestamp,
                    isRecurring = item.isRecurring,
                    walletId = item.walletId.orEmpty()
                )
            }
            dao.clearTransactions()
            if (parsed.isNotEmpty()) dao.insertTransactions(parsed)
            Result.success(parsed)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun syncWalletsFromServer(): Result<List<WalletEntity>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getWallets()
            val items = response.requireData("wallets")
            val parsed = items.map { it.toEntity() }
            dao.clearWallets()
            if (parsed.isNotEmpty()) dao.insertWallets(parsed)
            Result.success(parsed)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun syncBudgetFromServer(): Result<BudgetConfigEntity?> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDashboardSummary()
            val summary = response.requireData("dashboard summary")
            val now = Calendar.getInstance()
            val config = BudgetConfigEntity(
                id = 1,
                monthlyIncome = summary.monthlyIncome,
                budgetLimit = summary.monthlyBudgetLimit,
                month = now.get(Calendar.MONTH) + 1,
                year = now.get(Calendar.YEAR),
                availableBalance = summary.availableBalance,
                monthlySpent = summary.monthlySpent
            )
            dao.saveBudgetConfig(config)
            Result.success(config)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun syncGoalsFromServer(): Result<List<SavingsGoalEntity>> = withContext(Dispatchers.IO) {
        try {
            val now = Calendar.getInstance()
            val response = apiService.getGoals(now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR))
            val parsed = response.requireData("goals").map { it.toEntity() }
            dao.clearGoals()
            if (parsed.isNotEmpty()) dao.insertGoals(parsed)
            Result.success(parsed)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun addWallet(wallet: WalletEntity) = withContext(Dispatchers.IO) {
        dao.insertWallet(apiService.addWallet(wallet.toServer()).requireData("wallet creation").toEntity())
    }

    suspend fun updateWallet(wallet: WalletEntity) = withContext(Dispatchers.IO) {
        dao.insertWallet(
            apiService.updateWallet(wallet.id, wallet.toServer(includeId = true))
                .requireData("wallet update")
                .toEntity()
        )
    }

    suspend fun deleteWallet(walletId: String) = withContext(Dispatchers.IO) {
        apiService.deleteWallet(walletId).requireSuccess("wallet deletion")
        dao.deleteWalletById(walletId)
        dao.deleteCardDisplayBalance(walletId)
    }

    suspend fun saveCardDisplayBalance(walletId: String, balance: Double) = withContext(Dispatchers.IO) {
        require(balance.isFinite() && balance >= 0.0) {
            "Enter a valid non-negative card display balance."
        }
        if (dao.getWalletById(walletId) == null) {
            throw IllegalStateException("The selected card is no longer available.")
        }
        dao.saveCardDisplayBalance(CardDisplayBalanceEntity(walletId, balance))
    }

    suspend fun topUpWallet(walletId: String, amount: Double) = withContext(Dispatchers.IO) {
        val saved = apiService.addMoneyToWallet(walletId, amount)
            .requireData("wallet balance update")
            .toEntity()
        dao.insertWallet(saved)
    }

    suspend fun recordCreditCardRepayment(
        cardId: String,
        sourceWalletId: String,
        amount: Double,
        statementId: String?,
        idempotencyKey: String
    ): CreditRepaymentResponse = withContext(Dispatchers.IO) {
        require(amount.isFinite() && amount > 0.0) { "Enter a valid payment amount greater than zero." }
        val response = apiService.recordCreditCardRepayment(
            cardId,
            CreditRepaymentRequest(
                sourceWalletId = sourceWalletId,
                amount = amount,
                statementId = statementId,
                idempotencyKey = idempotencyKey
            )
        ).requireData("credit card repayment")

        val card = dao.getWalletById(cardId)
            ?: throw IllegalStateException("The selected credit card is no longer available.")
        val sourceWallet = dao.getWalletById(sourceWalletId)
            ?: throw IllegalStateException("The selected source account is no longer available.")
        dao.insertWallet(card.copy(outstandingBalance = response.outstandingBalance))
        dao.insertWallet(sourceWallet.copy(balance = response.sourceWalletBalance))
        response
    }

    suspend fun getCreditCardStatements(cardId: String): List<CreditStatement> =
        withContext(Dispatchers.IO) {
            apiService.getCreditCardStatements(cardId).requireData("credit card statements")
        }

    suspend fun generateCreditCardStatement(cardId: String): CreditStatement =
        withContext(Dispatchers.IO) {
            apiService.generateCreditCardStatement(cardId, CreditStatementRequest())
                .requireData("credit card statement")
        }

    suspend fun getCreditCardActivity(cardId: String): List<CreditActivityItem> =
        withContext(Dispatchers.IO) {
            apiService.getCreditCardActivity(cardId).requireData("credit card activity")
        }

    suspend fun recordCreditCardRefund(
        purchaseId: String,
        amount: Double,
        idempotencyKey: String
    ): TransactionEntity = withContext(Dispatchers.IO) {
        require(amount.isFinite() && amount > 0.0) { "Enter a valid refund amount greater than zero." }
        val saved = apiService.recordCreditRefund(
            purchaseId,
            CreditRefundRequest(amount = amount, idempotencyKey = idempotencyKey)
        ).requireData("credit card refund")
        saved.toEntity().also { dao.insertTransaction(it) }
    }

    suspend fun addTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        val saved = apiService.addTransaction(transaction.toServer())
            .requireData("transaction creation")
            .toEntity()
        dao.insertTransaction(saved)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        val saved = apiService.updateTransaction(transaction.id, transaction.toServer())
            .requireData("transaction update")
            .toEntity()
        dao.insertTransaction(saved)
    }

    suspend fun deleteTransaction(transactionId: String) = withContext(Dispatchers.IO) {
        apiService.deleteTransaction(transactionId).requireSuccess("transaction deletion")
        dao.deleteTransactionById(transactionId)
    }

    suspend fun addGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        dao.insertGoal(apiService.addGoal(goal.toServer()).requireData("goal creation").toEntity())
    }

    suspend fun updateGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        dao.insertGoal(
            apiService.updateGoal(goal.id, goal.toServer(includeId = true))
                .requireData("goal update")
                .toEntity()
        )
    }

    suspend fun toggleGoalStatus(goalId: String, completed: Boolean) = withContext(Dispatchers.IO) {
        dao.insertGoal(
            apiService.updateGoalStatus(goalId, completed)
                .requireData("goal status update")
                .toEntity()
        )
    }

    suspend fun deleteGoal(goalId: String) = withContext(Dispatchers.IO) {
        apiService.deleteGoal(goalId).requireSuccess("goal deletion")
        dao.deleteGoalById(goalId)
    }

    suspend fun updateBudgetConfig(income: Double, limit: Double) = withContext(Dispatchers.IO) {
        val now = Calendar.getInstance()
        val saved = apiService.saveBudget(
            ServerBudget(
                monthlyIncome = income,
                budgetLimit = limit,
                month = now.get(Calendar.MONTH) + 1,
                year = now.get(Calendar.YEAR)
            )
        ).requireData("budget update")
        dao.saveBudgetConfig(
            BudgetConfigEntity(
                id = 1,
                monthlyIncome = saved.monthlyIncome,
                budgetLimit = saved.budgetLimit,
                month = saved.month,
                year = saved.year
            )
        )
    }

    suspend fun updateUserSettings(settings: UserSettingsEntity) = withContext(Dispatchers.IO) {
        dao.saveUserSettings(settings)
    }

    suspend fun saveUserSettings(settings: UserSettingsEntity) = withContext(Dispatchers.IO) {
        val previous = dao.getUserSettingsOnce()
        if (previous?.name != settings.name) {
            apiService.updateUserSettings(UserSettingsRequest(name = settings.name))
                .requireData("profile settings update")
        }
        if (previous?.accentColor != settings.accentColor) {
            apiService.updateUserSettings(UserSettingsRequest(accentColor = settings.accentColor))
                .requireData("accent color update")
        }
        if (previous?.currency != settings.currency) {
            apiService.updateUserSettings(UserSettingsRequest(currency = settings.currencyCode))
                .requireData("currency update")
        }
        dao.saveUserSettings(
            settings.copy(email = previous?.email ?: settings.email, isGuest = false)
        )
    }

    suspend fun getUserSettingsOnce(): UserSettingsEntity? = withContext(Dispatchers.IO) {
        dao.getUserSettingsOnce()
    }

    suspend fun clearAllUserData() = withContext(Dispatchers.IO) {
        dao.clearWallets()
        dao.clearCardDisplayBalances()
        dao.clearTransactions()
        dao.clearGoals()
        dao.clearBudgetConfig()
        dao.clearUserSettings()
    }

    suspend fun runCrudDiagnostics(): List<ApiTestCaseResult> = withContext(Dispatchers.IO) {
        val checks = listOf(
            Triple("Transactions", "/api/transactions", suspend {
                val now = Calendar.getInstance()
                apiService.getTransactions(now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR)).code()
            }),
            Triple("Wallets", "/api/wallet", suspend { apiService.getWallets().code() }),
            Triple("Dashboard", "/api/dashboard/summary", suspend { apiService.getDashboardSummary().code() })
        )
        checks.map { (name, endpoint, request) ->
            val started = System.currentTimeMillis()
            try {
                val status = request()
                ApiTestCaseResult(
                    testName = "$name (GET)",
                    method = "GET",
                    endpoint = endpoint,
                    statusCode = status,
                    latencyMs = System.currentTimeMillis() - started,
                    isSuccess = status in 200..299,
                    detailMessage = if (status in 200..299) "Backend request succeeded"
                    else "Backend returned HTTP $status"
                )
            } catch (error: Exception) {
                ApiTestCaseResult(
                    testName = "$name (GET)",
                    method = "GET",
                    endpoint = endpoint,
                    statusCode = 0,
                    latencyMs = System.currentTimeMillis() - started,
                    isSuccess = false,
                    detailMessage = error.localizedMessage ?: "Network request failed"
                )
            }
        }
    }

    private fun WalletEntity.toServer(includeId: Boolean = false) = ServerWallet(
        id = id.takeIf { includeId },
        cardNumber = cardNumber,
        cardType = cardType,
        cardBrand = cardBrand,
        cardName = cardName,
        expiryDate = expiryDate,
        cardHolderName = cardHolderName,
        balance = balance,
        bankName = bankName,
        creditLimit = creditLimit,
        outstandingBalance = outstandingBalance,
        statementClosingDay = statementClosingDay,
        paymentDueDay = paymentDueDay,
        minimumPaymentAmount = minimumPaymentAmount,
        creditTermsConfigured = creditTermsConfigured,
        timezone = timezone,
        designPreset = designId,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        textColor = textColor,
        cardIcon = cardIcon,
        backSignatureText = backSignatureText,
        backContactInfo = backContactInfo
    )

    private fun ServerWallet.toEntity(): WalletEntity {
        val walletId = mongoId ?: id
            ?: throw IllegalStateException("Wallet response did not include an id")
        return WalletEntity(
            id = walletId,
            bankName = bankName,
            cardType = cardType,
            cardBrand = cardBrand.orEmpty(),
            cardName = cardName.orEmpty(),
            cardNumber = cardNumber,
            cardHolderName = cardHolderName,
            expiryDate = expiryDate.orEmpty(),
            balance = balance,
            creditLimit = creditLimit,
            outstandingBalance = outstandingBalance,
            statementClosingDay = statementClosingDay,
            paymentDueDay = paymentDueDay,
            minimumPaymentAmount = minimumPaymentAmount,
            creditTermsConfigured = creditTermsConfigured,
            timezone = timezone ?: "UTC",
            primaryColor = primaryColor ?: "#1A1A2E",
            secondaryColor = secondaryColor ?: "#16213E",
            designId = designPreset.orEmpty(),
            cardTheme = designPreset.orEmpty(),
            accentColor = "#38BDF8",
            artwork = "waves",
            cardStyle = "illustrated",
            textColor = textColor ?: "#FFFFFF",
            cardIcon = cardIcon ?: "none",
            backSignatureText = backSignatureText ?: "Not Valid without Authorized Signature",
            backContactInfo = backContactInfo ?: "support@bank.com | 1-800-555-0199"
        )
    }

    private fun TransactionEntity.toServer() = ServerTransaction(
        id = null,
        type = type,
        amount = amount,
        category = category,
        description = description,
        date = date.replace(" ", "T").take(16),
        isRecurring = isRecurring,
        walletId = walletId.takeIf(String::isNotBlank),
        transactionKind = transactionKind,
        idempotencyKey = idempotencyKey,
        statementId = statementId,
        relatedTransactionId = relatedTransactionId
    )

    private fun ServerTransaction.toEntity(): TransactionEntity {
        val transactionId = mongoId ?: id
            ?: throw IllegalStateException("Transaction response did not include an id")
        val timestamp = parseTimestamp(date) ?: System.currentTimeMillis()
        return TransactionEntity(
            id = transactionId,
            type = type,
            amount = amount,
            category = category,
            description = description ?: title.orEmpty(),
            date = date?.replace("T", " ")?.take(16).orEmpty(),
            timestamp = timestamp,
            isRecurring = isRecurring,
            walletId = walletId.orEmpty(),
            transactionKind = transactionKind,
            idempotencyKey = idempotencyKey,
            statementId = statementId,
            relatedTransactionId = relatedTransactionId
        )
    }

    private fun SavingsGoalEntity.toServer(includeId: Boolean = false) = ServerGoal(
        id = id.takeIf { includeId },
        title = title,
        amount = amount,
        medium = medium,
        completed = completed,
        month = month,
        year = year
    )

    private fun ServerGoal.toEntity() = SavingsGoalEntity(
        id = mongoId ?: id ?: throw IllegalStateException("Goal response did not include an id"),
        title = title,
        amount = amount,
        medium = medium,
        completed = completed,
        month = month,
        year = year
    )

    private fun parseTimestamp(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd HH:mm")
        for (pattern in patterns) {
            try {
                return SimpleDateFormat(pattern, Locale.US).parse(value.take(pattern.length))?.time
            } catch (_: IllegalArgumentException) {
                continue
            } catch (_: java.text.ParseException) {
                continue
            }
        }
        return null
    }

    private fun apiError(resource: String, code: Int, message: String?): IllegalStateException {
        val detail = message?.takeIf(String::isNotBlank) ?: when (code) {
            0 -> "No internet connection or the backend is unavailable."
            400 -> "The server rejected the $resource request."
            401 -> "Your session has expired. Please sign in again."
            403 -> "You do not have permission to access this $resource."
            404 -> "The requested $resource was not found."
            in 500..599 -> "The backend is temporarily unavailable. Please try again."
            else -> "The $resource request failed (HTTP $code)."
        }
        return IllegalStateException(detail)
    }

    private fun <T> Response<ApiResponse<T>>.requireData(resource: String): T {
        val payload = body()
        if (!isSuccessful || payload?.success != true) {
            throw apiError(resource, code(), payload?.message)
        }
        return payload.data ?: throw IllegalStateException("The server returned no $resource data.")
    }

    private fun Response<ApiActionResponse>.requireSuccess(resource: String) {
        val payload = body()
        if (!isSuccessful) {
            throw apiError(resource, code(), payload?.message)
        }
        if (payload == null) {
            throw IllegalStateException("The server returned no confirmation for $resource.")
        }
        if (!payload.success) throw apiError(resource, code(), payload.message)
    }
}
