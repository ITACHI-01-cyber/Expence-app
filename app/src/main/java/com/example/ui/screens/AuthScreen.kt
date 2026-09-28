package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.viewmodel.AuthPhase
import com.example.viewmodel.ExpenseViewModel
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val authPhase by viewModel.authPhase.collectAsStateWithLifecycle()
    val otpEmail by viewModel.otpEmail.collectAsStateWithLifecycle()
    val activeOtpCode by viewModel.activeOtpCode.collectAsStateWithLifecycle()
    val gmailBanner by viewModel.gmailNotificationBanner.collectAsStateWithLifecycle()
    val isAuthenticating by viewModel.isAuthenticating.collectAsStateWithLifecycle()
    val authErrorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val authSuccessMessage by viewModel.authSuccessMessage.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_screen_root")
    ) {
        // ── 1. The Torii Archway Forest Anime Wallpaper as Background ──
        Image(
            painter = painterResource(id = R.drawable.auth_forest_bg),
            contentDescription = "Forest Archway Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ── 2. Cinematic Gradient Scrim (Dark tint at bottom for readable form controls) ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.20f),
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.65f),
                            Color(0xFF0D1411).copy(alpha = 0.90f),
                            Color(0xFF090E0C).copy(alpha = 0.96f)
                        )
                    )
                )
        )

        // ── 3. Interactive Pop-in Gmail Notification Banner (for OTP Verification) ──
        AnimatedVisibility(
            visible = gmailBanner != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E2621).copy(alpha = 0.96f),
                border = BorderStroke(1.dp, Color(0xFF4ADE80).copy(alpha = 0.5f)),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.dismissGmailNotification() }
                    .testTag("gmail_otp_notification_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEA4335)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "M",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Gmail • now",
                                color = Color(0xFF86EFAC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Security OTP",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "PathFinders Security Code: $gmailBanner",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap banner to dismiss",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // ── 4. Dynamic Phases Router ──
        AnimatedContent(
            targetState = authPhase,
            transitionSpec = {
                (slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn()) togetherWith
                        (slideOutHorizontally(targetOffsetX = { -it / 2 }) + fadeOut())
            },
            label = "auth_phase_transition",
            modifier = Modifier.fillMaxSize()
        ) { phase ->
            when (phase) {
                AuthPhase.WELCOME_SPLASH -> WelcomeSplashPhase(
                    onGoClick = { viewModel.setAuthPhase(AuthPhase.LANDING) }
                )

                AuthPhase.LANDING -> LandingPhase(
                    onSignUpClick = { viewModel.setAuthPhase(AuthPhase.SIGNUP) },
                    onLogInClick = { viewModel.setAuthPhase(AuthPhase.LOGIN) },
                    onGuestClick = { viewModel.exploreAsGuest() }
                )

                AuthPhase.LOGIN -> LoginPhase(
                    onBackClick = { viewModel.setAuthPhase(AuthPhase.LANDING) },
                    onForgotPasswordClick = { viewModel.setAuthPhase(AuthPhase.FORGOT_PASSWORD_EMAIL) },
                    onSignUpClick = { viewModel.setAuthPhase(AuthPhase.SIGNUP) },
                    onLoginSubmit = { user, pass, rem -> viewModel.loginUser(user, pass, rem) },
                    onSocialLogin = { viewModel.socialLogin(it) },
                    isAuthenticating = isAuthenticating,
                    errorMessage = authErrorMessage,
                    successMessage = authSuccessMessage
                )

                AuthPhase.SIGNUP -> SignUpPhase(
                    onBackClick = { viewModel.setAuthPhase(AuthPhase.LOGIN) },
                    onLogInClick = { viewModel.setAuthPhase(AuthPhase.LOGIN) },
                    onSignUpSubmit = { first, last, email, pass ->
                        viewModel.signupUser(first, last, email, pass)
                    },
                    onSocialLogin = { viewModel.socialLogin(it) },
                    isAuthenticating = isAuthenticating,
                    errorMessage = authErrorMessage
                )

                AuthPhase.FORGOT_PASSWORD_EMAIL -> ForgotPasswordEmailPhase(
                    initialEmail = otpEmail,
                    onBackClick = { viewModel.setAuthPhase(AuthPhase.LOGIN) },
                    onSendOtp = { email -> viewModel.sendGmailOtp(email) }
                )

                AuthPhase.FORGOT_PASSWORD_OTP -> ForgotPasswordOtpPhase(
                    targetEmail = otpEmail,
                    generatedOtp = activeOtpCode,
                    onBackClick = { viewModel.setAuthPhase(AuthPhase.FORGOT_PASSWORD_EMAIL) },
                    onResendOtp = { viewModel.sendGmailOtp(otpEmail) },
                    onVerifyOtp = { otp -> viewModel.verifyOtp(otp) }
                )

                AuthPhase.FORGOT_PASSWORD_NEW_PASS -> ForgotPasswordNewPassPhase(
                    onBackClick = { viewModel.setAuthPhase(AuthPhase.FORGOT_PASSWORD_OTP) },
                    onResetPassword = { pass -> viewModel.resetPasswordAndLogin(pass) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Phase 1: Welcome Splash ("Welcome to RACK" + "Records all in one App" + GO button)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun WelcomeSplashPhase(
    onGoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.18f),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
                modifier = Modifier.size(56.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_rabbit_logo),
                    contentDescription = "RACK Mascot Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Welcome to",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "RACK",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        }

        // Bottom area: Headline + GO Circle Button (traveling text removed)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Records all in one App",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Circular GO Button with Upward Chevron
            Surface(
                onClick = onGoClick,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.22f),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f)),
                modifier = Modifier
                    .size(68.dp)
                    .testTag("welcome_go_button")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Go",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "GO",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Phase 2: Landing Screen ("View detailed trail maps..." + "Sign Up" + Log In link)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LandingPhase(
    onSignUpClick: () -> Unit,
    onLogInClick: () -> Unit,
    onGuestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Take control of your finances.\nRecords all in one App.",
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Prominent Light Frosted "Sign Up" Button
            Button(
                onClick = onSignUpClick,
                shape = RoundedCornerShape(100),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD3D8D5).copy(alpha = 0.85f),
                    contentColor = Color(0xFF1E2621)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("landing_signup_btn")
            ) {
                Text(
                    text = "Sign Up",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // "Already have a account? Log in"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.clickable(onClick = onLogInClick)
            ) {
                Text(
                    text = "Already have a account? ",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.5.sp
                )
                Text(
                    text = "Log In",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.testTag("landing_login_link")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Guest Access
            Text(
                text = "Explore as Guest",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable(onClick = onGuestClick)
                    .padding(4.dp)
                    .testTag("landing_guest_link")
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Phase 3: Login Screen ("Welcome BACK!" + Username, Password, Remember Me, Forgot Pass)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun LoginPhase(
    onBackClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLoginSubmit: (String, String, Boolean) -> Unit,
    onSocialLogin: (String) -> Unit,
    isAuthenticating: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("bvivek514@gmail.com") }
    var password by remember { mutableStateOf("123456") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 26.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Top Back Arrow Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("login_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // "Welcome BACK!"
            Text(
                text = "Welcome",
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "BACK!",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Continue your adventure",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Form Inputs: Username
            AuthTranslucentField(
                value = username,
                onValueChange = { username = it },
                placeholder = "Username or Email",
                leadingIcon = Icons.Default.Person,
                testTag = "login_username_field"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Field
            AuthTranslucentField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible },
                leadingIcon = Icons.Default.Lock,
                testTag = "login_password_field"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Row: "Remember me" + "Forgot Password?"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { rememberMe = !rememberMe }
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color.White,
                            checkmarkColor = Color(0xFF1E2621),
                            uncheckedColor = Color.White.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Remember me",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "Forgot Password?",
                    color = Color.White.copy(alpha = 0.90f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable(onClick = onForgotPasswordClick)
                        .padding(vertical = 4.dp)
                        .testTag("login_forgot_password_link")
                )
            }

            if (!successMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF4ADE80).copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, Color(0xFF4ADE80).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = successMessage,
                        color = Color(0xFF86EFAC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEA4335).copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, Color(0xFFEA4335).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFFB4AB),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // "Log In" Pill Button
            Button(
                onClick = {
                    if (username.isBlank()) {
                        Toast.makeText(context, "Please enter username or email", Toast.LENGTH_SHORT).show()
                    } else {
                        onLoginSubmit(username, password, rememberMe)
                    }
                },
                enabled = !isAuthenticating,
                shape = RoundedCornerShape(100),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD3D8D5).copy(alpha = 0.90f),
                    contentColor = Color(0xFF1E2621)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_btn")
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color(0xFF1E2621),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Log In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Bottom link: "Don't have an account? Sign Up"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            Text(
                text = "Sign Up",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable(onClick = onSignUpClick)
                    .testTag("login_to_signup_link")
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Phase 4: Sign Up Screen ("Create account" + Name, Lastname, Email, Passwords)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SignUpPhase(
    onBackClick: () -> Unit,
    onLogInClick: () -> Unit,
    onSignUpSubmit: (String, String, String, String) -> Unit,
    onSocialLogin: (String) -> Unit,
    isAuthenticating: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("Vivek") }
    var lastName by remember { mutableStateOf("Bhardwaj") }
    var email by remember { mutableStateOf("bhardwajvivek226@gmail.com") }
    var password by remember { mutableStateOf("secret123") }
    var confirmPassword by remember { mutableStateOf("secret123") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 26.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("signup_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create account",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Two-column Name & Lastname Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AuthTranslucentField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        placeholder = "Name",
                        testTag = "signup_name_field"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    AuthTranslucentField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = "Lastname",
                        testTag = "signup_lastname_field"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Email Field
            AuthTranslucentField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                testTag = "signup_email_field"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            AuthTranslucentField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible },
                leadingIcon = Icons.Default.Lock,
                testTag = "signup_password_field"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password Field
            AuthTranslucentField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm Password",
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible },
                leadingIcon = Icons.Default.Lock,
                testTag = "signup_confirm_password_field"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Legal disclaimer
            Text(
                text = "By continuing, I agree to PathFinders Terms of Service and acknowledge the Privacy Policy",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 10.5.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEA4335).copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, Color(0xFFEA4335).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFFB4AB),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // "Sign Up" Pill Button
            Button(
                onClick = {
                    if (email.isBlank()) {
                        Toast.makeText(context, "Please enter an email", Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    } else {
                        onSignUpSubmit(firstName, lastName, email, password)
                    }
                },
                enabled = !isAuthenticating,
                shape = RoundedCornerShape(100),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD3D8D5).copy(alpha = 0.90f),
                    contentColor = Color(0xFF1E2621)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("signup_submit_btn")
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color(0xFF1E2621),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Bottom link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            Text(
                text = "Log In",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable(onClick = onLogInClick)
                    .testTag("signup_to_login_link")
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Phase 5A: Forgot Password - Step 1 (Enter Gmail to receive OTP)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ForgotPasswordEmailPhase(
    initialEmail: String,
    onBackClick: () -> Unit,
    onSendOtp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var emailInput by remember { mutableStateOf(initialEmail) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 26.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp).testTag("forgot_pass_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Forgot",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "PASSWORD?",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Enter your registered Gmail address. We will generate and dispatch an instant 6-digit OTP code to verify your identity.",
                color = Color.White.copy(alpha = 0.80f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Gmail Address Input
            AuthTranslucentField(
                value = emailInput,
                onValueChange = { emailInput = it },
                placeholder = "yourname@gmail.com",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                testTag = "forgot_pass_email_input"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (emailInput.isBlank()) {
                        Toast.makeText(context, "Please enter your Gmail address", Toast.LENGTH_SHORT).show()
                    } else {
                        onSendOtp(emailInput)
                        Toast.makeText(context, "6-digit OTP code dispatched to $emailInput", Toast.LENGTH_LONG).show()
                    }
                },
                shape = RoundedCornerShape(100),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD3D8D5).copy(alpha = 0.90f),
                    contentColor = Color(0xFF1E2621)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("send_otp_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFF1E2621),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Send Gmail OTP Code",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Back to Log In",
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable(onClick = onBackClick)
                    .padding(8.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Phase 5B: Forgot Password - Step 2 (Enter & Verify 6-digit Gmail OTP)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ForgotPasswordOtpPhase(
    targetEmail: String,
    generatedOtp: String,
    onBackClick: () -> Unit,
    onResendOtp: () -> Unit,
    onVerifyOtp: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var otpText by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(50) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000L)
            countdown--
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 26.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp).testTag("otp_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Verify",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "GMAIL OTP",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter the 6-digit verification code sent to:",
                color = Color.White.copy(alpha = 0.80f),
                fontSize = 13.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Email Chip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFF86EFAC),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = targetEmail,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 6-digit OTP Display Boxes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Focus hidden field
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 6) {
                    val char = if (i < otpText.length) otpText[i].toString() else ""
                    val isCurrent = i == otpText.length

                    Box(
                        modifier = Modifier
                            .size(width = 46.dp, height = 54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isError) Color(0xFFEF4444) else if (isCurrent) Color(0xFF86EFAC) else Color.White.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real Keyboard Input Field for the OTP
            BasicTextField(
                value = otpText,
                onValueChange = {
                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                        otpText = it
                        isError = false
                        if (it.length == 6) {
                            val success = onVerifyOtp(it)
                            if (!success) {
                                isError = true
                                Toast.makeText(context, "Invalid OTP code. Try $generatedOtp", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .testTag("otp_hidden_input")
            )

            // Auto-fill button helper (makes demonstration and testing instantaneous!)
            Surface(
                onClick = {
                    otpText = generatedOtp
                    isError = false
                    Toast.makeText(context, "Auto-filled OTP: $generatedOtp", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF86EFAC).copy(alpha = 0.18f),
                border = BorderStroke(1.dp, Color(0xFF86EFAC).copy(alpha = 0.4f)),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("autofill_otp_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF86EFAC),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Quick Fill OTP ($generatedOtp)",
                        color = Color(0xFF86EFAC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Verify Button
            Button(
                onClick = {
                    if (otpText.length < 6) {
                        Toast.makeText(context, "Please enter all 6 digits", Toast.LENGTH_SHORT).show()
                    } else {
                        val success = onVerifyOtp(otpText)
                        if (!success) {
                            isError = true
                            Toast.makeText(context, "Incorrect OTP. Code is $generatedOtp", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                shape = RoundedCornerShape(100),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD3D8D5).copy(alpha = 0.90f),
                    contentColor = Color(0xFF1E2621)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("verify_otp_submit_btn")
            ) {
                Text(
                    text = "Verify Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Resend OTP Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (countdown > 0) {
                    Text(
                        text = "Resend OTP in 00:${countdown.toString().padStart(2, '0')}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "Didn't receive code? ",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Resend OTP",
                        color = Color(0xFF86EFAC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .clickable {
                                countdown = 50
                                onResendOtp()
                                Toast.makeText(context, "New OTP code generated and sent!", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("resend_otp_btn")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Phase 5C: Forgot Password - Step 3 (Set New Password)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ForgotPasswordNewPassPhase(
    onBackClick: () -> Unit,
    onResetPassword: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 26.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp).testTag("new_pass_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Set New",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = "PASSWORD",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create a secure new password for your account to regain full access.",
                color = Color.White.copy(alpha = 0.80f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            AuthTranslucentField(
                value = newPassword,
                onValueChange = { newPassword = it },
                placeholder = "New Password",
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible },
                leadingIcon = Icons.Default.Lock,
                testTag = "new_password_field"
            )

            Spacer(modifier = Modifier.height(14.dp))

            AuthTranslucentField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm New Password",
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible },
                leadingIcon = Icons.Default.Lock,
                testTag = "confirm_new_password_field"
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (newPassword.length < 4) {
                        Toast.makeText(context, "Password should be at least 4 characters", Toast.LENGTH_SHORT).show()
                    } else if (newPassword != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                        onResetPassword(newPassword)
                    }
                },
                shape = RoundedCornerShape(100),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD3D8D5).copy(alpha = 0.90f),
                    contentColor = Color(0xFF1E2621)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_new_pass_btn")
            ) {
                Text(
                    text = "Reset Password & Log In",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Common Auth Reusable Components: Translucent Glass Fields & Social Buttons
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AuthTranslucentField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF26332C).copy(alpha = 0.55f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 14.sp
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(Color.White),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isPassword && onTogglePassword != null) {
                IconButton(
                    onClick = onTogglePassword,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SocialLoginRow(
    onSocialClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Facebook Button
        SocialIconButton(
            name = "Facebook",
            onClick = {
                Toast.makeText(context, "Connecting with Facebook...", Toast.LENGTH_SHORT).show()
                onSocialClick("Facebook")
            },
            testTag = "social_facebook_btn"
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1877F2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "f",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(22.dp))

        // Google Button
        SocialIconButton(
            name = "Google",
            onClick = {
                Toast.makeText(context, "Logged in via Google (bhardwajvivek226@gmail.com)", Toast.LENGTH_SHORT).show()
                onSocialClick("Google")
            },
            testTag = "social_google_btn"
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    color = Color(0xFF4285F4),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(22.dp))

        // Apple Button
        SocialIconButton(
            name = "Apple",
            onClick = {
                Toast.makeText(context, "Logged in via Apple ID", Toast.LENGTH_SHORT).show()
                onSocialClick("Apple")
            },
            testTag = "social_apple_btn"
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun SocialIconButton(
    name: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        modifier = modifier
            .size(48.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
