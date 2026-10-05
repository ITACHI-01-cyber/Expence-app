package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WalletEntity
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen

@Composable
fun FintechPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    testTag: String = ""
) {
    val colors = AppTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primaryAccent,
            contentColor = colors.onPrimaryAccent,
            disabledContainerColor = colors.primaryAccent.copy(alpha = 0.5f),
            disabledContentColor = colors.onPrimaryAccent.copy(alpha = 0.6f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = colors.onPrimaryAccent,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Card Design Preset definition inspired by the colorful collectible illustrated cards reference.
 */
data class CardDesignPreset(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val accentColorHex: String,
    val artwork: String,
    val cardStyle: String
)

val CARD_DESIGN_PRESETS = listOf(
    CardDesignPreset(
        id = "broco",
        name = "BROCO",
        subtitle = "Mint Pop & Energy",
        category = "Collectible",
        primaryColorHex = "#14B8A6",
        secondaryColorHex = "#042F2E",
        accentColorHex = "#A3E635",
        artwork = "mascot_broco",
        cardStyle = "illustrated"
    ),
    CardDesignPreset(
        id = "tonot",
        name = "TONOT",
        subtitle = "Cosmic Astronaut Orbit",
        category = "Space",
        primaryColorHex = "#1E3A8A",
        secondaryColorHex = "#0B132B",
        accentColorHex = "#FBBF24",
        artwork = "space_tonot",
        cardStyle = "illustrated"
    ),
    CardDesignPreset(
        id = "bredo",
        name = "BREDO",
        subtitle = "Royal Cobalt & Toast",
        category = "Playful",
        primaryColorHex = "#2563EB",
        secondaryColorHex = "#172554",
        accentColorHex = "#FDE047",
        artwork = "toast_bredo",
        cardStyle = "illustrated"
    ),
    CardDesignPreset(
        id = "kocheng",
        name = "KOCHENG",
        subtitle = "Coral Spark & Cat Box",
        category = "Playful",
        primaryColorHex = "#EA580C",
        secondaryColorHex = "#431407",
        accentColorHex = "#22D3EE",
        artwork = "cat_kocheng",
        cardStyle = "illustrated"
    ),
    CardDesignPreset(
        id = "drino",
        name = "DRINO",
        subtitle = "Amber Dragon Work",
        category = "Gaming",
        primaryColorHex = "#D97706",
        secondaryColorHex = "#451A03",
        accentColorHex = "#2DD4BF",
        artwork = "dragon_drino",
        cardStyle = "illustrated"
    ),
    CardDesignPreset(
        id = "wuhu",
        name = "WUHU",
        subtitle = "Deep Imperial Violet",
        category = "Nature",
        primaryColorHex = "#86198F",
        secondaryColorHex = "#3B0764",
        accentColorHex = "#FDBA74",
        artwork = "bird_wuhu",
        cardStyle = "illustrated"
    ),
    CardDesignPreset(
        id = "yo",
        name = "YO",
        subtitle = "Graphite Soundwave",
        category = "Modern",
        primaryColorHex = "#374151",
        secondaryColorHex = "#111827",
        accentColorHex = "#EF4444",
        artwork = "music_yo",
        cardStyle = "illustrated"
    ),
    CardDesignPreset(
        id = "midnight",
        name = "MIDNIGHT",
        subtitle = "Obsidian Dark Luxury",
        category = "Minimal",
        primaryColorHex = "#18181B",
        secondaryColorHex = "#09090B",
        accentColorHex = "#38BDF8",
        artwork = "minimal",
        cardStyle = "dark"
    ),
    CardDesignPreset(
        id = "aurora",
        name = "AURORA",
        subtitle = "Northern Lights Glow",
        category = "Gradient",
        primaryColorHex = "#7C3AED",
        secondaryColorHex = "#1E1B4B",
        accentColorHex = "#34D399",
        artwork = "aurora_lights",
        cardStyle = "gradient"
    ),
    CardDesignPreset(
        id = "ocean",
        name = "OCEAN",
        subtitle = "Deep Tidal Current",
        category = "Nature",
        primaryColorHex = "#0284C7",
        secondaryColorHex = "#082F49",
        accentColorHex = "#67E8F9",
        artwork = "waves",
        cardStyle = "illustrated"
    ),
    CardDesignPreset(
        id = "sunset",
        name = "SUNSET",
        subtitle = "Golden Hour Dusk",
        category = "Gradient",
        primaryColorHex = "#E11D48",
        secondaryColorHex = "#4C0519",
        accentColorHex = "#FBBF24",
        artwork = "geometric",
        cardStyle = "gradient"
    ),
    CardDesignPreset(
        id = "forest",
        name = "FOREST",
        subtitle = "Emerald Botanical",
        category = "Nature",
        primaryColorHex = "#047857",
        secondaryColorHex = "#064E3B",
        accentColorHex = "#A3E635",
        artwork = "geometric",
        cardStyle = "illustrated"
    ),
    CardDesignPreset(
        id = "cyber",
        name = "CYBER",
        subtitle = "Neon Wireframe Grid",
        category = "Technology",
        primaryColorHex = "#0A0A0A",
        secondaryColorHex = "#171717",
        accentColorHex = "#06B6D4",
        artwork = "cyber_grid",
        cardStyle = "neon"
    )
)

val CURATED_COLORS = listOf(
    "#18181B" to "Black",
    "#374151" to "Graphite",
    "#1E3A8A" to "Navy",
    "#2563EB" to "Cobalt",
    "#7C3AED" to "Purple",
    "#86198F" to "Violet",
    "#14B8A6" to "Teal",
    "#047857" to "Green",
    "#EA580C" to "Coral",
    "#DC2626" to "Red",
    "#E11D48" to "Rose",
    "#D97706" to "Gold"
)

val CARD_STYLES = listOf(
    "illustrated" to "Illustrated",
    "gradient" to "Gradient",
    "dark" to "Dark Luxury",
    "neon" to "Neon Cyber",
    "glass" to "Glassmorphism",
    "minimal" to "Minimal"
)

val ARTWORK_OPTIONS = listOf(
    "mascot_broco" to "Broco Pop",
    "space_tonot" to "Space Astronaut",
    "toast_bredo" to "Bredo Toast",
    "cat_kocheng" to "Cat in Box",
    "dragon_drino" to "Tech Dragon",
    "bird_wuhu" to "Night Bird",
    "music_yo" to "Soundwaves",
    "waves" to "Ocean Waves",
    "cyber_grid" to "Cyber Grid",
    "aurora_lights" to "Aurora Lights",
    "geometric" to "Bauhaus Shapes",
    "minimal" to "Minimal Monogram"
)

fun parseColorSafe(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

/**
 * Large Interactive Customizable Payment Card View
 * - Displays the real wallet financial info (cardBrand, Google Pay, UPI id / masked number, expiry, holder name, balance)
 * - Dynamically renders the selected artwork, colors, gradient, depth, reflections, and freeze state!
 */
@Composable
fun CustomizablePaymentCardView(
    wallet: WalletEntity,
    currencySymbol: String = "₹",
    isFrozen: Boolean = false,
    overrideTheme: String? = null,
    overridePrimaryColor: String? = null,
    overrideSecondaryColor: String? = null,
    overrideAccentColor: String? = null,
    overrideArtwork: String? = null,
    overrideCardStyle: String? = null,
    modifier: Modifier = Modifier
) {
    val theme = overrideTheme ?: wallet.cardTheme.ifBlank { wallet.designId }
    val primaryHex = overridePrimaryColor ?: wallet.primaryColor
    val secondaryHex = overrideSecondaryColor ?: wallet.secondaryColor
    val accentHex = overrideAccentColor ?: wallet.accentColor
    val artwork = overrideArtwork ?: wallet.artwork
    val style = overrideCardStyle ?: wallet.cardStyle

    val primaryColor = parseColorSafe(primaryHex, Color(0xFF14B8A6))
    val secondaryColor = parseColorSafe(secondaryHex, Color(0xFF042F2E))
    val accentColor = parseColorSafe(accentHex, Color(0xFFA3E635))

    val animatedPrimary by animateColorAsState(targetValue = primaryColor, animationSpec = tween(300), label = "c_pri")
    val animatedSecondary by animateColorAsState(targetValue = secondaryColor, animationSpec = tween(300), label = "c_sec")
    val animatedAccent by animateColorAsState(targetValue = accentColor, animationSpec = tween(300), label = "c_acc")

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = animatedSecondary),
        border = BorderStroke(
            1.5.dp,
            if (isFrozen) Color(0xFF38BDF8).copy(alpha = 0.8f) else animatedAccent.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(218.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = animatedPrimary.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            )
            .testTag("customizable_payment_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            animatedPrimary.copy(alpha = 0.95f),
                            animatedSecondary.copy(alpha = 0.98f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(700f, 700f)
                    )
                )
        ) {
            // Background Artwork Illustration Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCustomCardArtwork(
                    artwork = artwork,
                    accentColor = animatedAccent,
                    primaryColor = animatedPrimary,
                    secondaryColor = animatedSecondary
                )
            }

            // Subtle Glass Light Sheen / Specular overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.35f)
                            )
                        )
                    )
            )

            // Front Card Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Card Brand (VISA / Mastercard / RuPay) and Bank / Google Pay
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
                        Text(
                            text = brand.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            fontSize = 19.sp,
                            letterSpacing = 1.sp
                        )

                        // Collectible theme mini badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.35f),
                            border = BorderStroke(0.8.dp, animatedAccent.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = theme.uppercase().take(8),
                                color = animatedAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Contactless Wave
                        Icon(
                            imageVector = Icons.Outlined.Wifi,
                            contentDescription = "Contactless",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )

                        // Google Pay indicator
                        Text(
                            text = "Google Pay",
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.3.sp
                        )
                    }
                }

                // Middle: EMV Metallic Chip & Masked Identifier
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Realistic Gold/Brass EMV Chip
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = Color(0xFFD4AF37),
                        border = BorderStroke(1.dp, Color(0xFFB8860B)),
                        modifier = Modifier.size(width = 40.dp, height = 30.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawLine(
                                    color = Color(0xFF8B6508),
                                    start = Offset(size.width * 0.45f, 0f),
                                    end = Offset(size.width * 0.45f, size.height),
                                    strokeWidth = 1.5f
                                )
                                drawLine(
                                    color = Color(0xFF8B6508),
                                    start = Offset(0f, size.height * 0.5f),
                                    end = Offset(size.width, size.height * 0.5f),
                                    strokeWidth = 1.5f
                                )
                            }
                        }
                    }

                    // Card Balance Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Balance: ",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "$currencySymbol${String.format("%,.2f", wallet.balance)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Card Number / Identifier
                Text(
                    text = wallet.cardNumber.ifBlank { "•••• •••• •••• 4819" },
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    letterSpacing = 1.8.sp
                )

                // Bottom Row: Cardholder Name and Expiration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "CARDHOLDER",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.65f),
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = wallet.cardHolderName.ifBlank { "VIVEK BHARDWAJ" }.uppercase(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "EXPIRES",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.65f),
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = wallet.expiryDate.ifBlank { "12/28" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Freeze Overlay if Card is Frozen
            if (isFrozen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A).copy(alpha = 0.82f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Frozen",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "CARD FROZEN",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Canvas drawing routine for the collectible illustrated artwork variations
 */
fun DrawScope.drawCustomCardArtwork(
    artwork: String,
    accentColor: Color,
    primaryColor: Color,
    secondaryColor: Color
) {
    val w = size.width
    val h = size.height

    when (artwork.lowercase()) {
        "mascot_broco" -> {
            // Playful circular curly shapes and bouncy geometric curves
            drawCircle(
                color = accentColor.copy(alpha = 0.22f),
                radius = h * 0.45f,
                center = Offset(w * 0.82f, h * 0.40f)
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.40f),
                radius = h * 0.28f,
                center = Offset(w * 0.72f, h * 0.32f)
            )
            // Visor / sunglasses line
            drawRoundRect(
                color = accentColor.copy(alpha = 0.65f),
                topLeft = Offset(w * 0.68f, h * 0.38f),
                size = Size(w * 0.22f, h * 0.12f),
                cornerRadius = CornerRadius(12f, 12f)
            )
            // Cheerful geometric dots
            drawCircle(color = accentColor.copy(alpha = 0.8f), radius = 6f, center = Offset(w * 0.62f, h * 0.22f))
            drawCircle(color = Color.White.copy(alpha = 0.7f), radius = 4f, center = Offset(w * 0.88f, h * 0.20f))
        }

        "space_tonot" -> {
            // Space void with astronaut helmet curve, planet, and orbital path
            drawCircle(
                color = accentColor.copy(alpha = 0.28f),
                radius = h * 0.38f,
                center = Offset(w * 0.85f, h * 0.45f)
            )
            // Golden crescent visor
            drawArc(
                color = accentColor.copy(alpha = 0.7f),
                startAngle = 120f,
                sweepAngle = 200f,
                useCenter = false,
                topLeft = Offset(w * 0.74f, h * 0.30f),
                size = Size(w * 0.20f, h * 0.30f),
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
            // Orbit ring
            drawOval(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(w * 0.60f, h * 0.25f),
                size = Size(w * 0.36f, h * 0.42f),
                style = Stroke(width = 2f)
            )
            // Stars
            drawCircle(color = Color.White, radius = 3f, center = Offset(w * 0.62f, h * 0.20f))
            drawCircle(color = accentColor, radius = 4f, center = Offset(w * 0.58f, h * 0.65f))
            drawCircle(color = Color.White, radius = 2.5f, center = Offset(w * 0.92f, h * 0.15f))
        }

        "toast_bredo" -> {
            // Rounded toast slice shape silhouette & cheerful aura
            val toastPath = Path().apply {
                moveTo(w * 0.72f, h * 0.25f)
                cubicTo(w * 0.74f, h * 0.15f, w * 0.90f, h * 0.15f, w * 0.92f, h * 0.25f)
                lineTo(w * 0.93f, h * 0.65f)
                lineTo(w * 0.71f, h * 0.65f)
                close()
            }
            drawPath(path = toastPath, color = accentColor.copy(alpha = 0.25f))
            drawPath(path = toastPath, color = accentColor.copy(alpha = 0.6f), style = Stroke(width = 3f))
            // Butter sparkle
            drawCircle(color = accentColor, radius = 5f, center = Offset(w * 0.65f, h * 0.32f))
            drawCircle(color = Color.White, radius = 3.5f, center = Offset(w * 0.88f, h * 0.55f))
        }

        "cat_kocheng" -> {
            // Mischievous cat ears and playful lightning sparks
            val earLeft = Path().apply {
                moveTo(w * 0.72f, h * 0.40f)
                lineTo(w * 0.77f, h * 0.20f)
                lineTo(w * 0.82f, h * 0.40f)
                close()
            }
            val earRight = Path().apply {
                moveTo(w * 0.84f, h * 0.40f)
                lineTo(w * 0.89f, h * 0.20f)
                lineTo(w * 0.94f, h * 0.40f)
                close()
            }
            drawPath(path = earLeft, color = accentColor.copy(alpha = 0.55f))
            drawPath(path = earRight, color = accentColor.copy(alpha = 0.55f))
            // Spark lightning
            val sparkPath = Path().apply {
                moveTo(w * 0.65f, h * 0.25f)
                lineTo(w * 0.68f, h * 0.35f)
                lineTo(w * 0.64f, h * 0.37f)
                lineTo(w * 0.67f, h * 0.50f)
            }
            drawPath(path = sparkPath, color = Color.White.copy(alpha = 0.75f), style = Stroke(width = 3f, cap = StrokeCap.Round))
        }

        "dragon_drino" -> {
            // Dragon wing silhouette curve and laptop screen glow
            drawArc(
                color = accentColor.copy(alpha = 0.35f),
                startAngle = 180f,
                sweepAngle = 160f,
                useCenter = true,
                topLeft = Offset(w * 0.70f, h * 0.20f),
                size = Size(w * 0.25f, h * 0.45f)
            )
            // Tech sparkle
            drawCircle(color = Color.White, radius = 4f, center = Offset(w * 0.68f, h * 0.28f))
            drawCircle(color = accentColor, radius = 5f, center = Offset(w * 0.62f, h * 0.52f))
        }

        "bird_wuhu" -> {
            // Owl crescent eye and concentric feather curves
            drawCircle(
                color = accentColor.copy(alpha = 0.30f),
                radius = h * 0.32f,
                center = Offset(w * 0.82f, h * 0.42f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.65f),
                radius = h * 0.15f,
                center = Offset(w * 0.80f, h * 0.40f),
                style = Stroke(width = 4f)
            )
            drawArc(
                color = accentColor,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.68f, h * 0.35f),
                size = Size(w * 0.25f, h * 0.35f),
                style = Stroke(width = 3f)
            )
        }

        "music_yo" -> {
            // Soundwave equalizer arcs & microphone circle
            drawCircle(
                color = accentColor.copy(alpha = 0.35f),
                radius = h * 0.28f,
                center = Offset(w * 0.82f, h * 0.42f)
            )
            for (i in 1..4) {
                drawArc(
                    color = Color.White.copy(alpha = 0.25f * i),
                    startAngle = 120f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(w * (0.82f - i * 0.05f), h * (0.42f - i * 0.08f)),
                    size = Size(w * i * 0.10f, h * i * 0.16f),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
            }
        }

        "waves" -> {
            // Rhythmic layered ocean waves
            val wavePath1 = Path().apply {
                moveTo(w * 0.45f, h * 0.85f)
                cubicTo(w * 0.60f, h * 0.65f, w * 0.75f, h * 0.95f, w, h * 0.60f)
                lineTo(w, h)
                lineTo(w * 0.45f, h)
                close()
            }
            val wavePath2 = Path().apply {
                moveTo(w * 0.55f, h * 0.90f)
                cubicTo(w * 0.70f, h * 0.75f, w * 0.85f, h * 0.98f, w, h * 0.72f)
                lineTo(w, h)
                lineTo(w * 0.55f, h)
                close()
            }
            drawPath(path = wavePath1, color = accentColor.copy(alpha = 0.25f))
            drawPath(path = wavePath2, color = accentColor.copy(alpha = 0.40f))
        }

        "cyber_grid" -> {
            // Futuristic isometric perspective grid
            val strokeColor = accentColor.copy(alpha = 0.35f)
            for (x in 6..10) {
                drawLine(
                    color = strokeColor,
                    start = Offset(w * (x * 0.1f), 0f),
                    end = Offset(w * (x * 0.12f - 0.2f), h),
                    strokeWidth = 1.5f
                )
            }
            for (y in 1..4) {
                drawLine(
                    color = strokeColor,
                    start = Offset(w * 0.55f, h * (y * 0.25f)),
                    end = Offset(w, h * (y * 0.25f)),
                    strokeWidth = 1.5f
                )
            }
        }

        "aurora_lights" -> {
            // Smooth wavy northern light bands
            val p = Path().apply {
                moveTo(w * 0.50f, 0f)
                cubicTo(w * 0.75f, h * 0.35f, w * 0.60f, h * 0.65f, w * 0.90f, h)
                lineTo(w, h)
                lineTo(w, 0f)
                close()
            }
            drawPath(
                path = p,
                brush = Brush.linearGradient(
                    listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
                    start = Offset(w * 0.6f, 0f),
                    end = Offset(w, h)
                )
            )
        }

        else -> {
            // Minimalist geometric arcs
            drawCircle(
                color = accentColor.copy(alpha = 0.18f),
                radius = h * 0.35f,
                center = Offset(w * 0.82f, h * 0.38f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = h * 0.50f,
                center = Offset(w * 0.88f, h * 0.42f),
                style = Stroke(width = 2f)
            )
        }
    }
}

/**
 * Mini Collectible Card Preview Item for the Design Gallery
 * - Inspired by the colorful collectible card poster reference image
 */
@Composable
fun MiniCardPresetItem(
    preset: CardDesignPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = parseColorSafe(preset.primaryColorHex, Color(0xFF14B8A6))
    val secondaryColor = parseColorSafe(preset.secondaryColorHex, Color(0xFF042F2E))
    val accentColor = parseColorSafe(preset.accentColorHex, Color(0xFFA3E635))

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = tween(200),
        label = "mini_card_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(scale)
            .width(104.dp)
            .clickable(onClick = onClick)
            .testTag("preset_card_${preset.id}")
    ) {
        // Collectible Mini Card Shell
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = secondaryColor),
            border = BorderStroke(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
            modifier = Modifier
                .width(104.dp)
                .height(148.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(primaryColor, secondaryColor)
                        )
                    )
            ) {
                // Mini Artwork Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCustomCardArtwork(
                        artwork = preset.artwork,
                        accentColor = accentColor,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor
                    )
                }

                // Mini Card Typography & Badges
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = preset.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )

                        if (isSelected) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(16.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.Black,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        Text(
                            text = preset.category.uppercase(),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "VISA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = preset.name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Dedicated Card Customization Screen / Modal
 * - Mobile-first layout + Desktop two-column layout
 * - Large LIVE CARD PREVIEW at top/left that reflects every touch in real-time
 * - Segmented tabs: Design Gallery, Colors, Artwork, Style
 * - [ Save Card Design ] persists changes to database/backend
 */
@Composable
fun CardCustomizationScreen(
    wallet: WalletEntity,
    currencySymbol: String,
    onSaveCustomization: (walletId: String, theme: String, primaryColor: String, secondaryColor: String, accentColor: String, artwork: String, cardStyle: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Current live customization state initialized from existing wallet data
    var selectedTheme by remember { mutableStateOf(wallet.cardTheme.ifBlank { wallet.designId }) }
    var selectedPrimaryColor by remember { mutableStateOf(wallet.primaryColor) }
    var selectedSecondaryColor by remember { mutableStateOf(wallet.secondaryColor) }
    var selectedAccentColor by remember { mutableStateOf(wallet.accentColor) }
    var selectedArtwork by remember { mutableStateOf(wallet.artwork) }
    var selectedStyle by remember { mutableStateOf(wallet.cardStyle) }
    var activeTab by remember { mutableStateOf("gallery") } // "gallery", "colors", "artwork", "style"
    var isSaving by remember { mutableStateOf(false) }

    val colors = AppTheme.colors

    Surface(
        color = colors.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header: Back, Title, Close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.surfaceBorder),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textCrispWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Customize Your Card",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textCrispWhite
                        )
                        Text(
                            text = "Make your wallet card match your style.",
                            fontSize = 12.sp,
                            color = colors.textMutedLavender
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textMutedLavender
                    )
                }
            }

            HorizontalDivider(color = colors.surfaceBorder)

            // Responsive Layout: Single column on phones, two-column on tablets/desktops
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val isWide = maxWidth > 650.dp

                if (isWide) {
                    // Two-column layout
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left: Large Live Card Preview
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "LIVE CARD PREVIEW",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMutedLavender,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            CustomizablePaymentCardView(
                                wallet = wallet,
                                currencySymbol = currencySymbol,
                                overrideTheme = selectedTheme,
                                overridePrimaryColor = selectedPrimaryColor,
                                overrideSecondaryColor = selectedSecondaryColor,
                                overrideAccentColor = selectedAccentColor,
                                overrideArtwork = selectedArtwork,
                                overrideCardStyle = selectedStyle
                            )
                        }

                        // Right: Customization Controls
                        Column(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CustomizationControlsSection(
                                activeTab = activeTab,
                                onTabSelect = { activeTab = it },
                                selectedTheme = selectedTheme,
                                onSelectPreset = { preset ->
                                    selectedTheme = preset.id
                                    selectedPrimaryColor = preset.primaryColorHex
                                    selectedSecondaryColor = preset.secondaryColorHex
                                    selectedAccentColor = preset.accentColorHex
                                    selectedArtwork = preset.artwork
                                    selectedStyle = preset.cardStyle
                                },
                                selectedPrimaryColor = selectedPrimaryColor,
                                onSelectPrimaryColor = { selectedPrimaryColor = it },
                                selectedAccentColor = selectedAccentColor,
                                onSelectAccentColor = { selectedAccentColor = it },
                                selectedArtwork = selectedArtwork,
                                onSelectArtwork = { selectedArtwork = it },
                                selectedStyle = selectedStyle,
                                onSelectStyle = { selectedStyle = it }
                            )

                            Spacer(modifier = Modifier.weight(1f, fill = false))

                            FintechPrimaryButton(
                                text = if (isSaving) "Saving..." else "Save Card Design",
                                isLoading = isSaving,
                                onClick = {
                                    isSaving = true
                                    onSaveCustomization(
                                        wallet.id,
                                        selectedTheme,
                                        selectedPrimaryColor,
                                        selectedSecondaryColor,
                                        selectedAccentColor,
                                        selectedArtwork,
                                        selectedStyle
                                    )
                                },
                                testTag = "save_card_customization_btn"
                            )
                        }
                    }
                } else {
                    // Mobile-first single vertical column layout
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Large Live Card Preview at Top
                        Column {
                            Text(
                                text = "LIVE CARD PREVIEW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMutedLavender,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            CustomizablePaymentCardView(
                                wallet = wallet,
                                currencySymbol = currencySymbol,
                                overrideTheme = selectedTheme,
                                overridePrimaryColor = selectedPrimaryColor,
                                overrideSecondaryColor = selectedSecondaryColor,
                                overrideAccentColor = selectedAccentColor,
                                overrideArtwork = selectedArtwork,
                                overrideCardStyle = selectedStyle
                            )
                        }

                        // 2. Customization Controls Section
                        CustomizationControlsSection(
                            activeTab = activeTab,
                            onTabSelect = { activeTab = it },
                            selectedTheme = selectedTheme,
                            onSelectPreset = { preset ->
                                selectedTheme = preset.id
                                selectedPrimaryColor = preset.primaryColorHex
                                selectedSecondaryColor = preset.secondaryColorHex
                                selectedAccentColor = preset.accentColorHex
                                selectedArtwork = preset.artwork
                                selectedStyle = preset.cardStyle
                            },
                            selectedPrimaryColor = selectedPrimaryColor,
                            onSelectPrimaryColor = { selectedPrimaryColor = it },
                            selectedAccentColor = selectedAccentColor,
                            onSelectAccentColor = { selectedAccentColor = it },
                            selectedArtwork = selectedArtwork,
                            onSelectArtwork = { selectedArtwork = it },
                            selectedStyle = selectedStyle,
                            onSelectStyle = { selectedStyle = it }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3. Save Button
                        FintechPrimaryButton(
                            text = if (isSaving) "Saving..." else "Save Card Design",
                            isLoading = isSaving,
                            onClick = {
                                isSaving = true
                                onSaveCustomization(
                                    wallet.id,
                                    selectedTheme,
                                    selectedPrimaryColor,
                                    selectedSecondaryColor,
                                    selectedAccentColor,
                                    selectedArtwork,
                                    selectedStyle
                                )
                            },
                            testTag = "save_card_customization_btn"
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomizationControlsSection(
    activeTab: String,
    onTabSelect: (String) -> Unit,
    selectedTheme: String,
    onSelectPreset: (CardDesignPreset) -> Unit,
    selectedPrimaryColor: String,
    onSelectPrimaryColor: (String) -> Unit,
    selectedAccentColor: String,
    onSelectAccentColor: (String) -> Unit,
    selectedArtwork: String,
    onSelectArtwork: (String) -> Unit,
    selectedStyle: String,
    onSelectStyle: (String) -> Unit
) {
    val colors = AppTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Segmented Tabs: Design Gallery | Colors | Artwork | Style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surface)
                .border(1.dp, colors.surfaceBorder, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(
                "gallery" to "Design",
                "colors" to "Colors",
                "artwork" to "Artwork",
                "style" to "Style"
            ).forEach { (key, label) ->
                val isSelected = activeTab == key
                Surface(
                    onClick = { onTabSelect(key) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) colors.primaryAccent else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) colors.onPrimaryAccent else colors.textMutedLavender
                        )
                    }
                }
            }
        }

        // Tab Content
        when (activeTab) {
            "gallery" -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Collectible Card Presets",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textCrispWhite
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CARD_DESIGN_PRESETS, key = { it.id }) { preset ->
                            MiniCardPresetItem(
                                preset = preset,
                                isSelected = selectedTheme.equals(preset.id, ignoreCase = true),
                                onClick = { onSelectPreset(preset) }
                            )
                        }
                    }
                }
            }

            "colors" -> {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Primary Base Color Swatches
                    Text(
                        text = "Card Primary Color",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textCrispWhite
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CURATED_COLORS.forEach { (hex, name) ->
                            val color = parseColorSafe(hex, Color.Black)
                            val isSelected = selectedPrimaryColor.equals(hex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable { onSelectPrimaryColor(hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = name,
                                        tint = if (hex == "#18181B" || hex == "#374151") Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Accent Highlight Swatches
                    Text(
                        text = "Accent Highlight Color",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textCrispWhite
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CURATED_COLORS.reversed().forEach { (hex, name) ->
                            val color = parseColorSafe(hex, Color.Cyan)
                            val isSelected = selectedAccentColor.equals(hex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable { onSelectAccentColor(hex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = name,
                                        tint = if (hex == "#18181B" || hex == "#374151") Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "artwork" -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Card Artwork Illustration",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textCrispWhite
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ARTWORK_OPTIONS.forEach { (artKey, label) ->
                            val isSelected = selectedArtwork.equals(artKey, ignoreCase = true)

                            Surface(
                                onClick = { onSelectArtwork(artKey) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) colors.primaryAccent.copy(alpha = 0.18f) else colors.surface,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) colors.primaryAccent else colors.surfaceBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = colors.brandColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = label,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.brandColor else colors.textCrispWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "style" -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Visual Style & Finishes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textCrispWhite
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CARD_STYLES.forEach { (styleKey, label) ->
                            val isSelected = selectedStyle.equals(styleKey, ignoreCase = true)

                            Surface(
                                onClick = { onSelectStyle(styleKey) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) colors.primaryAccent.copy(alpha = 0.18f) else colors.surface,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) colors.primaryAccent else colors.surfaceBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = colors.brandColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = label,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.brandColor else colors.textCrispWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card Details Bottom Sheet / Screen displayed when user taps any wallet card
 */
@Composable
fun CardDetailsBottomSheet(
    wallet: WalletEntity,
    currencySymbol: String,
    onCustomizeClick: () -> Unit,
    onTopUpClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    var isFrozen by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.surfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drag Handle & Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = wallet.bankName.ifBlank { "Wallet Card" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textCrispWhite
                    )
                    Text(
                        text = "${wallet.cardBrand} • ${wallet.cardType.uppercase()}",
                        fontSize = 12.sp,
                        color = colors.textMutedLavender
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = colors.textMutedLavender)
                }
            }

            // Large Card Preview
            CustomizablePaymentCardView(
                wallet = wallet,
                currencySymbol = currencySymbol,
                isFrozen = isFrozen
            )

            // Primary Actions: [ Customize Card ] & [ Top Up ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCustomizeClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent,
                        contentColor = colors.onPrimaryAccent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("customize_card_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Customize Card",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                OutlinedButton(
                    onClick = onTopUpClick,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.surfaceBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("top_up_card_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = colors.brandColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Top Up",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = colors.brandColor
                    )
                }
            }

            // Quick Toggle: Freeze Card
            Surface(
                onClick = { isFrozen = !isFrozen },
                shape = RoundedCornerShape(14.dp),
                color = colors.surfaceVariant,
                border = BorderStroke(1.dp, colors.surfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (isFrozen) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (isFrozen) Color(0xFF38BDF8) else colors.textMutedLavender,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isFrozen) "Card is Frozen" else "Freeze Card",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textCrispWhite
                            )
                            Text(
                                text = if (isFrozen) "Tap to unfreeze transactions" else "Temporarily disable transactions",
                                fontSize = 11.5.sp,
                                color = colors.textMutedLavender
                            )
                        }
                    }

                    Text(
                        text = if (isFrozen) "UNFREEZE" else "FREEZE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFrozen) Color(0xFF38BDF8) else colors.textMutedLavender
                    )
                }
            }

            // Delete Card if supported
            if (onDeleteClick != null) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Delete Card", color = DangerRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
