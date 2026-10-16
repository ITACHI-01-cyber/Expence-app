package com.example.repository

import com.example.data.AppDao
import com.example.model.ApiTestCaseResult
import com.example.model.BudgetConfigEntity
import com.example.model.CardDisplayBalanceEntity
import com.example.model.SavingsGoalEntity
import com.example.model.TransactionEntity
import com.example.model.UserSettingsEntity
import com.example.model.WalletCustomizationEntity
import com.example.model.WalletEntity
import com.example.network.ApiActionResponse
import com.example.network.ApiResponse
import com.example.network.BackendApiService
import com.example.network.CardCustomizationRequest
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    // Serializes per-process design saves so a slow older PATCH cannot apply
    // after a newer selection and restore stale card artwork/colors.
    private val walletCustomizationMutex = Mutex()
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
            val localCustomizations = dao.getAllWalletCustomizationsList().associateBy { it.walletId }
            val existingWallets = dao.getAllWalletsList().associateBy { it.id }

            val parsed = items.map { serverWallet ->
                val walletId = serverWallet.mongoId ?: serverWallet.id
                    ?: throw IllegalStateException("Wallet response did not include an id")
                serverWallet.toEntity(
                    localCustomization = localCustomizations[walletId],
                    existingWallet = existingWallets[walletId]
                )
            }
            dao.clearWallets()
            if (parsed.isNotEmpty()) dao.insertWallets(parsed)
            // A GET response is authoritative only for fields it actually
            // contains. Persist the resolved value so an omitted optional
            // server property cannot reset a valid local design on restart.
            dao.insertWalletCustomizations(parsed.map { it.toCustomization() })
            Result.success(parsed)
        } catch (error: Exception) {
            val parsedError = if (error is IllegalStateException) error else apiError("wallets", 0, error.localizedMessage, error)
            Result.failure(parsedError)
        }
    }

    suspend fun updateCardCustomization(
        walletId: String,
        theme: String,
        primaryColor: String,
        secondaryColor: String,
        accentColor: String,
        artwork: String,
        cardStyle: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        walletCustomizationMutex.withLock {
            val currentWallet = dao.getWalletById(walletId)
            try {
                requireNotNull(currentWallet) { "The selected card is no longer available." }
                // Do not update Room before the server confirms the PATCH. This
                // prevents a failed or stale request from appearing as saved.
                val response = apiService.updateWalletCustomization(
                    walletId,
                    CardCustomizationRequest(
                        designPreset = theme,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textColor = currentWallet.textColor,
                        accentColor = accentColor,
                        artwork = artwork,
                        cardStyle = cardStyle
                    )
                )
                val confirmed = response.requireData("wallet customization update")
                val requested = currentWallet.copy(
                    designId = theme,
                    cardTheme = theme,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    accentColor = accentColor,
                    artwork = artwork,
                    cardStyle = cardStyle
                ).toCustomization()
                // If an older deployment omits a customization property in its
                // response, retain the just-confirmed value for that property.
                val merged = currentWallet.mergeCustomizationResponse(confirmed, requested)
                dao.insertWallet(merged)
                dao.insertWalletCustomization(merged.toCustomization())
                Result.success(Unit)
            } catch (error: Exception) {
                Result.failure(error)
            }
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
        val customization = WalletCustomizationEntity(
            walletId = wallet.id,
            cardTheme = wallet.cardTheme,
            primaryColor = wallet.primaryColor,
            secondaryColor = wallet.secondaryColor,
            accentColor = wallet.accentColor,
            artwork = wallet.artwork,
            cardStyle = wallet.cardStyle,
            textColor = wallet.textColor,
            cardIcon = wallet.cardIcon
        )
        dao.insertWalletCustomization(customization)
        dao.insertWallet(wallet)

        try {
            val serverWallet = apiService.addWallet(wallet.toServer()).requireData("wallet creation").toEntity()
            val merged = serverWallet.copy(
                cardTheme = wallet.cardTheme,
                designId = wallet.designId,
                primaryColor = wallet.primaryColor,
                secondaryColor = wallet.secondaryColor,
                accentColor = wallet.accentColor,
                artwork = wallet.artwork,
                cardStyle = wallet.cardStyle
            )
            if (serverWallet.id != wallet.id) {
                dao.deleteWalletById(wallet.id)
                dao.deleteWalletCustomization(wallet.id)
                dao.insertWalletCustomization(customization.copy(walletId = serverWallet.id))
                dao.insertWallet(merged.copy(id = serverWallet.id))
            } else {
                dao.insertWallet(merged)
            }
        } catch (_: Exception) {
            // Retain local wallet if server is unavailable
        }
    }

    suspend fun updateWallet(wallet: WalletEntity) = withContext(Dispatchers.IO) {
        val cust = dao.getWalletCustomization(wallet.id)
        val customization = WalletCustomizationEntity(
            walletId = wallet.id,
            cardTheme = wallet.cardTheme.ifBlank { cust?.cardTheme ?: "midnight" },
            primaryColor = wallet.primaryColor.ifBlank { cust?.primaryColor ?: "#1A1A2E" },
            secondaryColor = wallet.secondaryColor.ifBlank { cust?.secondaryColor ?: "#16213E" },
            accentColor = wallet.accentColor.ifBlank { cust?.accentColor ?: "#38BDF8" },
            artwork = wallet.artwork.ifBlank { cust?.artwork ?: "waves" },
            cardStyle = wallet.cardStyle.ifBlank { cust?.cardStyle ?: "illustrated" }
        )
        dao.insertWalletCustomization(customization)
        dao.insertWallet(wallet)

        try {
            val serverWallet = apiService.updateWallet(wallet.id, wallet.toServer(includeId = true))
                .requireData("wallet update")
                .toEntity()
            val merged = serverWallet.copy(
                cardTheme = customization.cardTheme,
                designId = customization.cardTheme,
                primaryColor = customization.primaryColor,
                secondaryColor = customization.secondaryColor,
                accentColor = customization.accentColor,
                artwork = customization.artwork,
                cardStyle = customization.cardStyle
            )
            dao.insertWallet(merged)
        } catch (_: Exception) {
            // Retain local update if server call failed
        }
    }

    suspend fun deleteWallet(walletId: String) = withContext(Dispatchers.IO) {
        try {
            apiService.deleteWallet(walletId).requireSuccess("wallet deletion")
        } catch (_: Exception) { }
        dao.deleteWalletById(walletId)
        dao.deleteWalletCustomization(walletId)
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
        dao.clearWalletCustomizations()
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
        accentColor = accentColor,
        artwork = artwork,
        cardStyle = cardStyle,
        textColor = textColor,
        cardIcon = cardIcon,
        backSignatureText = backSignatureText,
        backContactInfo = backContactInfo
    )

    private fun WalletEntity.toCustomization() = WalletCustomizationEntity(
        walletId = id,
        cardTheme = cardTheme.ifBlank { designId },
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        accentColor = accentColor,
        artwork = artwork,
        cardStyle = cardStyle,
        textColor = textColor,
        cardIcon = cardIcon,
        lastUpdated = System.currentTimeMillis()
    )

    /**
     * The customization PATCH may return a compact wallet projection. Keep
     * financial/ownership fields from the known wallet and accept only the
     * customization fields that the response actually includes.
     */
    private fun WalletEntity.mergeCustomizationResponse(
        response: ServerWallet,
        requested: WalletCustomizationEntity
    ) = copy(
        designId = response.designPreset?.takeIf(String::isNotBlank) ?: requested.cardTheme,
        cardTheme = response.designPreset?.takeIf(String::isNotBlank) ?: requested.cardTheme,
        primaryColor = response.primaryColor?.takeIf(String::isNotBlank) ?: requested.primaryColor,
        secondaryColor = response.secondaryColor?.takeIf(String::isNotBlank) ?: requested.secondaryColor,
        textColor = response.textColor?.takeIf(String::isNotBlank) ?: requested.textColor,
        accentColor = response.accentColor?.takeIf(String::isNotBlank) ?: requested.accentColor,
        artwork = response.artwork?.takeIf(String::isNotBlank) ?: requested.artwork,
        cardStyle = response.cardStyle?.takeIf(String::isNotBlank) ?: requested.cardStyle
    )

    private fun ServerWallet.toEntity(
        localCustomization: WalletCustomizationEntity? = null,
        existingWallet: WalletEntity? = null
    ): WalletEntity {
        val walletId = mongoId ?: id
            ?: throw IllegalStateException("Wallet response did not include an id")
        val resolvedBalance = balance ?: existingWallet?.balance ?: 0.0
        return WalletEntity(
            id = walletId,
            bankName = bankName.ifBlank { existingWallet?.bankName ?: "Bank" },
            cardType = cardType.ifBlank { existingWallet?.cardType ?: "debit" },
            cardBrand = cardBrand.orEmpty().ifBlank { existingWallet?.cardBrand.orEmpty() },
            cardName = cardName.orEmpty().ifBlank { existingWallet?.cardName.orEmpty() },
            cardNumber = cardNumber.ifBlank { existingWallet?.cardNumber.orEmpty() },
            cardHolderName = cardHolderName.ifBlank { existingWallet?.cardHolderName.orEmpty() },
            expiryDate = expiryDate.orEmpty().ifBlank { existingWallet?.expiryDate.orEmpty() },
            balance = resolvedBalance,
            creditLimit = creditLimit ?: existingWallet?.creditLimit,
            outstandingBalance = outstandingBalance ?: existingWallet?.outstandingBalance,
            statementClosingDay = statementClosingDay ?: existingWallet?.statementClosingDay,
            paymentDueDay = paymentDueDay ?: existingWallet?.paymentDueDay,
            minimumPaymentAmount = minimumPaymentAmount ?: existingWallet?.minimumPaymentAmount,
            creditTermsConfigured = creditTermsConfigured || (existingWallet?.creditTermsConfigured == true),
            timezone = timezone ?: existingWallet?.timezone ?: "UTC",
            primaryColor = primaryColor?.takeIf(String::isNotBlank) ?: localCustomization?.primaryColor ?: existingWallet?.primaryColor ?: "#1A1A2E",
            secondaryColor = secondaryColor?.takeIf(String::isNotBlank) ?: localCustomization?.secondaryColor ?: existingWallet?.secondaryColor ?: "#16213E",
            designId = designPreset?.takeIf(String::isNotBlank) ?: localCustomization?.cardTheme ?: existingWallet?.designId ?: "midnight",
            cardTheme = designPreset?.takeIf(String::isNotBlank) ?: localCustomization?.cardTheme ?: existingWallet?.cardTheme ?: "midnight",
            accentColor = accentColor?.takeIf(String::isNotBlank) ?: localCustomization?.accentColor ?: existingWallet?.accentColor ?: "#38BDF8",
            artwork = artwork?.takeIf(String::isNotBlank) ?: localCustomization?.artwork ?: existingWallet?.artwork ?: "waves",
            cardStyle = cardStyle?.takeIf(String::isNotBlank) ?: localCustomization?.cardStyle ?: existingWallet?.cardStyle ?: "illustrated",
            textColor = textColor?.takeIf(String::isNotBlank) ?: localCustomization?.textColor ?: existingWallet?.textColor ?: "#FFFFFF",
            cardIcon = cardIcon?.takeIf(String::isNotBlank) ?: localCustomization?.cardIcon ?: existingWallet?.cardIcon ?: "none",
            backSignatureText = backSignatureText ?: existingWallet?.backSignatureText ?: "Not Valid without Authorized Signature",
            backContactInfo = backContactInfo ?: existingWallet?.backContactInfo ?: "support@bank.com | 1-800-555-0199"
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

    private fun parseTimestamp(value: String?): Long? =
        com.example.analytics.parseDateStringToMillis(value)

    private fun apiError(resource: String, code: Int, message: String?, error: Throwable? = null): IllegalStateException {
        val detail = when {
            error is com.squareup.moshi.JsonDataException || error is com.squareup.moshi.JsonEncodingException || error is IllegalArgumentException ->
                "Failed to parse $resource response: ${error.localizedMessage}"
            error is java.io.IOException ->
                "Network connectivity failure while syncing $resource: ${error.localizedMessage}"
            else -> message?.takeIf(String::isNotBlank) ?: when (code) {
                0 -> "No internet connection or the backend is unavailable."
                400 -> "The server rejected the $resource request."
                401 -> "Your session has expired. Please sign in again."
                403 -> "You do not have permission to access this $resource."
                404 -> "The requested $resource was not found."
                in 500..599 -> "The backend is temporarily unavailable. Please try again."
                else -> "The $resource request failed (HTTP $code)."
            }
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
