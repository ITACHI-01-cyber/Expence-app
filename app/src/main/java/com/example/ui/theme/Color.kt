package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Data class representing the full adaptive semantic palette of the app.
 * Switches seamlessly when user toggles Accent Color in Settings.
 */
data class AppThemeColors(
    val background: Color,
    val backgroundDark: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceContainer: Color,
    val surfaceBorder: Color,
    val primaryAccent: Color,
    val primaryAccentLight: Color,
    val onPrimaryAccent: Color,
    val ctaButton: Color,
    val onCtaButton: Color,
    val textCrispWhite: Color,
    val textMutedLavender: Color,
    val textSoftPurple: Color,
    val accentGlow: Color,
    val brandColor: Color
)

// ── 1. Purple / Plum Palette (Default / Image Reference) ──
val PurpleThemeColors = AppThemeColors(
    background = Color(0xFF322A4E),
    backgroundDark = Color(0xFF27203F),
    surface = Color(0xFF423963),
    surfaceVariant = Color(0xFF4C4270),
    surfaceContainer = Color(0xFF3B3359),
    surfaceBorder = Color(0xFF554B7C),
    primaryAccent = Color(0xFFF5CE9F),      // Warm Peach / Sand
    primaryAccentLight = Color(0xFFFBE4C6),
    onPrimaryAccent = Color(0xFF2B2245),
    ctaButton = Color(0xFF262553),          // Deep Slate Blue
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFFFFFFFF),
    textMutedLavender = Color(0xFFB3A8CE),
    textSoftPurple = Color(0xFF968AA9),
    accentGlow = Color(0xFFF5CE9F),
    brandColor = Color(0xFF8B5CF6)
)

// ── 2. Blue / Sapphire Palette (Fintech Modern) ──
val BlueThemeColors = AppThemeColors(
    background = Color(0xFF0F172A),
    backgroundDark = Color(0xFF090D16),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF293548),
    surfaceContainer = Color(0xFF182234),
    surfaceBorder = Color(0xFF384B66),
    primaryAccent = Color(0xFF60A5FA),      // Electric Sky Blue
    primaryAccentLight = Color(0xFF93C5FD),
    onPrimaryAccent = Color(0xFF0F172A),
    ctaButton = Color(0xFF1D4ED8),          // Royal Navy Blue
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFFFFFFFF),
    textMutedLavender = Color(0xFF94A3B8),
    textSoftPurple = Color(0xFF64748B),
    accentGlow = Color(0xFF60A5FA),
    brandColor = Color(0xFF3B82F6)
)

// ── 3. Emerald / Mint Palette (Wealth & Growth) ──
val EmeraldThemeColors = AppThemeColors(
    background = Color(0xFF062016),
    backgroundDark = Color(0xFF03140E),
    surface = Color(0xFF0E3827),
    surfaceVariant = Color(0xFF154A34),
    surfaceContainer = Color(0xFF0A2E20),
    surfaceBorder = Color(0xFF1F6347),
    primaryAccent = Color(0xFF34D399),      // Mint Emerald
    primaryAccentLight = Color(0xFF6EE7B7),
    onPrimaryAccent = Color(0xFF022C22),
    ctaButton = Color(0xFF047857),          // Deep Pine Spruce
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFFFFFFFF),
    textMutedLavender = Color(0xFFA7F3D0).copy(alpha = 0.8f),
    textSoftPurple = Color(0xFF6EE7B7).copy(alpha = 0.7f),
    accentGlow = Color(0xFF34D399),
    brandColor = Color(0xFF10B981)
)

