package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.analytics.calculateCreditCardSummary
import com.example.model.DashboardSummary
import com.example.model.WalletEntity
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.getCardGradient
import com.example.ui.theme.parseColorSafe

@Composable
fun FintechBalanceHeader(
    summary: DashboardSummary,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var isBalanceVisible by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Total Balance",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textMutedLavender,
                letterSpacing = 0.2.sp
            )

            Surface(
                onClick = { isBalanceVisible = !isBalanceVisible },
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("toggle_balance_visibility_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Balance Visibility",
                        tint = colors.textMutedLavender,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        AnimatedContent(
            targetState = isBalanceVisible,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "balance_toggle_anim"
        ) { visible ->
            if (visible) {
                Text(
                    text = "$currencySymbol ${String.format("%,.2f", summary.availableBalance)}",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textCrispWhite,
                    letterSpacing = (-1.0).sp,
                    modifier = Modifier.testTag("hero_total_balance_text")
                )
            } else {
                Text(
                    text = "$currencySymbol ••••••••",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textCrispWhite,
                    letterSpacing = 2.sp,
                    modifier = Modifier.testTag("hero_total_balance_text")
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Income
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.surfaceBorder),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Income",
                            tint = SuccessGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Income",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textMutedLavender
                        )
                        Text(
                            text = if (isBalanceVisible) "$currencySymbol${String.format("%,.0f", summary.monthlyIncome)}" else "••••",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textCrispWhite
                        )
                    }
                }
            }

            // Spent
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.surfaceBorder),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(DangerRed.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Spent",
                            tint = DangerRed,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Spent",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textMutedLavender
                        )
                        Text(
                            text = if (isBalanceVisible) "$currencySymbol${String.format("%,.0f", summary.monthlySpent)}" else "••••",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textCrispWhite
                        )
                    }
                }
            }
        }
    }
}

/**
 * Premium Card Sleeve Presentation with 3D Pop-out & Flip Animation:
 * - Card sits partially inside textured dark leather card sleeve
 * - Tap card to pop out & inspect Front and Back side
 * - Shows distinct payment type badges (UPI, CASH, SAVINGS, DEBIT, CREDIT)
 * - Saved card customization choices propagate instantly
 */
