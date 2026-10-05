package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.model.DashboardSummary
import com.example.model.WalletEntity
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen

/**
 * Clean Fintech Balance Area matching Reference Design:
 * - Label "Total Balance" with small eye toggle button
 * - Large dominant typography: e.g. ₹9,178.00
 * - Side-by-side Income & Spent indicators
 */
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
        // Label & Eye Visibility Toggle
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

        // Large Dominant Balance Number
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

        // Side-by-side Income and Spent Pills
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
 * Realistic Dark Premium Payment Card:
 * - Black / dark charcoal card
 * - Textured / refined subtle gradient background
 * - Rounded corners 24dp
 * - Visa/Mastercard/RuPay branding
 * - EMV Chip & Contactless indicator
 * - Formatted card number & holder details
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

    val safeIndex = if (wallets.isNotEmpty()) activeIndex.coerceIn(0, wallets.size - 1) else 0
    val activeWallet = wallets.getOrNull(safeIndex)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Digital Card Container
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
            // Premium Dark Payment Card
            val cardPrimary = parseColorSafe(activeWallet.primaryColor, Color(0xFF222733))
            val cardSecondary = parseColorSafe(activeWallet.secondaryColor, Color(0xFF0D0F16))
            val cardAccent = parseColorSafe(activeWallet.accentColor, Color(0xFF38BDF8))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardSecondary),
                border = BorderStroke(1.dp, if (isCardFrozen) Color(0xFF38BDF8).copy(alpha = 0.5f) else cardAccent.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(214.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = cardPrimary.copy(alpha = 0.30f),
                        spotColor = Color.Black.copy(alpha = 0.50f)
                    )
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
                    .testTag("fintech_active_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = if (isCardFrozen) {
                                    listOf(Color(0xFF0F1E2E), Color(0xFF09131C), Color(0xFF03070B))
                                } else {
                                    listOf(cardPrimary, cardSecondary)
                                },
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .padding(22.dp)
                ) {
                    // Artwork illustration canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCustomCardArtwork(
                            artwork = activeWallet.artwork,
                            accentColor = cardAccent,
                            primaryColor = cardPrimary,
                            secondaryColor = cardSecondary
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Row: Brand & Contactless / GPay
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Card Brand Logo & Theme Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val brand = activeWallet.cardBrand.ifBlank { "Visa" }
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

                                val themeName = activeWallet.cardTheme.ifBlank { activeWallet.designId }
                                if (themeName.isNotBlank() && !themeName.equals("midnight", ignoreCase = true)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.Black.copy(alpha = 0.35f),
                                        border = BorderStroke(0.8.dp, cardAccent.copy(alpha = 0.6f))
                                    ) {
                                        Text(
                                            text = themeName.uppercase().take(8),
                                            color = cardAccent,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Contactless / Google Pay indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (isCardFrozen) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AcUnit,
                                                contentDescription = "Frozen",
                                                tint = Color(0xFF7DD3FC),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "FROZEN",
                                                color = Color(0xFFE0F2FE),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                ContactlessWavesIcon(tint = Color.White.copy(alpha = 0.85f))
                            }
                        }

                        // Middle: EMV Gold Chip & Card Number
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // EMV Smart Chip
                            EmvGoldChip()

                            // Formatted Card Number
                            val cleanNumber = activeWallet.cardNumber.filter { it.isDigit() }
                            val lastFour = if (cleanNumber.length >= 4) cleanNumber.takeLast(4) else "4242"
                            val displayedNumber = if (showFullNumbers && cleanNumber.length >= 12) {
                                "${cleanNumber.take(4)}   ${cleanNumber.drop(4).take(4)}   ${cleanNumber.drop(8).take(4)}   $lastFour"
                            } else {
                                "••••   ••••   ••••   $lastFour"
                            }

                            Text(
                                text = displayedNumber,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.2.sp
                            )
                        }

                        // Bottom Row: Cardholder Name & Expiry Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "CARDHOLDER",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activeWallet.cardHolderName.ifBlank { "VIVEK BHARDWAJ" }.uppercase(),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.4.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "EXPIRES",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activeWallet.expiryDate.ifBlank { "12/28" },
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.4.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card Indicator Dots (if multiple cards)
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

        // ── Card Actions Row ──
        // [Card Details]  [Freeze Card]  [More]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Card Details Action
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

            // 2. Freeze Card Action
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

            // 3. More Action
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
}

/**
 * EMV Gold Smart Chip with detailed circuitry lines
 */
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

            // Center circuit lines
            drawLine(lineCol, Offset(w * 0.35f, 0f), Offset(w * 0.35f, h), stroke.width)
            drawLine(lineCol, Offset(w * 0.65f, 0f), Offset(w * 0.65f, h), stroke.width)
            drawLine(lineCol, Offset(0f, h * 0.50f), Offset(w, h * 0.50f), stroke.width)

            // Center contact oval
            drawCircle(lineCol, radius = w * 0.12f, center = Offset(w / 2f, h / 2f), style = stroke)
        }
    }
}

/**
 * Contactless Radio Waves Graphic (4 radiating curves)
 */
@Composable
fun ContactlessWavesIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)

        // Arc 1 (inner)
        drawArc(
            color = tint,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.10f, h * 0.28f),
            size = androidx.compose.ui.geometry.Size(w * 0.40f, h * 0.44f),
            style = stroke
        )

        // Arc 2 (outer)
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

/**
 * Mastercard Interlocking Circles
 */
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

/**
 * RuPay Badge Logo
 */
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