// ── 4. Rose / Crimson Palette (Luxury Velvet - In User Screenshot) ──
val RoseThemeColors = AppThemeColors(
    background = Color(0xFF26101B),
    backgroundDark = Color(0xFF1A0A12),
    surface = Color(0xFF3D1B2B),
    surfaceVariant = Color(0xFF4F2438),
    surfaceContainer = Color(0xFF331624),
    surfaceBorder = Color(0xFF6B2949),
    primaryAccent = Color(0xFFFB7185),      // Warm Coral Rose
    primaryAccentLight = Color(0xFFFDA4AF),
    onPrimaryAccent = Color(0xFF4C0519),
    ctaButton = Color(0xFF9F1239),          // Deep Crimson Ruby
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFFFFFFFF),
    textMutedLavender = Color(0xFFF472B6).copy(alpha = 0.8f),
    textSoftPurple = Color(0xFFFDA4AF).copy(alpha = 0.7f),
    accentGlow = Color(0xFFFB7185),
    brandColor = Color(0xFFF43F5E)
)

// ── Light Theme Palettes ──
val PurpleLightColors = AppThemeColors(
    background = Color(0xFFF6F4FB),
    backgroundDark = Color(0xFFECE7F7),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1EDFA),
    surfaceContainer = Color(0xFFE8E2F5),
    surfaceBorder = Color(0xFFD8CEEE),
    primaryAccent = Color(0xFF7C3AED),      // Royal Purple
    primaryAccentLight = Color(0xFFDDD6FE),
    onPrimaryAccent = Color(0xFFFFFFFF),
    ctaButton = Color(0xFF4C1D95),          // Deep Purple
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFF1E1730),     // Dark text in light mode
    textMutedLavender = Color(0xFF6B5C8A),  // Secondary text
    textSoftPurple = Color(0xFF8B7AA9),
    accentGlow = Color(0xFF7C3AED),
    brandColor = Color(0xFF8B5CF6)
)

val BlueLightColors = AppThemeColors(
    background = Color(0xFFF1F5F9),
    backgroundDark = Color(0xFFE2E8F0),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE0EBF7),
    surfaceContainer = Color(0xFFD6E4F5),
    surfaceBorder = Color(0xFFBFDBFE),
    primaryAccent = Color(0xFF2563EB),      // Bright Blue
    primaryAccentLight = Color(0xFFBFDBFE),
    onPrimaryAccent = Color(0xFFFFFFFF),
    ctaButton = Color(0xFF1E40AF),          // Deep Blue CTA
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFF0F172A),     // Dark text in light mode
    textMutedLavender = Color(0xFF475569),
    textSoftPurple = Color(0xFF64748B),
    accentGlow = Color(0xFF2563EB),
    brandColor = Color(0xFF3B82F6)
)

val EmeraldLightColors = AppThemeColors(
    background = Color(0xFFF0FDF4),
    backgroundDark = Color(0xFFDCFCE7),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE1F8EB),
    surfaceContainer = Color(0xFFD1F4E0),
    surfaceBorder = Color(0xFFA7F3D0),
    primaryAccent = Color(0xFF059669),      // Vivid Emerald
    primaryAccentLight = Color(0xFFA7F3D0),
    onPrimaryAccent = Color(0xFFFFFFFF),
    ctaButton = Color(0xFF065F46),          // Deep Pine
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFF064E3B),     // Dark text in light mode
    textMutedLavender = Color(0xFF374151),
    textSoftPurple = Color(0xFF4B5563),
    accentGlow = Color(0xFF059669),
    brandColor = Color(0xFF10B981)
)

val RoseLightColors = AppThemeColors(
    background = Color(0xFFFFF1F2),
    backgroundDark = Color(0xFFFFE4E6),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFDE8EB),
    surfaceContainer = Color(0xFFFCD5DB),
    surfaceBorder = Color(0xFFFECDD3),
    primaryAccent = Color(0xFFE11D48),      // Rose Crimson
    primaryAccentLight = Color(0xFFFECDD3),
    onPrimaryAccent = Color(0xFFFFFFFF),
    ctaButton = Color(0xFF9F1239),          // Deep Crimson
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFF4C0519),     // Dark text in light mode
    textMutedLavender = Color(0xFF881337),
    textSoftPurple = Color(0xFF9F1239),
    accentGlow = Color(0xFFE11D48),
    brandColor = Color(0xFFF43F5E)
)

/**
 * Returns the theme palette based on the selected accent name and mode.
 */
