package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WalletEntity
import com.example.ui.theme.CardAuroraEnd
import com.example.ui.theme.CardAuroraStart
import com.example.ui.theme.CardBerryEnd
import com.example.ui.theme.CardBerryStart
import com.example.ui.theme.CardGlassEnd
import com.example.ui.theme.CardGlassStart
import com.example.ui.theme.CardLavenderEnd
import com.example.ui.theme.CardLavenderStart
import com.example.ui.theme.CardMidnightEnd
import com.example.ui.theme.CardMidnightStart
import com.example.ui.theme.CardMintEnd
import com.example.ui.theme.CardMintStart
import com.example.ui.theme.CardOceanEnd
import com.example.ui.theme.CardOceanStart
import com.example.ui.theme.CardSunsetEnd
import com.example.ui.theme.CardSunsetStart
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

fun getCardGradient(designId: String, primaryColor: String, secondaryColor: String): Brush {
    return when (designId.lowercase()) {
        "lavender", "lilac", "onebank" -> Brush.linearGradient(
            listOf(Color(0xFFC5B4F5), Color(0xFFA592EB), Color(0xFF8E7CE3))
        )
        "midnight", "midnight-aurora", "black", "matte-black" -> Brush.linearGradient(
            listOf(Color(0xFF26262B), Color(0xFF131316))
        )
        "translucent", "glass", "silver", "silver-glass" -> Brush.linearGradient(
            listOf(Color(0xFFE6E8F2), Color(0xFFD6DAE8))
        )
        "sunset" -> Brush.linearGradient(listOf(CardSunsetStart, CardSunsetEnd))
        "aurora" -> Brush.linearGradient(listOf(CardAuroraStart, CardAuroraEnd))
        "berry" -> Brush.linearGradient(listOf(CardBerryStart, CardBerryEnd))
        "ocean-gradient", "ocean" -> Brush.linearGradient(listOf(CardOceanStart, CardOceanEnd))
        "mint", "upi-mint" -> Brush.linearGradient(listOf(CardMintStart, CardMintEnd))
        else -> {
            try {
                val c1 = Color(android.graphics.Color.parseColor(primaryColor))
                val c2 = Color(android.graphics.Color.parseColor(secondaryColor))
                Brush.linearGradient(listOf(c1, c2))
            } catch (e: Exception) {
                Brush.linearGradient(listOf(Color(0xFFC5B4F5), Color(0xFF8E7CE3)))
            }
        }
    }
}

fun formatCurrency(amount: Double, symbol: String = "$"): String {
    val formatter = DecimalFormat("#,##0.00")
    return "$symbol${formatter.format(amount)}"
}

fun formatCardNumberSpaced(rawNumber: String): String {
    val clean = rawNumber.filter { it.isDigit() }
    if (clean.length < 12) {
        return rawNumber.ifBlank { "••••   ••••   ••••   ••••" }
    }
    val padded = clean.padEnd(16, '0')
    return "${padded.substring(0, 4)}   ${padded.substring(4, 8)}   ${padded.substring(8, 12)}   ${padded.substring(12, 16)}"
}

@Composable
fun MastercardLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(width = 36.dp, height = 22.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFFEB001B))
        )
        Box(
            modifier = Modifier
                .padding(start = 13.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFFF79E1B).copy(alpha = 0.95f))
        )
    }
}

/**
 * High-fidelity replica of the "Your balance" and fanned-out Cards Deck UI
 * matching the design in IMG_20260920_180144.jpg.
 */