@Composable
fun FintechDigitalCard(
    wallets: List<WalletEntity>,
    currencySymbol: String,
    onAddCard: () -> Unit,
    onTopUp: (WalletEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var activeIndex by remember(wallets) { mutableIntStateOf(0) }
    var isCardFrozen by remember { mutableStateOf(false) }
    var showFullNumbers by remember { mutableStateOf(false) }
    var isPoppedOutModalOpen by remember { mutableStateOf(false) }
    var isFlippedBack by remember { mutableStateOf(false) }

    val safeIndex = if (wallets.isNotEmpty()) activeIndex.coerceIn(0, wallets.size - 1) else 0
    val activeWallet = wallets.getOrNull(safeIndex)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeWallet == null) {
            // Empty State Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14171F)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clickable { onAddCard() }
                    .testTag("empty_digital_card")
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.10f),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Card",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            text = "Add Your Payment Card",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Connect debit or credit card to track balances",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            // Premium Leather Card Pocket Container with swipe support
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(wallets.size) {
                        if (wallets.size > 1) {
                            var totalDrag = 0f
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (totalDrag < -60) {
                                        activeIndex = (activeIndex + 1) % wallets.size
                                    } else if (totalDrag > 60) {
                                        activeIndex = (activeIndex - 1 + wallets.size) % wallets.size
                                    }
                                    totalDrag = 0f
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    totalDrag += dragAmount
                                }
                            )
                        }
                    }
            ) {
                LeatherCardPocket(
                    wallet = activeWallet,
                    currencySymbol = currencySymbol,
                    isFrozen = isCardFrozen,
                    onCardClick = {
                        isFlippedBack = false
                        isPoppedOutModalOpen = true
                    }
                )
            }
        }

        // Indicator Dots for Multiple Cards
        if (wallets.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                wallets.forEachIndexed { idx, _ ->
                    val isCurrent = idx == activeIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = if (isCurrent) 18.dp else 6.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) AppTheme.colors.primaryAccent else AppTheme.colors.surfaceBorder)
                            .clickable { activeIndex = idx }
                    )
                }
            }
        }

        // Card Action Buttons Row: [Card Details] [Freeze Card] [More]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                onClick = {
                    if (activeWallet != null) {
                        showFullNumbers = !showFullNumbers
                        clipboardManager.setText(AnnotatedString(activeWallet.cardNumber))
                        Toast.makeText(context, "Card number copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                color = AppTheme.colors.surface,
                border = BorderStroke(1.dp, AppTheme.colors.surfaceBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("action_card_details_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CreditCard,
                        contentDescription = "Card Details",
                        tint = AppTheme.colors.textCrispWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Card Details",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textCrispWhite
                    )
                }
            }

            Surface(
                onClick = {
                    isCardFrozen = !isCardFrozen
                    val msg = if (isCardFrozen) "Card is now frozen" else "Card is unfreezed and active"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(14.dp),
                color = if (isCardFrozen) Color(0xFF0284C7).copy(alpha = 0.15f) else AppTheme.colors.surface,
                border = BorderStroke(1.dp, if (isCardFrozen) Color(0xFF38BDF8) else AppTheme.colors.surfaceBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("action_freeze_card_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCardFrozen) Icons.Filled.Lock else Icons.Outlined.Lock,
                        contentDescription = "Freeze Card",
                        tint = if (isCardFrozen) Color(0xFF0284C7) else AppTheme.colors.textCrispWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCardFrozen) "Unfreeze" else "Freeze Card",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCardFrozen) Color(0xFF0284C7) else AppTheme.colors.textCrispWhite
                    )
                }
            }

            Surface(
                onClick = {
                    if (activeWallet != null) {
                        onTopUp(activeWallet)
                    } else {
                        onAddCard()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                color = AppTheme.colors.surface,
                border = BorderStroke(1.dp, AppTheme.colors.surfaceBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("action_more_card_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreHoriz,
                        contentDescription = "More",
                        tint = AppTheme.colors.textCrispWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "More",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.textCrispWhite
                    )
                }
            }
        }
    }

    // Interactive Pop-Out & 3D Flip Overlay Dialog
    if (isPoppedOutModalOpen && activeWallet != null) {
        val rotationYAnim by animateFloatAsState(
            targetValue = if (isFlippedBack) 180f else 0f,
            animationSpec = tween(durationMillis = 500),
            label = "card_flip_3d_rotation"
        )

        Dialog(onDismissRequest = { isPoppedOutModalOpen = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isPoppedOutModalOpen = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .clickable(enabled = false) {}
                        .padding(16.dp)
                ) {
                    // Card Container with 3D Flip Rotation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(218.dp)
                            .graphicsLayer {
                                rotationY = rotationYAnim
                                cameraDistance = 12f * density
                            }
                            .clickable { isFlippedBack = !isFlippedBack }
                    ) {
                        if (rotationYAnim <= 90f) {
                            SinglePaymentCardView(
                                wallet = activeWallet,
                                currencySymbol = currencySymbol,
                                isFrozen = isCardFrozen,
                                showFullNumbers = showFullNumbers,
                                isBack = false
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f }
                            ) {
                                SinglePaymentCardView(
                                    wallet = activeWallet,
                                    currencySymbol = currencySymbol,
                                    isFrozen = isCardFrozen,
                                    showFullNumbers = showFullNumbers,
                                    isBack = true
                                )
                            }
                        }
                    }

                    // Flip & Dismiss Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { isFlippedBack = !isFlippedBack },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                        ) {
                            Icon(Icons.Default.Flip, contentDescription = "Flip Card", tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text(if (isFlippedBack) "Show Front" else "Flip to Back", color = Color.White)
                        }

                        Button(
                            onClick = { isPoppedOutModalOpen = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222736))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text("In Sleeve", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable Payment Card Renderer (Front / Back):
 * Uses `getCardGradient(wallet.designId, wallet.primaryColor, wallet.secondaryColor)` so all saved designs propagate instantly!
 */
@Composable
fun SinglePaymentCardView(
    wallet: WalletEntity,
    currencySymbol: String,
    isFrozen: Boolean = false,
    showFullNumbers: Boolean = false,
    isBack: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (!isBack) {
        CustomizablePaymentCardView(
            wallet = wallet,
            currencySymbol = currencySymbol,
            isFrozen = isFrozen,
            modifier = modifier
        )
        return
    }

    val cardGradient = getCardGradient(wallet.designId, wallet.primaryColor, wallet.secondaryColor)
    val cardAccent = parseColorSafe(wallet.accentColor, Color(0xFF38BDF8))
    val isCredit = wallet.cardType.equals("credit", ignoreCase = true)
    val isUpi = wallet.cardType.equals("upi", ignoreCase = true)
    val isCash = wallet.cardType.equals("cash", ignoreCase = true)
    val isDebit = wallet.cardType.equals("debit", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
        border = BorderStroke(1.dp, if (isFrozen) Color(0xFF38BDF8) else cardAccent.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.50f)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isFrozen) Brush.linearGradient(listOf(Color(0xFF0F1E2E), Color(0xFF09131C))) else cardGradient)
                .padding(if (isBack) 0.dp else 22.dp)
        ) {
            if (isBack) {
                // ── REVERSE SIDE (BACK OF CARD) ──
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(Modifier.height(18.dp))
                    // Dark Magnetic Stripe
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .background(Color(0xFF090A0F))
                    )

                    // Signature Strip with Masked Identifier
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp)
                    ) {
                        Text(
                            text = "AUTHORIZED SIGNATURE",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = wallet.cardHolderName.ifBlank { "VIVEK BHARDWAJ" }.uppercase(),
                                color = Color(0xFF334155),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Cursive,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "•••• ${wallet.cardNumber.filter(Char::isDigit).takeLast(4).ifEmpty { "4242" }}",
                                color = Color(0xFF0F172A),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Safe Account Information Notice
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 22.dp, end = 22.dp, bottom = 18.dp)
                    ) {
                        Text(
                            text = "${wallet.bankName.ifBlank { "EXPENSETRACK" }} • ${wallet.cardType.uppercase()}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Not valid without authorized signature. Customer Support: 1-800-555-0199",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 8.5.sp
                        )
                    }
                }
            } else {
                // ── FRONT SIDE ──
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Row: Brand & Payment Type Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val brand = wallet.cardBrand.ifBlank { "Visa" }
                            if (brand.contains("master", ignoreCase = true)) {
                                MastercardFintechLogo()
                            } else if (brand.contains("rupay", ignoreCase = true)) {
                                RuPayFintechLogo()
                            } else {
                                Text(
                                    text = brand.uppercase(),
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    fontStyle = FontStyle.Italic,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Payment Type Distinct Badge
                            val badgeLabel = when {
                                isUpi -> "UPI"
                                isCash -> "CASH"
                                isCredit -> "CREDIT"
                                isDebit -> "DEBIT"
                                else -> "SAVINGS"
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.40f),
                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = badgeLabel,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        ContactlessWavesIcon(tint = Color.White.copy(alpha = 0.85f))
                    }

                    // Middle Row: EMV Chip & Number
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isCash) {
                            EmvGoldChip()
                        } else {
                            Text("CASH WALLET", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        val cleanNumber = wallet.cardNumber.filter { it.isDigit() }
                        val lastFour = if (cleanNumber.length >= 4) cleanNumber.takeLast(4) else "4242"
                        val displayedNumber = if (showFullNumbers && cleanNumber.length >= 12) {
                            "${cleanNumber.take(4)}   ${cleanNumber.drop(4).take(4)}   ${cleanNumber.drop(8).take(4)}   $lastFour"
                        } else {
                            "••••   ••••   ••••   $lastFour"
                        }

                        Text(
                            text = if (isUpi && wallet.cardNumber.contains("@")) wallet.cardNumber else displayedNumber,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Bottom Row: Cardholder Name & Expiry Date / Balance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "ACCOUNT",
                                color = Color(0xFF94A3B8),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = wallet.bankName.ifBlank { wallet.cardHolderName.ifBlank { "ACCOUNT" } }.uppercase(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isCredit) "AVAILABLE" else "BALANCE",
                                color = Color(0xFF94A3B8),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            )
                            val creditSum = if (isCredit) calculateCreditCardSummary(wallet) else null
                            val balVal = if (isCredit) (creditSum?.availableCredit ?: 0.0) else wallet.balance
                            Text(
                                text = "$currencySymbol${String.format("%,.2f", balVal)}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmvGoldChip(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 38.dp, height = 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFFDF7D), Color(0xFFE5B73B), Color(0xFFC4971A))
                )
            )
            .border(0.8.dp, Color(0xFF9E770E), RoundedCornerShape(6.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stroke = Stroke(width = 0.9.dp.toPx())
            val lineCol = Color(0xFF8A6506)

            drawLine(lineCol, Offset(w * 0.35f, 0f), Offset(w * 0.35f, h), stroke.width)
            drawLine(lineCol, Offset(w * 0.65f, 0f), Offset(w * 0.65f, h), stroke.width)
            drawLine(lineCol, Offset(0f, h * 0.50f), Offset(w, h * 0.50f), stroke.width)

            drawCircle(lineCol, radius = w * 0.12f, center = Offset(w / 2f, h / 2f), style = stroke)
        }
    }
}

@Composable
fun ContactlessWavesIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)

        drawArc(
            color = tint,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.10f, h * 0.28f),
            size = androidx.compose.ui.geometry.Size(w * 0.40f, h * 0.44f),
            style = stroke
        )

        drawArc(
            color = tint,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.28f, h * 0.14f),
            size = androidx.compose.ui.geometry.Size(w * 0.60f, h * 0.72f),
            style = stroke
        )
    }
}

@Composable
fun MastercardFintechLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(width = 38.dp, height = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFEB001B))
        )
        Box(
            modifier = Modifier
                .padding(start = 14.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFF79E1B).copy(alpha = 0.95f))
        )
    }
}

@Composable
fun RuPayFintechLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "RuPay",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic,
            fontSize = 18.sp,
            letterSpacing = (-0.5).sp
        )
        Box(
            modifier = Modifier
                .padding(start = 2.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF00B0FF))
        )
    }
}
