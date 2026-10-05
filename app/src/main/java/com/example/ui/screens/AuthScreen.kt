package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.viewmodel.AuthPhase
import com.example.viewmodel.ExpenseViewModel

/**
 * Premium Fintech Dark Authentication Experience
 * - Strict username/id + password database authentication
 * - NO social logins
 * - Complete 6-phase flow:
 *   1. LOGIN
 *   2. SIGNUP
 *   3. FORGOT PASSWORD
 *   4. OTP VERIFICATION (6-digit code with resend timer)
 *   5. CREATE NEW PASSWORD
 *   6. PASSWORD RESET SUCCESS
 */
@Composable
fun AuthScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val authPhase by viewModel.authPhase.collectAsStateWithLifecycle()
    val isAuthenticating by viewModel.isAuthenticating.collectAsStateWithLifecycle()
    val authErrorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val authSuccessMessage by viewModel.authSuccessMessage.collectAsStateWithLifecycle()
    val otpEmail by viewModel.otpEmail.collectAsStateWithLifecycle()
    val activeOtpCode by viewModel.activeOtpCode.collectAsStateWithLifecycle()
    val otpCooldown by viewModel.otpCooldown.collectAsStateWithLifecycle()
    val gmailBanner by viewModel.gmailNotificationBanner.collectAsStateWithLifecycle()

    val darkBg = Color(0xFF090A10)
    val cardSurface = Color(0xFF11131E)
    val borderColor = Color(0xFF1E2333)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("auth_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        // Batman background image (Image 1)
        Image(
            painter = painterResource(id = R.drawable.auth_batman_bg),
            contentDescription = "Authentication Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradient scrim overlay to maintain high contrast and readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color(0xFF090A10).copy(alpha = 0.65f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // OTP Banner simulation for testing convenience
        AnimatedVisibility(
            visible = authPhase == AuthPhase.OTP_VERIFICATION && activeOtpCode.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Email Service: Verification Code Sent",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "Your OTP is: $activeOtpCode",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Centered Content (Mobile-first, desktop width capped at 440.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = authPhase,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally { width -> width / 3 } + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut(tween(200)))
                    }
                },
                label = "auth_phase_transition"
            ) { phase ->
                when (phase) {
                    AuthPhase.LOGIN -> LoginView(
                        isAuthenticating = isAuthenticating,
                        errorMessage = authErrorMessage,
                        successMessage = authSuccessMessage,
                        onLogin = { id, pass -> viewModel.loginUser(id, pass) },
                        onForgotPassword = { viewModel.setAuthPhase(AuthPhase.FORGOT_PASSWORD) },
                        onCreateAccount = { viewModel.setAuthPhase(AuthPhase.SIGNUP) },
                        onClearError = { viewModel.clearAuthError() }
                    )

                    AuthPhase.SIGNUP -> SignUpView(
                        isAuthenticating = isAuthenticating,
                        errorMessage = authErrorMessage,
                        onSignUp = { user, email, pass, confirm ->
                            viewModel.signupUser(user, email, pass, confirm)
                        },
                        onBackToLogin = { viewModel.setAuthPhase(AuthPhase.LOGIN) },
                        onClearError = { viewModel.clearAuthError() }
                    )

                    AuthPhase.FORGOT_PASSWORD -> ForgotPasswordView(
                        isAuthenticating = isAuthenticating,
                        errorMessage = authErrorMessage,
                        onSendOtp = { identifier -> viewModel.requestPasswordResetOtp(identifier) },
                        onBackToLogin = { viewModel.setAuthPhase(AuthPhase.LOGIN) },
                        onClearError = { viewModel.clearAuthError() }
                    )

                    AuthPhase.OTP_VERIFICATION -> OtpVerificationView(
                        targetEmail = otpEmail,
                        cooldownSeconds = otpCooldown,
                        errorMessage = authErrorMessage,
                        onVerifyOtp = { code -> viewModel.verifyOtpCode(code) },
                        onResendOtp = { viewModel.resendOtp() },
                        onBack = { viewModel.setAuthPhase(AuthPhase.FORGOT_PASSWORD) },
                        onClearError = { viewModel.clearAuthError() }
                    )

                    AuthPhase.CREATE_NEW_PASSWORD -> CreateNewPasswordView(
                        isAuthenticating = isAuthenticating,
                        errorMessage = authErrorMessage,
                        onSavePassword = { newPass, confirmPass ->
                            viewModel.saveNewPassword(newPass, confirmPass)
                        },
                        onBack = { viewModel.setAuthPhase(AuthPhase.OTP_VERIFICATION) },
                        onClearError = { viewModel.clearAuthError() }
                    )

                    AuthPhase.PASSWORD_RESET_SUCCESS -> PasswordResetSuccessView(
                        onBackToLogin = { viewModel.returnToLoginFromReset() }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. LOGIN SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LoginView(
    isAuthenticating: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onLogin: (String, String) -> Unit,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit,
    onClearError: () -> Unit
) {
    var identifier by remember { mutableStateOf("vivek123") }
    var password by remember { mutableStateOf("Password@123") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // App Logo (Second Image)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0B061A),
            border = BorderStroke(2.dp, Color(0xFFA855F7)),
            shadowElevation = 10.dp,
            modifier = Modifier.size(66.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_ghost_skull_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome Back",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Success message banner if navigated from signup or password reset
        if (!successMessage.isNullOrBlank()) {
            SuccessBanner(message = successMessage)
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Inline error message banner
        if (!errorMessage.isNullOrBlank()) {
            ErrorBanner(message = errorMessage)
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Username / ID Field
        FintechInputField(
            label = "Username / ID",
            value = identifier,
            onValueChange = {
                identifier = it
                onClearError()
            },
            placeholder = "Enter your username or ID",
            leadingIcon = Icons.Default.Person,
            imeAction = ImeAction.Next,
            testTag = "login_username_input"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Password Field
        FintechInputField(
            label = "Password",
            value = password,
            onValueChange = {
                password = it
                onClearError()
            },
            placeholder = "Enter your password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            imeAction = ImeAction.Done,
            onDone = {
                if (!isAuthenticating) onLogin(identifier, password)
            },
            testTag = "login_password_input"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Forgot Password Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Forgot password?",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF38BDF8),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onForgotPassword)
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("login_forgot_password_link")
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign In Button
        FintechPrimaryButton(
            text = if (isAuthenticating) "Signing in..." else "Sign In",
            isLoading = isAuthenticating,
            onClick = { onLogin(identifier, password) },
            testTag = "login_submit_btn"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Secondary Navigation: Don't have an account? Create account
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account? ",
                fontSize = 13.5.sp,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = "Create account",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onCreateAccount)
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("login_create_account_link")
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. SIGN UP SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SignUpView(
    isAuthenticating: Boolean,
    errorMessage: String?,
    onSignUp: (String, String, String, String) -> Unit,
    onBackToLogin: () -> Unit,
    onClearError: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Navigation Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                onClick = onBackToLogin,
                shape = CircleShape,
                color = Color(0xFF161824),
                border = BorderStroke(1.dp, Color(0xFF252A3C)),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("signup_back_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Login",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // App Logo (Second Image)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0B061A),
            border = BorderStroke(2.dp, Color(0xFFA855F7)),
            shadowElevation = 8.dp,
            modifier = Modifier.size(60.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_ghost_skull_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Let's Get Started",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Create your account to start managing your finances.",
            fontSize = 13.5.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!errorMessage.isNullOrBlank()) {
            ErrorBanner(message = errorMessage)
            Spacer(modifier = Modifier.height(14.dp))
        }

        FintechInputField(
            label = "Username / User ID",
            value = username,
            onValueChange = {
                username = it
                onClearError()
            },
            placeholder = "Choose a unique username",
            leadingIcon = Icons.Default.Person,
            imeAction = ImeAction.Next,
            testTag = "signup_username_input"
        )

        Spacer(modifier = Modifier.height(14.dp))

        FintechInputField(
            label = "Email Address",
            value = email,
            onValueChange = {
                email = it
                onClearError()
            },
            placeholder = "name@example.com",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            testTag = "signup_email_input"
        )

        Spacer(modifier = Modifier.height(14.dp))

        FintechInputField(
            label = "Password",
            value = password,
            onValueChange = {
                password = it
                onClearError()
            },
            placeholder = "Minimum 8 characters",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            imeAction = ImeAction.Next,
            testTag = "signup_password_input"
        )

        Spacer(modifier = Modifier.height(14.dp))

        FintechInputField(
            label = "Confirm Password",
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                onClearError()
            },
            placeholder = "Re-enter your password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onTogglePasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
            imeAction = ImeAction.Done,
            onDone = {
                if (!isAuthenticating) onSignUp(username, email, password, confirmPassword)
            },
            testTag = "signup_confirm_password_input"
        )

        Spacer(modifier = Modifier.height(24.dp))

        FintechPrimaryButton(
            text = if (isAuthenticating) "Creating Account..." else "Create Account",
            isLoading = isAuthenticating,
            onClick = { onSignUp(username, email, password, confirmPassword) },
            testTag = "signup_submit_btn"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account? ",
                fontSize = 13.5.sp,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = "Sign In",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onBackToLogin)
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("signup_sign_in_link")
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. FORGOT PASSWORD SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ForgotPasswordView(
    isAuthenticating: Boolean,
    errorMessage: String?,
    onSendOtp: (String) -> Unit,
    onBackToLogin: () -> Unit,
    onClearError: () -> Unit
) {
    var identifier by remember { mutableStateOf("bhardwajvivek226@gmail.com") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                onClick = onBackToLogin,
                shape = CircleShape,
                color = Color(0xFF161824),
                border = BorderStroke(1.dp, Color(0xFF252A3C)),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("forgot_back_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Logo (Second Image)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0B061A),
            border = BorderStroke(2.dp, Color(0xFFA855F7)),
            shadowElevation = 8.dp,
            modifier = Modifier.size(60.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_ghost_skull_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Forgot Password?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enter your username or registered email address and we'll send you a verification code.",
            fontSize = 13.5.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!errorMessage.isNullOrBlank()) {
            ErrorBanner(message = errorMessage)
            Spacer(modifier = Modifier.height(14.dp))
        }

        FintechInputField(
            label = "Username / Email",
            value = identifier,
            onValueChange = {
                identifier = it
                onClearError()
            },
            placeholder = "Enter your username or email",
            leadingIcon = Icons.Default.Email,
            imeAction = ImeAction.Done,
            onDone = {
                if (!isAuthenticating) onSendOtp(identifier)
            },
            testTag = "forgot_identifier_input"
        )

        Spacer(modifier = Modifier.height(24.dp))

        FintechPrimaryButton(
            text = if (isAuthenticating) "Sending Code..." else "Send OTP",
            isLoading = isAuthenticating,
            onClick = { onSendOtp(identifier) },
            testTag = "forgot_send_otp_btn"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Back to Login",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF94A3B8),
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onBackToLogin)
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .testTag("forgot_back_to_login_link")
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. OTP VERIFICATION SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun OtpVerificationView(
    targetEmail: String,
    cooldownSeconds: Int,
    errorMessage: String?,
    onVerifyOtp: (String) -> Unit,
    onResendOtp: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit
) {
    var otpDigits by remember { mutableStateOf(List(6) { "" }) }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequesters.firstOrNull()?.requestFocus()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = Color(0xFF161824),
                border = BorderStroke(1.dp, Color(0xFF252A3C)),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("otp_back_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF38BDF8).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MarkEmailRead,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Verify Your Email",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enter the 6-digit verification code sent to your registered email.",
            fontSize = 13.5.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )

        if (targetEmail.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = targetEmail,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        if (!errorMessage.isNullOrBlank()) {
            ErrorBanner(message = errorMessage)
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 6-digit OTP Box Inputs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            otpDigits.forEachIndexed { index, digit ->
                Box(
                    modifier = Modifier
                        .size(width = 46.dp, height = 56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF131520))
                        .border(
                            width = if (digit.isNotEmpty()) 1.5.dp else 1.dp,
                            color = if (digit.isNotEmpty()) Color(0xFF38BDF8) else Color(0xFF242A3D),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = digit,
                        onValueChange = { newVal ->
                            onClearError()
                            val cleanVal = newVal.filter { it.isDigit() }.takeLast(1)
                            val updated = otpDigits.toMutableList()
                            updated[index] = cleanVal
                            otpDigits = updated

                            if (cleanVal.isNotEmpty() && index < 5) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = if (index == 5) ImeAction.Done else ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                val full = otpDigits.joinToString("")
                                if (full.length == 6) onVerifyOtp(full)
                            }
                        ),
                        cursorBrush = SolidColor(Color(0xFF38BDF8)),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 12.dp)
                            .focusRequester(focusRequesters[index])
                            .testTag("otp_digit_input_$index")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        FintechPrimaryButton(
            text = "Verify OTP",
            onClick = {
                val fullCode = otpDigits.joinToString("")
                onVerifyOtp(fullCode)
            },
            testTag = "verify_otp_btn"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Timer & Resend Option
        if (cooldownSeconds > 0) {
            val formattedTime = String.format("00:%02d", cooldownSeconds)
            Text(
                text = "Resend in $formattedTime",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Didn't receive the code? ",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "Resend OTP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onResendOtp)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("resend_otp_btn")
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. CREATE NEW PASSWORD SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CreateNewPasswordView(
    isAuthenticating: Boolean,
    errorMessage: String?,
    onSavePassword: (String, String) -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = Color(0xFF161824),
                border = BorderStroke(1.dp, Color(0xFF252A3C)),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("new_pass_back_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF38BDF8).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create New Password",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your new password must be different from your previous password.",
            fontSize = 13.5.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!errorMessage.isNullOrBlank()) {
            ErrorBanner(message = errorMessage)
            Spacer(modifier = Modifier.height(14.dp))
        }

        FintechInputField(
            label = "New Password",
            value = newPassword,
            onValueChange = {
                newPassword = it
                onClearError()
            },
            placeholder = "Minimum 8 characters",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = newPasswordVisible,
            onTogglePasswordVisibility = { newPasswordVisible = !newPasswordVisible },
            imeAction = ImeAction.Next,
            testTag = "new_password_input"
        )

        Spacer(modifier = Modifier.height(14.dp))

        FintechInputField(
            label = "Confirm New Password",
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                onClearError()
            },
            placeholder = "Re-enter new password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onTogglePasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
            imeAction = ImeAction.Done,
            onDone = {
                if (!isAuthenticating) onSavePassword(newPassword, confirmPassword)
            },
            testTag = "confirm_new_password_input"
        )

        Spacer(modifier = Modifier.height(24.dp))

        FintechPrimaryButton(
            text = if (isAuthenticating) "Saving Password..." else "Save Password",
            isLoading = isAuthenticating,
            onClick = { onSavePassword(newPassword, confirmPassword) },
            testTag = "save_password_btn"
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. PASSWORD RESET SUCCESS SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PasswordResetSuccessView(
    onBackToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                .border(2.dp, Color(0xFF10B981), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Password Updated",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your password has been successfully changed.",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        FintechPrimaryButton(
            text = "Back to Login",
            onClick = onBackToLogin,
            testTag = "success_back_to_login_btn"
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REUSABLE FINTECH FORM CONTROLS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FintechInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onDone: () -> Unit = {},
    testTag: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFCBD5E1),
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF121420),
            border = BorderStroke(1.dp, Color(0xFF222738)),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(19.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                            imeAction = imeAction
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onDone() }
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        ),
                        cursorBrush = SolidColor(Color(0xFF38BDF8)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(testTag)
                    )
                }

                if (isPassword) {
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FintechPrimaryButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    testTag: String = ""
) {
    Button(
        onClick = { if (!isLoading) onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF090A10)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color(0xFF090A10),
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DangerRed.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = DangerRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                fontSize = 12.5.sp,
                color = Color(0xFFFCA5A5),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SuccessBanner(message: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SuccessGreen.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                fontSize = 12.5.sp,
                color = Color(0xFF86EFAC),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