fun getThemeColors(accentColorName: String, isDark: Boolean = true): AppThemeColors {
    return if (isDark) {
        when (accentColorName.lowercase()) {
            "blue" -> BlueThemeColors
            "emerald", "green" -> EmeraldThemeColors
            "rose", "red" -> RoseThemeColors
            else -> PurpleThemeColors
        }
    } else {
        when (accentColorName.lowercase()) {
            "blue" -> BlueLightColors
            "emerald", "green" -> EmeraldLightColors
            "rose", "red" -> RoseLightColors
            else -> PurpleLightColors
        }
    }
}

val LocalAppColors = staticCompositionLocalOf { PurpleThemeColors }

object AppTheme {
    val colors: AppThemeColors
        @Composable
        get() = LocalAppColors.current
}

// ── Legacy Compatibility Tokens ──
val PlumBackground = PurpleThemeColors.background
val PlumBackgroundDark = PurpleThemeColors.backgroundDark
val PlumSurface = PurpleThemeColors.surface
val PlumSurfaceVariant = PurpleThemeColors.surfaceVariant
val PlumSurfaceContainer = PurpleThemeColors.surfaceContainer
val PlumSurfaceBorder = PurpleThemeColors.surfaceBorder
val WarmPeach = PurpleThemeColors.primaryAccent
val WarmPeachLight = PurpleThemeColors.primaryAccentLight
val WarmPeachDark = Color(0xFFDCA970)
val OnWarmPeachText = PurpleThemeColors.onPrimaryAccent
val DeepBlueCTA = PurpleThemeColors.ctaButton
val DeepBlueCTAHover = Color(0xFF32316C)
val DeepBlueBorder = Color(0xFF3B3A78)
val TextCrispWhite = Color(0xFFFFFFFF)
val TextMutedLavender = PurpleThemeColors.textMutedLavender
val TextSoftPurple = PurpleThemeColors.textSoftPurple

// Slider multi-color tracks
val SliderTrackBlue = Color(0xFF8BB5E8)
val SliderTrackPurple = Color(0xFFA594D6)
val SliderTrackPeach = Color(0xFFF3A79E)

val PurplePrimary = WarmPeach
val PurpleDark = DeepBlueCTA
val PurpleLight = WarmPeachLight
val PurpleContainer = PlumSurface

val Slate900 = PlumBackgroundDark
val Slate800 = PlumBackground
val Slate700 = PlumSurface
val Slate600 = PlumSurfaceVariant
val Slate500 = PlumSurfaceBorder
val Slate400 = TextMutedLavender
val Slate200 = TextCrispWhite.copy(alpha = 0.8f)
val Slate100 = PlumSurface
val Slate50 = PlumBackground

val SuccessGreen = Color(0xFF34D399)
val SuccessGreenLight = Color(0xFFD1FAE5)
val DangerRed = Color(0xFFF87171)
val DangerRedLight = Color(0xFFFEE2E2)
val WarningAmber = Color(0xFFFBBF24)
val WarningAmberLight = Color(0xFFFEF3C7)

// Card Gradients
val CardMidnightStart = Color(0xFF1E1A38)
val CardMidnightEnd = Color(0xFF423963)
val CardSunsetStart = Color(0xFFE08D46)
val CardSunsetEnd = Color(0xFFF5CE9F)
val CardAuroraStart = Color(0xFF2A4365)
val CardAuroraEnd = Color(0xFF5A67D8)
val CardBerryStart = Color(0xFF6B21A8)
val CardBerryEnd = Color(0xFFA855F7)
val CardOceanStart = Color(0xFF1E3A8A)
val CardOceanEnd = Color(0xFF3B82F6)
val CardMintStart = Color(0xFF065F46)
val CardMintEnd = Color(0xFF10B981)
val CardLavenderStart = Color(0xFFC5B4F5)
val CardLavenderEnd = Color(0xFF8E7CE3)
val CardGlassStart = Color(0xFF554B7C)
val CardGlassEnd = Color(0xFF423963)
