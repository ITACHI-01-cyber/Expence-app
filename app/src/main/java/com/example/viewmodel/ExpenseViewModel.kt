package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.model.ApiTestCaseResult
import com.example.model.BudgetConfigEntity
import com.example.model.CategoryExpense
import com.example.model.DashboardSummary
import com.example.model.SavingsGoalEntity
import com.example.model.TransactionEntity
import com.example.model.UserSettingsEntity
import com.example.model.WalletEntity
import com.example.network.ForgotPasswordRequest
import com.example.network.CreditActivityItem
import com.example.network.CreditStatement
import com.example.network.LoginRequest
import com.example.network.NetworkClient
import com.example.network.RegisterRequest
import com.example.network.ResetPasswordRequest
import com.example.network.TokenManager
import com.example.network.VerifyOtpRequest
import com.example.repository.ExpenseRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class ScreenTab {
    DASHBOARD, TRANSACTIONS, WALLET, BUDGET, SETTINGS
}

enum class AuthPhase {
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD,
    OTP_VERIFICATION,
    CREATE_NEW_PASSWORD,
    PASSWORD_RESET_SUCCESS
}

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    // Auth Flow State
    private val _authPhase = MutableStateFlow(AuthPhase.LOGIN)
    val authPhase: StateFlow<AuthPhase> = _authPhase.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _serverToken = MutableStateFlow<String?>(null)
    val serverToken: StateFlow<String?> = _serverToken.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _transactionSyncError = MutableStateFlow<String?>(null)
    val transactionSyncError: StateFlow<String?> = _transactionSyncError.asStateFlow()

    private val _isTransactionOperationInProgress = MutableStateFlow(false)
    val isTransactionOperationInProgress: StateFlow<Boolean> =
        _isTransactionOperationInProgress.asStateFlow()
    private val _isTransactionDeleteInProgress = MutableStateFlow(false)
    val isTransactionDeleteInProgress: StateFlow<Boolean> =
        _isTransactionDeleteInProgress.asStateFlow()

    private val _serverConnected = MutableStateFlow(false)
    val serverConnected: StateFlow<Boolean> = _serverConnected.asStateFlow()

    private val _operationErrorMessage = MutableStateFlow<String?>(null)
    val operationErrorMessage: StateFlow<String?> = _operationErrorMessage.asStateFlow()

    private val _operationSuccessMessage = MutableStateFlow<String?>(null)
    val operationSuccessMessage: StateFlow<String?> = _operationSuccessMessage.asStateFlow()

    private val _creditStatements = MutableStateFlow<Map<String, List<CreditStatement>>>(emptyMap())
    val creditStatements: StateFlow<Map<String, List<CreditStatement>>> = _creditStatements.asStateFlow()

    private val _creditActivity = MutableStateFlow<Map<String, List<CreditActivityItem>>>(emptyMap())
    val creditActivity: StateFlow<Map<String, List<CreditActivityItem>>> = _creditActivity.asStateFlow()

    private val _creditDataLoading = MutableStateFlow(false)
    val creditDataLoading: StateFlow<Boolean> = _creditDataLoading.asStateFlow()

    private val _creditRepaymentInProgress = MutableStateFlow(false)
    val creditRepaymentInProgress: StateFlow<Boolean> = _creditRepaymentInProgress.asStateFlow()

    // Forgot Password & OTP State
    private val _otpEmail = MutableStateFlow("")
    val otpEmail: StateFlow<String> = _otpEmail.asStateFlow()

    private val _otpCooldown = MutableStateFlow(45)
    val otpCooldown: StateFlow<Int> = _otpCooldown.asStateFlow()

    private val _gmailNotificationBanner = MutableStateFlow<String?>(null)
    val gmailNotificationBanner: StateFlow<String?> = _gmailNotificationBanner.asStateFlow()

    private var pendingRegistration: RegisterRequest? = null
    private var isRegistrationOtpFlow = false
    private var pendingResetCode: String? = null

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(ScreenTab.DASHBOARD)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Filter states for Transactions tab
    val transactionFilterType = MutableStateFlow("all") // "all", "expense", "income"
    val transactionSearchQuery = MutableStateFlow("")
    val transactionDateFilter = MutableStateFlow("month") // "month", "week", "all"

    init {
        TokenManager.init(application)
        val database = AppDatabase.getInstance(application)
        repository = ExpenseRepository(database.appDao())

        // Ensure user settings default to Dark theme
        viewModelScope.launch {
            val existing = repository.getUserSettingsOnce()
            if (existing == null) {
                repository.updateUserSettings(UserSettingsEntity(theme = "dark"))
            } else if (existing.theme.equals("light", ignoreCase = true) || existing.theme.equals("system", ignoreCase = true)) {
                repository.updateUserSettings(existing.copy(theme = "dark"))
            }
        }

        // Restore the backend session and refresh its cache.
        val savedToken = TokenManager.getToken()
        if (!savedToken.isNullOrBlank()) {
            _serverToken.value = savedToken
            _isAuthenticated.value = true

            // Restore user profile if saved
            val savedName = TokenManager.getUserName()
            val savedEmail = TokenManager.getUserEmail()
            if (!savedName.isNullOrBlank() || !savedEmail.isNullOrBlank()) {
                viewModelScope.launch {
                    repository.updateUserSettings(
                        UserSettingsEntity(
                            id = 1,
                            name = savedName ?: "User",
                            email = savedEmail ?: "",
                            isGuest = false
                        )
                    )
                }
            }

            // Sync live MongoDB data from Render backend
            syncAllData()
        }
    }

    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    // Repository Flows
    val wallets: StateFlow<List<WalletEntity>> = repository.wallets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cardDisplayBalances: StateFlow<Map<String, Double>> = repository.cardDisplayBalances
        .map { balances -> balances.associate { it.walletId to it.balance } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<SavingsGoalEntity>> = repository.goals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetConfig: StateFlow<BudgetConfigEntity?> = repository.budgetConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userSettings: StateFlow<UserSettingsEntity?> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Dashboard Summary combine
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        wallets, allTransactions, budgetConfig
    ) { walletList, txList, config ->
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        var monthSpent = 0.0
        var monthIncome = 0.0

        txList.forEach { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
                if (tx.type == "expense") {
                    monthSpent += tx.amount
                } else if (tx.type == "income") {
                    monthIncome += tx.amount
                }
            }
        }

        val totalWalletBalance = walletList.sumOf { it.balance }
        val incomeVal = config?.monthlyIncome ?: monthIncome
        val limitVal = config?.budgetLimit ?: 0.0

        DashboardSummary(
            monthlyIncome = incomeVal,
            monthlyBudgetLimit = limitVal,
            monthlySpent = config?.monthlySpent ?: monthSpent,
            availableBalance = config?.availableBalance ?: totalWalletBalance
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardSummary(0.0, 0.0, 0.0, 0.0)
    )

    // Category Expenses for Dashboard / Grid
    val categoryExpenses: StateFlow<List<CategoryExpense>> = allTransactions.combine(dashboardSummary) { txList, _ ->
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        val map = mutableMapOf<String, Double>()
        var total = 0.0

        txList.forEach { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
                if (tx.type == "expense") {
                    val cat = tx.category.ifBlank { "Other" }
                    map[cat] = (map[cat] ?: 0.0) + tx.amount
                    total += tx.amount
                }
            }
        }

        map.entries.map { (cat, amt) ->
            CategoryExpense(
                category = cat,
                amount = amt,
                percentage = if (total > 0) (amt / total).toFloat() else 0f
            )
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─────────────────────────────────────────────────────────────────────────
    // Live Server Sync & Backend Diagnostics
    // ─────────────────────────────────────────────────────────────────────────

    private val _apiTestResults = MutableStateFlow<List<ApiTestCaseResult>>(emptyList())
    val apiTestResults: StateFlow<List<ApiTestCaseResult>> = _apiTestResults.asStateFlow()

    private val _isRunningApiTests = MutableStateFlow(false)
    val isRunningApiTests: StateFlow<Boolean> = _isRunningApiTests.asStateFlow()

    fun runBackendCrudDiagnostics() {
        viewModelScope.launch {
            _isRunningApiTests.value = true
            try {
                val results = repository.runCrudDiagnostics()
                _apiTestResults.value = results
                _serverConnected.value = results.any { it.isSuccess }
            } finally {
                _isRunningApiTests.value = false
            }
        }
    }

    fun syncAllData() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val res = repository.syncAllFromServer { transactionResult ->
                    _transactionSyncError.value = transactionResult.exceptionOrNull()?.let {
                        "Could not refresh transactions: ${it.localizedMessage ?: "Please try again."}"
                    }
                }
                _serverConnected.value = res.isSuccess
                if (res.isFailure) {
                    val error = res.exceptionOrNull()
                    if (error != null) reportOperationError(error)
                    else _operationErrorMessage.value = "Could not refresh data from the backend."
                } else {
                    _operationErrorMessage.value = null
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun clearOperationError() {
        _operationErrorMessage.value = null
    }

    fun clearOperationSuccess() {
        _operationSuccessMessage.value = null
    }

    private fun reportOperationError(error: Throwable) {
        if (error.localizedMessage?.contains("session has expired", ignoreCase = true) == true) {
            _isAuthenticated.value = false
            _serverToken.value = null
            _authPhase.value = AuthPhase.LOGIN
            _authErrorMessage.value = "Your session has expired. Please sign in again."
            TokenManager.clear()
            viewModelScope.launch { repository.clearAllUserData() }
        }
        _operationErrorMessage.value = when (error) {
            is SocketTimeoutException -> "The backend took too long to respond. Please try again."
            is UnknownHostException, is IOException -> "No internet connection or the backend is unavailable."
            else -> error.localizedMessage ?: "The request failed. Please try again."
        }
    }

    private fun runRepositoryOperation(operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operation()
                _operationErrorMessage.value = null
            } catch (error: Exception) {
                reportOperationError(error)
            }
        }
    }

    private fun runTransactionRepositoryOperation(
        operation: suspend () -> Unit,
        onComplete: (Boolean) -> Unit,
        isDelete: Boolean = false
    ) {
        if (_isTransactionOperationInProgress.value) {
            _operationErrorMessage.value = "A transaction change is already in progress."
            onComplete(false)
            return
        }

        viewModelScope.launch {
            _isTransactionOperationInProgress.value = true
            _isTransactionDeleteInProgress.value = isDelete
            var confirmed = false
            try {
                operation()
                confirmed = true
                _operationErrorMessage.value = null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                reportOperationError(error)
            } finally {
                _isTransactionOperationInProgress.value = false
                _isTransactionDeleteInProgress.value = false
            }

            onComplete(confirmed)
            if (confirmed) {
                val refreshResult = repository.syncWalletsFromServer()
                refreshResult.exceptionOrNull()?.let { error ->
                    reportOperationError(error)
                    _operationErrorMessage.value =
                        "The transaction change was confirmed, but wallet balances could not be refreshed. " +
                            (_operationErrorMessage.value ?: "")
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Authentication Operations
    // ─────────────────────────────────────────────────────────────────────────

    fun setAuthPhase(phase: AuthPhase) {
        _authErrorMessage.value = null
        _authSuccessMessage.value = null
        _authPhase.value = phase
    }

    fun dismissGmailNotification() {
        _gmailNotificationBanner.value = null
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun startOtpCooldown() {
        _otpCooldown.value = 45
        viewModelScope.launch {
            while (_otpCooldown.value > 0) {
                kotlinx.coroutines.delay(1000)
                _otpCooldown.value -= 1
            }
        }
    }

    fun loginUser(emailOrUsername: String, pass: String, rememberMe: Boolean = true) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authErrorMessage.value = null
            _authSuccessMessage.value = null
            val targetIdentifier = emailOrUsername.trim()

            if (targetIdentifier.isBlank() || pass.isBlank()) {
                _authErrorMessage.value = "Please enter both identifier and password"
                _isAuthenticating.value = false
                return@launch
            }

            try {
                val response = NetworkClient.apiService.login(
                    LoginRequest(identifier = targetIdentifier, password = pass)
                )
                val body = response.body()
                val loginData = body?.data
                if (response.isSuccessful && body?.success == true && loginData != null) {
                    establishSession(loginData, targetIdentifier)
                } else {
                    _authErrorMessage.value = apiResponseError(
                        response.code(),
                        body?.message,
                        response.errorBody()?.string()
                    )
                }
            } catch (error: Exception) {
                _authErrorMessage.value = networkError(error)
            } finally {
                _isAuthenticating.value = false
            }
        }
    }

    fun signupUser(name: String, username: String, email: String, pass: String, confirmPass: String) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authErrorMessage.value = null
            _authSuccessMessage.value = null

            val cleanName = name.trim()
            val cleanUsername = username.trim()
            val cleanEmail = email.trim()

            if (cleanName.isBlank()) {
                _authErrorMessage.value = "Name is required."
                _isAuthenticating.value = false
                return@launch
            }
            if (cleanUsername.isBlank()) {
                _authErrorMessage.value = "Username is required."
                _isAuthenticating.value = false
                return@launch
            }
            if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                _authErrorMessage.value = "Enter a valid email address."
                _isAuthenticating.value = false
                return@launch
            }
            if (pass.length < 8) {
                _authErrorMessage.value = "Password must contain at least 8 characters."
                _isAuthenticating.value = false
                return@launch
            }
            if (pass != confirmPass) {
                _authErrorMessage.value = "Passwords do not match."
                _isAuthenticating.value = false
                return@launch
            }

            try {
                val request = RegisterRequest(
                    name = cleanName,
                    username = cleanUsername,
                    email = cleanEmail,
                    password = pass
                )
                val response = NetworkClient.apiService.sendRegistrationOtp(request)
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    pendingRegistration = request
                    isRegistrationOtpFlow = true
                    pendingResetCode = null
                    _otpEmail.value = cleanEmail
                    _otpCooldown.value = 0
                    startOtpCooldown()
                    _authSuccessMessage.value = "Verification code sent. Check your email."
                    _authPhase.value = AuthPhase.OTP_VERIFICATION
                } else {
                    _authErrorMessage.value = apiResponseError(
                        response.code(),
                        body?.message,
                        response.errorBody()?.string()
                    )
                }
            } catch (error: Exception) {
                _authErrorMessage.value = networkError(error)
            } finally {
                _isAuthenticating.value = false
            }
        }
    }

    fun requestPasswordResetOtp(usernameOrEmail: String) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authErrorMessage.value = null
            _authSuccessMessage.value = null

            val target = usernameOrEmail.trim()
            if (target.isBlank()) {
                _authErrorMessage.value = "Please enter your username or registered email."
                _isAuthenticating.value = false
                return@launch
            }

            try {
                val response = NetworkClient.apiService.requestPasswordReset(ForgotPasswordRequest(email = target))
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    pendingRegistration = null
                    isRegistrationOtpFlow = false
                    pendingResetCode = null
                    _otpEmail.value = target
                    startOtpCooldown()
                    _authSuccessMessage.value = body.message ?: "Verification code sent. Check your email."
                    _authPhase.value = AuthPhase.OTP_VERIFICATION
                } else {
                    _authErrorMessage.value = apiResponseError(
                        response.code(),
                        body?.message,
                        response.errorBody()?.string()
                    )
                }
            } catch (error: Exception) {
                _authErrorMessage.value = networkError(error)
            } finally {
                _isAuthenticating.value = false
            }
        }
    }

    fun resendOtp() {
        if (_otpCooldown.value > 0) return
        viewModelScope.launch {
            try {
                val response = if (isRegistrationOtpFlow) {
                    val registration = pendingRegistration
                        ?: throw IllegalStateException("Registration details are no longer available. Please sign up again.")
                    NetworkClient.apiService.sendRegistrationOtp(registration)
                } else {
                    NetworkClient.apiService.requestPasswordReset(ForgotPasswordRequest(email = _otpEmail.value))
                }
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    startOtpCooldown()
                    _authErrorMessage.value = null
                    _authSuccessMessage.value = body.message ?: "A new verification code was sent."
                } else {
                    _authErrorMessage.value = apiResponseError(
                        response.code(),
                        body?.message,
                        response.errorBody()?.string()
                    )
                }
            } catch (error: Exception) {
                _authErrorMessage.value = networkError(error)
            }
        }
    }

    fun verifyOtpCode(enteredOtp: String) {
        val clean = enteredOtp.trim()
        if (clean.length != 6) {
            _authErrorMessage.value = "Please enter the complete 6-digit code."
            return
        }

        if (isRegistrationOtpFlow) {
            viewModelScope.launch {
                _isAuthenticating.value = true
                try {
                    val response = NetworkClient.apiService.verifyRegistrationOtp(
                        VerifyOtpRequest(email = _otpEmail.value, code = clean)
                    )
                    val body = response.body()
                    val loginData = body?.data
                    if (response.isSuccessful && body?.success == true && loginData != null) {
                        establishSession(loginData, _otpEmail.value)
                    } else {
                        _authErrorMessage.value = apiResponseError(
                            response.code(),
                            body?.message,
                            response.errorBody()?.string()
                        )
                    }
                } catch (error: Exception) {
                    _authErrorMessage.value = networkError(error)
                } finally {
                    _isAuthenticating.value = false
                }
            }
        } else {
            pendingResetCode = clean
            _authErrorMessage.value = null
            _authPhase.value = AuthPhase.CREATE_NEW_PASSWORD
        }
    }

    fun saveNewPassword(newPass: String, confirmPass: String) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authErrorMessage.value = null

            if (newPass.length < 8) {
                _authErrorMessage.value = "Password must contain at least 8 characters."
                _isAuthenticating.value = false
                return@launch
            }
            if (newPass != confirmPass) {
                _authErrorMessage.value = "Passwords do not match."
                _isAuthenticating.value = false
                return@launch
            }

            val resetCode = pendingResetCode
            if (resetCode.isNullOrBlank()) {
                _authErrorMessage.value = "Enter the verification code sent to your email."
                _isAuthenticating.value = false
                _authPhase.value = AuthPhase.OTP_VERIFICATION
                return@launch
            }

            try {
                val response = NetworkClient.apiService.resetPassword(
                    ResetPasswordRequest(
                        email = _otpEmail.value,
                        code = resetCode,
                        newPassword = newPass
                    )
                )
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    pendingResetCode = null
                    _authSuccessMessage.value = body.message ?: "Password updated. You can now sign in."
                    _authPhase.value = AuthPhase.PASSWORD_RESET_SUCCESS
                } else {
                    _authErrorMessage.value = apiResponseError(
                        response.code(),
                        body?.message,
                        response.errorBody()?.string()
                    )
                }
            } catch (error: Exception) {
                _authErrorMessage.value = networkError(error)
            } finally {
                _isAuthenticating.value = false
            }
        }
    }

    fun returnToLoginFromReset() {
        pendingResetCode = null
        _authPhase.value = AuthPhase.LOGIN
        _authSuccessMessage.value = "Password updated! You can now sign in."
    }

    private suspend fun establishSession(loginData: com.example.network.LoginData, identifier: String) {
        val token = loginData.token?.takeIf(String::isNotBlank)
            ?: throw IllegalStateException("The backend did not return an authentication token.")
        TokenManager.saveToken(token)
        _serverToken.value = token
        repository.clearAllUserData()

        val name = loginData.name ?: loginData.username ?: identifier.substringBefore("@")
        val email = loginData.email ?: identifier.takeIf { it.contains("@") }.orEmpty()
        val currencyCode = when (loginData.currency?.uppercase()) {
            "USD", "$" -> "USD"
            "EUR", "€" -> "EUR"
            "GBP", "£" -> "GBP"
            "JPY", "¥" -> "JPY"
            else -> "INR"
        }
        val currencySymbol = when (currencyCode) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            else -> "₹"
        }
        TokenManager.saveUserInfo(loginData.mongoId ?: loginData.id, name, email)
        repository.updateUserSettings(
            UserSettingsEntity(
                id = 1,
                name = name,
                email = email,
                currency = currencySymbol,
                currencyCode = currencyCode,
                theme = "dark",
                accentColor = loginData.accentColor ?: "purple",
                isGuest = false,
                gmailConnected = loginData.gmailConnected ?: false
            )
        )
        pendingRegistration = null
        isRegistrationOtpFlow = false
        _authErrorMessage.value = null
        _authSuccessMessage.value = null
        _isAuthenticated.value = true
        syncAllData()
    }

    private fun apiResponseError(code: Int, message: String?, errorBody: String?): String {
        if (!message.isNullOrBlank()) return message
        if (!errorBody.isNullOrBlank()) {
            val parsed = try {
                val json = JSONObject(errorBody)
                json.optString("message").ifBlank { json.optString("error") }
            } catch (_: Exception) {
                ""
            }
            if (parsed.isNotBlank()) return parsed
        }
        return when (code) {
            400 -> "The server rejected the request. Check the entered details."
            401 -> "Invalid credentials or your session has expired."
            403 -> "This action is not allowed for your account."
            404 -> "The requested service was not found."
            in 500..599 -> "The backend is temporarily unavailable. Please try again."
            else -> "Request failed (HTTP $code). Please try again."
        }
    }

    private fun networkError(error: Throwable): String = when (error) {
        is SocketTimeoutException -> "The backend took too long to respond. Please try again."
        is UnknownHostException, is IOException -> "No internet connection or the backend is unavailable."
        else -> error.localizedMessage ?: "The request failed. Please try again."
    }

    private fun currencyCodeForSymbol(symbol: String): String = when (symbol) {
        "$" -> "USD"
        "€" -> "EUR"
        "£" -> "GBP"
        "¥" -> "JPY"
        else -> "INR"
    }

    fun updateCardCustomization(
        walletId: String,
        theme: String,
        primaryColor: String,
        secondaryColor: String,
        accentColor: String,
        artwork: String,
        cardStyle: String
    ) {
        runRepositoryOperation {
            wallets.value.find { it.id == walletId }?.let { wallet ->
                val updated = wallet.copy(
                    cardTheme = theme,
                    designId = theme,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    accentColor = accentColor,
                    artwork = artwork,
                    cardStyle = cardStyle
                )
                repository.updateWallet(updated)
            }
        }
    }

    fun updateWallet(
        wallet: WalletEntity,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.updateWallet(wallet)
                _operationErrorMessage.value = null
                onComplete(true)
            } catch (error: Exception) {
                reportOperationError(error)
                onComplete(false)
            }
        }
    }

    fun loadCreditCardData(cardId: String) {
        viewModelScope.launch {
            _creditDataLoading.value = true
            try {
                refreshCreditCardData(cardId)
            } catch (error: Exception) {
                reportOperationError(error)
            } finally {
                _creditDataLoading.value = false
            }
        }
    }

    fun loadCreditCardsData(cardIds: List<String>) {
        viewModelScope.launch {
            _creditDataLoading.value = true
            try {
                cardIds.forEach { cardId -> refreshCreditCardData(cardId) }
            } catch (error: Exception) {
                reportOperationError(error)
            } finally {
                _creditDataLoading.value = false
            }
        }
    }

    fun generateCreditCardStatement(cardId: String) {
        viewModelScope.launch {
            _creditDataLoading.value = true
            try {
                repository.generateCreditCardStatement(cardId)
                refreshCreditCardData(cardId)
                _operationErrorMessage.value = null
            } catch (error: Exception) {
                reportOperationError(error)
            } finally {
                _creditDataLoading.value = false
            }
        }
    }

    private suspend fun refreshCreditCardData(cardId: String) {
        val statements = repository.getCreditCardStatements(cardId)
        val activity = repository.getCreditCardActivity(cardId)
        _creditStatements.value = _creditStatements.value + (cardId to statements)
        _creditActivity.value = _creditActivity.value + (cardId to activity)
    }

    fun recordCreditCardRepayment(
        cardId: String,
        sourceWalletId: String,
        amount: Double,
        statementId: String?,
        idempotencyKey: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (_creditRepaymentInProgress.value) {
            _operationErrorMessage.value = "A card repayment is already in progress."
            onComplete(false)
            return
        }
        viewModelScope.launch {
            _creditRepaymentInProgress.value = true
            var confirmed = false
            try {
                repository.recordCreditCardRepayment(
                    cardId,
                    sourceWalletId,
                    amount,
                    statementId,
                    idempotencyKey
                )
                confirmed = true
                _operationErrorMessage.value = null
                _operationSuccessMessage.value =
                    "Repayment recorded in the app. This does not confirm payment to the card issuer."
            } catch (error: Exception) {
                reportOperationError(error)
            } finally {
                _creditRepaymentInProgress.value = false
            }
            onComplete(confirmed)
            if (confirmed) loadCreditCardData(cardId)
        }
    }

    fun saveCardDisplayBalance(
        walletId: String,
        balance: Double,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.saveCardDisplayBalance(walletId, balance)
                _operationErrorMessage.value = null
                onComplete(true)
            } catch (error: Exception) {
                reportOperationError(error)
                onComplete(false)
            }
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _serverToken.value = null
        TokenManager.clear()
        pendingRegistration = null
        isRegistrationOtpFlow = false
        pendingResetCode = null
        _authPhase.value = AuthPhase.LOGIN
        viewModelScope.launch {
            repository.clearAllUserData()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // User Settings Updates
    // ─────────────────────────────────────────────────────────────────────────

    fun updateUserSettings(
        name: String,
        email: String,
        currency: String,
        theme: String,
        accentColor: String
    ) {
        runRepositoryOperation {
            val current = userSettings.value ?: UserSettingsEntity()
            repository.saveUserSettings(
                current.copy(
                    name = name,
                    email = email,
                    currency = currency,
                    currencyCode = currencyCodeForSymbol(currency),
                    theme = theme,
                    accentColor = accentColor
                )
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Transaction CRUD
    // ─────────────────────────────────────────────────────────────────────────

    fun addTransaction(
        type: String,
        amount: Double,
        category: String,
        description: String,
        date: String,
        isRecurring: Boolean,
        walletId: String,
        idempotencyKey: String? = null,
        onComplete: (Boolean) -> Unit = {}
    ) {
        runTransactionRepositoryOperation(operation = {
            val newTx = TransactionEntity(
                type = type,
                amount = amount,
                category = category,
                description = description,
                date = date,
                timestamp = System.currentTimeMillis(),
                isRecurring = isRecurring,
                walletId = walletId,
                transactionKind = if (wallets.value.find { it.id == walletId }
                        ?.cardType.equals("credit", ignoreCase = true)
                ) "credit_purchase" else "standard",
                idempotencyKey = idempotencyKey
            )
            repository.addTransaction(newTx)
            if (wallets.value.any {
                    it.id == walletId && it.cardType.equals("credit", ignoreCase = true)
                }
            ) {
                loadCreditCardData(walletId)
            }
        }, onComplete = onComplete)
    }

    fun updateTransaction(
        transaction: TransactionEntity,
        onComplete: (Boolean) -> Unit = {}
    ) {
        runTransactionRepositoryOperation(
            operation = {
                repository.updateTransaction(transaction)
                if (wallets.value.any {
                        it.id == transaction.walletId && it.cardType.equals("credit", ignoreCase = true)
                    }
                ) {
                    loadCreditCardData(transaction.walletId)
                }
            },
            onComplete = onComplete
        )
    }

    fun deleteTransaction(id: String, onComplete: (Boolean) -> Unit = {}) {
        runTransactionRepositoryOperation(
            operation = {
                val transaction = allTransactions.value.firstOrNull { it.id == id }
                repository.deleteTransaction(id)
                if (transaction != null && wallets.value.any {
                        it.id == transaction.walletId && it.cardType.equals("credit", ignoreCase = true)
                    }
                ) {
                    loadCreditCardData(transaction.walletId)
                }
            },
            onComplete = onComplete,
            isDelete = true
        )
    }

    fun recordCreditCardRefund(
        purchaseId: String,
        amount: Double,
        idempotencyKey: String,
        onComplete: (Boolean) -> Unit
    ) {
        val purchase = allTransactions.value.firstOrNull { it.id == purchaseId }
        if (purchase == null || purchase.transactionKind != "purchase") {
            _operationErrorMessage.value = "The original credit purchase is no longer available."
            onComplete(false)
            return
        }
        runTransactionRepositoryOperation(
            operation = {
                repository.recordCreditCardRefund(purchaseId, amount, idempotencyKey)
                loadCreditCardData(purchase.walletId)
            },
            onComplete = { confirmed ->
                if (confirmed) {
                    _operationSuccessMessage.value =
                        "Purchase refund recorded and linked to its original credit transaction."
                }
                onComplete(confirmed)
            }
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Wallet CRUD
    // ─────────────────────────────────────────────────────────────────────────

    fun addWallet(
        bankName: String,
        cardType: String,
        cardBrand: String,
        cardNumber: String,
        cardHolderName: String,
        expiryDate: String,
        initialBalance: Double,
        designId: String,
        primaryColor: String,
        secondaryColor: String
    ) {
        runRepositoryOperation {
            repository.addWallet(
                WalletEntity(
                    bankName = bankName,
                    cardType = cardType,
                    cardBrand = cardBrand,
                    cardNumber = cardNumber,
                    cardHolderName = cardHolderName,
                    expiryDate = expiryDate,
                    balance = initialBalance,
                    designId = designId,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
            )
        }
    }

    fun topUpWallet(walletId: String, amount: Double) {
        runRepositoryOperation { repository.topUpWallet(walletId, amount) }
    }

    fun deleteWallet(walletId: String) {
        runRepositoryOperation { repository.deleteWallet(walletId) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Budget & Goals CRUD
    // ─────────────────────────────────────────────────────────────────────────

    fun updateBudgetConfig(income: Double, limit: Double) {
        runRepositoryOperation { repository.updateBudgetConfig(income, limit) }
    }

    fun addGoal(title: String, amount: Double, medium: String) {
        runRepositoryOperation {
            val now = Calendar.getInstance()
            repository.addGoal(
                SavingsGoalEntity(
                    title = title,
                    amount = amount,
                    medium = medium,
                    completed = false,
                    month = now.get(Calendar.MONTH) + 1,
                    year = now.get(Calendar.YEAR)
                )
            )
        }
    }

    fun updateGoal(goal: SavingsGoalEntity) {
        runRepositoryOperation { repository.updateGoal(goal) }
    }

    fun toggleGoal(goalId: String, completed: Boolean) {
        runRepositoryOperation { repository.toggleGoalStatus(goalId, completed) }
    }

    fun deleteGoal(goalId: String) {
        runRepositoryOperation { repository.deleteGoal(goalId) }
    }

    fun updateBudgetLimits(income: Double, limit: Double) {
        updateBudgetConfig(income, limit)
    }

    fun clearAllLocalData() {
        viewModelScope.launch {
            repository.clearAllUserData()
        }
    }

    fun resetDemoData() {
        syncAllData()
    }

    fun fetchExpensesFromServer(token: String? = null) {
        syncAllData()
    }
}