@Composable
fun CardCarousel(
    wallets: List<WalletEntity>,
    currencySymbol: String,
    onAddCard: () -> Unit,
    onTopUp: (WalletEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedIndex by remember { mutableIntStateOf(0) }
    var showNumbers by remember { mutableStateOf(true) }

    // Ensure safe index bounds
    val activeIndex = if (wallets.isEmpty()) 0 else selectedIndex.coerceIn(0, wallets.lastIndex)

    val activeWallet = wallets.getOrNull(activeIndex) ?: WalletEntity(
        bankName = "",
        cardType = "",
        cardNumber = ""
    )

    // Left and right stacked cards for fanned-out 3D presentation
    val leftWallet = if (wallets.size > 1) {
        wallets[(activeIndex + wallets.size - 1) % wallets.size]
    } else activeWallet

    val rightWallet = if (wallets.size > 2) {
        wallets[(activeIndex + 1) % wallets.size]
    } else activeWallet

    val balanceAmount = if (wallets.isEmpty()) 0.0 else activeWallet.balance
    val formattedBalance = String.format(Locale.US, "%.2f", balanceAmount)
    val dotIndex = formattedBalance.indexOf('.')
    val integerPart = if (dotIndex != -1) formattedBalance.substring(0, dotIndex) else formattedBalance
    val decimalPart = if (dotIndex != -1) formattedBalance.substring(dotIndex) else ".00"
    val intWithSeparators = try {
        NumberFormat.getNumberInstance(Locale.US).format(integerPart.toLong())
    } catch (e: Exception) {
        integerPart
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── 1. Top Section: "Your balance" ──
        Text(
            text = "Your balance",
            color = Color(0xFFB3A8CE),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.2.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // ── 2. Arrow Controls + Large Balance: ( < )  $3567.37  ( > ) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Navigation Button <
            Surface(
                onClick = {
                    if (wallets.isNotEmpty()) {
                        selectedIndex = (activeIndex + wallets.size - 1) % wallets.size
                    }
                },
                shape = CircleShape,
                color = Color(0xFF3B3359),
                border = BorderStroke(1.dp, Color(0xFF554B7C)),
                modifier = Modifier
                    .size(38.dp)
                    .testTag("card_prev_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Card",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Large Bold Balance Display: e.g. $3567.37
            AnimatedContent(
                targetState = balanceAmount,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                label = "balance_anim"
            ) { _ ->
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.testTag("active_card_balance_text")
                ) {
                    Text(
                        text = currencySymbol,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF5CE9F),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = intWithSeparators,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = decimalPart,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF5CE9F),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Right Navigation Button >
            Surface(
                onClick = {
                    if (wallets.isNotEmpty()) {
                        selectedIndex = (activeIndex + 1) % wallets.size
                    }
                },
                shape = CircleShape,
                color = Color(0xFF3B3359),
                border = BorderStroke(1.dp, Color(0xFF554B7C)),
                modifier = Modifier
                    .size(38.dp)
                    .testTag("card_next_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Card",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ── 3. Fanned-Out 3D Card Stack (Back-Right, Back-Left, Front-Center) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // ── Layer 1: Back-Right Card (Icy / Translucent Glass) ──
            if (wallets.size > 2) {
                Box(
                    modifier = Modifier
                        .offset(x = 26.dp, y = (-22).dp)
                        .width(296.dp)
                        .height(178.dp)
                        .scale(0.92f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            getCardGradient(rightWallet.designId, rightWallet.primaryColor, rightWallet.secondaryColor)
                        )
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)), RoundedCornerShape(24.dp))
                        .clickable {
                            if (wallets.isNotEmpty()) {
                                selectedIndex = (activeIndex + 1) % wallets.size
                            }
                        }
                        .padding(18.dp)
                ) {
                    // Background card brand & bank
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rightWallet.cardBrand.ifBlank { "CARD" }.uppercase(),
                            color = Color(0xFF71717A).copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp
                        )
                        Text(
                            text = rightWallet.bankName,
                            color = Color(0xFF52525B).copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ── Layer 2: Back-Left Card (Matte Charcoal / Mastercard) ──
            if (wallets.size > 1) {
                Box(
                    modifier = Modifier
                        .offset(x = (-24).dp, y = (-12).dp)
                        .width(304.dp)
                        .height(184.dp)
                        .scale(0.96f)
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            getCardGradient(leftWallet.designId, leftWallet.primaryColor, leftWallet.secondaryColor)
                        )
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(24.dp))
                        .clickable {
                            if (wallets.isNotEmpty()) {
                                selectedIndex = (activeIndex + wallets.size - 1) % wallets.size
                            }
                        }
                        .padding(18.dp)
                ) {
                    // Top header of Left Card: Mastercard logo + onebank
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (leftWallet.cardBrand.contains("master", ignoreCase = true) || leftWallet.designId == "midnight") {
                            MastercardLogo()
                        } else {
                            Text(
                                text = leftWallet.cardBrand.ifBlank { "CARD" }.uppercase(),
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic,
                                fontSize = 16.sp
                            )
                        }

                        Text(
                            text = leftWallet.bankName,
                            color = Color(0xFFA0A0AB),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ── Layer 3: Dreamy Atmospheric Ambient Drop Glow beneath the Front Card ──
            Box(
                modifier = Modifier
                    .offset(y = 16.dp)
                    .width(280.dp)
                    .height(160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF8E7CE3).copy(alpha = 0.40f))
            )

            // ── Layer 4: Front-Center Card ──
            if (wallets.isEmpty()) {
                Card(
                    modifier = Modifier
                        .offset(y = 2.dp)
                        .width(318.dp)
                        .height(194.dp)
                        .testTag("empty_card_placeholder")
                        .clickable { onAddCard() },
                    shape = RoundedCornerShape(26.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF8B7AE0), Color(0xFF6B58C9))
                                )
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Card",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Payment Cards Linked",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap here to connect a card or account",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .offset(y = 2.dp)
                        .width(318.dp)
                        .height(194.dp)
                        .testTag("active_front_card"),
                    shape = RoundedCornerShape(26.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                getCardGradient(activeWallet.designId, activeWallet.primaryColor, activeWallet.secondaryColor)
                            )
                            .padding(18.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Card Top Row: VISA Logo (left) and onebank (right)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (activeWallet.cardBrand.contains("master", ignoreCase = true)) {
                                    MastercardLogo()
                                } else {
                                    Text(
                                        text = activeWallet.cardBrand.ifBlank { "CARD" }.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 20.sp,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Text(
                                    text = activeWallet.bankName,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Bottom Frosted Glass Overlay Container
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.28f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("card_frosted_panel")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    // 4 groups of 4 digits: e.g. 4153   2415   3467   8764
                                    val displayedNumber = if (showNumbers) {
                                        formatCardNumberSpaced(activeWallet.cardNumber)
                                    } else {
                                        val lastFour = if (activeWallet.cardNumber.length >= 4) activeWallet.cardNumber.takeLast(4) else "••••"
                                        "••••   ••••   ••••   $lastFour"
                                    }

                                    Text(
                                        text = displayedNumber,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.5.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Expiry Date: 06/25 aligned under first digits
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = activeWallet.expiryDate.ifBlank { "MM/YY" },
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Text(
                                            text = activeWallet.cardHolderName.ifBlank { "CARDHOLDER" }.uppercase(),
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── 4. Card Indicator Dots ──
        val cardCount = wallets.size
        if (cardCount > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                repeat(cardCount) { index ->
                    val isSelected = index == activeIndex
                    val width by animateFloatAsState(
                        targetValue = if (isSelected) 18f else 6f,
                        animationSpec = tween(200),
                        label = "dot_w"
                    )
                    Box(
                        modifier = Modifier
                            .size(width = width.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFF8E7CE3) else Color(0xFFD6CEE8)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── 5. Quick Actions Row: [+ Top Up]  [+ New Card]  [Card Details / Copy] ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top Up Button
            Surface(
                onClick = {
                    if (wallets.isNotEmpty()) {
                        onTopUp(activeWallet)
                    } else {
                        onAddCard()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF262553),
                border = BorderStroke(1.dp, Color(0xFF554B7C)),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("active_card_top_up_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFFF5CE9F),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Top Up",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // New Card Button
            Surface(
                onClick = onAddCard,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5CE9F),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("add_card_carousel_btn")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = Color(0xFF2B2245),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Card",
                        color = Color(0xFF2B2245),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Visibility / Mask Toggle Button
            Surface(
                onClick = {
                    showNumbers = !showNumbers
                    val msg = if (showNumbers) "Card numbers revealed" else "Card numbers masked"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF3B3359),
                border = BorderStroke(1.dp, Color(0xFF554B7C)),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("toggle_card_number_visibility")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (showNumbers) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Numbers",
                        tint = Color(0xFFF5CE9F),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

/**
 * Individual wallet card item for lists and wallet management screens.
 */
@Composable
fun WalletCardItem(
    wallet: WalletEntity,
    currencySymbol: String,
    onTopUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brush = getCardGradient(wallet.designId, wallet.primaryColor, wallet.secondaryColor)
    val isMaster = wallet.cardBrand.contains("master", ignoreCase = true)
    val isUpi = wallet.cardType.equals("upi", ignoreCase = true)
    val isCash = wallet.cardType.equals("cash", ignoreCase = true)
    val lastFour = if (wallet.cardNumber.length >= 4) wallet.cardNumber.takeLast(4) else wallet.cardNumber

    Card(
        modifier = modifier
            .width(280.dp)
            .height(172.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header: Brand & Bank Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMaster) {
                        MastercardLogo()
                    } else if (isCash) {
                        Text(
                            text = "CASH",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    } else if (isUpi) {
                        Text(
                            text = "UPI",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        Text(
                            text = wallet.cardBrand.ifBlank { "VISA" }.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = wallet.bankName,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Frosted glass overlay containing number and expiry
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isCash) "Physical Cash" else if (isUpi) wallet.cardNumber else "••••  ••••  ••••  $lastFour",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        if (wallet.expiryDate.isNotBlank()) {
                            Text(
                                text = wallet.expiryDate,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Bottom: Balance & Top Up
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "BALANCE",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = formatCurrency(wallet.balance, currencySymbol),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    Surface(
                        onClick = onTopUp,
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.testTag("wallet_top_up_${wallet.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Top Up",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Top Up",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
