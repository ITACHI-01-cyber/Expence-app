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

// ── 1. Default Fintech Modern Palette (Dark Mode) ──
val PurpleThemeColors = AppThemeColors(
    background = Color(0xFF0C0E14),
    backgroundDark = Color(0xFF08090D),
    surface = Color(0xFF151821),
    surfaceVariant = Color(0xFF1E2230),
    surfaceContainer = Color(0xFF191D28),
    surfaceBorder = Color(0xFF282D3D),
    primaryAccent = Color(0xFF6366F1),      // Modern Indigo
    primaryAccentLight = Color(0xFF818CF8),
    onPrimaryAccent = Color(0xFFFFFFFF),
    ctaButton = Color(0xFF6366F1),          // Primary Indigo CTA
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFFF9FAFB),
    textMutedLavender = Color(0xFF94A3B8),
    textSoftPurple = Color(0xFF64748B),
    accentGlow = Color(0xFF6366F1),
    brandColor = Color(0xFF818CF8)
)

// ── 2. Blue / Sapphire Palette (Fintech Modern) ──
val BlueThemeColors = AppThemeColors(
    background = Color(0xFF0B0F19),
    backgroundDark = Color(0xFF070A12),
    surface = Color(0xFF131B2E),
    surfaceVariant = Color(0xFF1C2740),
    surfaceContainer = Color(0xFF162035),
    surfaceBorder = Color(0xFF243354),
    primaryAccent = Color(0xFF3B82F6),      // Electric Sky Blue
    primaryAccentLight = Color(0xFF60A5FA),
    onPrimaryAccent = Color(0xFFFFFFFF),
    ctaButton = Color(0xFF2563EB),          // Royal Navy Blue
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFFF8FAFC),
    textMutedLavender = Color(0xFF94A3B8),
    textSoftPurple = Color(0xFF64748B),
    accentGlow = Color(0xFF3B82F6),
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
    textCrispWhite = Color(0xFFF9FAFB),
    textMutedLavender = Color(0xFFA7F3D0).copy(alpha = 0.8f),
    textSoftPurple = Color(0xFF6EE7B7).copy(alpha = 0.7f),
    accentGlow = Color(0xFF34D399),
    brandColor = Color(0xFF10B981)
)

// ── 4. Rose / Crimson Palette (Luxury Velvet) ──
val RoseThemeColors = AppThemeColors(
    background = Color(0xFF180A12),
    backgroundDark = Color(0xFF10060C),
    surface = Color(0xFF2B1320),
    surfaceVariant = Color(0xFF3C1C2E),
    surfaceContainer = Color(0xFF24101B),
    surfaceBorder = Color(0xFF4C2139),
    primaryAccent = Color(0xFFFB7185),      // Warm Coral Rose
    primaryAccentLight = Color(0xFFFDA4AF),
    onPrimaryAccent = Color(0xFFFFFFFF),
    ctaButton = Color(0xFFE11D48),          // Deep Crimson Ruby
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFFFFF1F2),
    textMutedLavender = Color(0xFFFDA4AF),
    textSoftPurple = Color(0xFFFB7185),
    accentGlow = Color(0xFFFB7185),
    brandColor = Color(0xFFF43F5E)
)

// ── Light Theme Palettes (Premium Minimal Fintech #F5F5F7) ──
val PurpleLightColors = AppThemeColors(
    background = Color(0xFFF5F5F7),        // Soft off-white as requested
    backgroundDark = Color(0xFFEBEBF0),
    surface = Color(0xFFFFFFFF),           // Crisp white cards
    surfaceVariant = Color(0xFFF0F1F5),    // Subtle light-gray pills
    surfaceContainer = Color(0xFFE8E9EE),
    surfaceBorder = Color(0xFFE5E7EB),     // Hairline border
    primaryAccent = Color(0xFF111827),     // Near-black charcoal
    primaryAccentLight = Color(0xFF374151),
    onPrimaryAccent = Color(0xFFFFFFFF),
    ctaButton = Color(0xFF111827),         // Dark premium CTA
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFF111827),    // Near-black primary text
    textMutedLavender = Color(0xFF6B7280), // Muted gray secondary text
    textSoftPurple = Color(0xFF9CA3AF),    // Subtle tertiary text
    accentGlow = Color(0xFF4F46E5),
    brandColor = Color(0xFF4F46E5)         // Restrained indigo accent
)

val BlueLightColors = AppThemeColors(
    background = Color(0xFFF5F5F7),
    backgroundDark = Color(0xFFEBEBF0),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0F2F7),
    surfaceContainer = Color(0xFFE5E9F2),
    surfaceBorder = Color(0xFFE2E8F0),
    primaryAccent = Color(0xFF1E293B),
    primaryAccentLight = Color(0xFF3B82F6),
    onPrimaryAccent = Color(0xFFFFFFFF),
    ctaButton = Color(0xFF0F172A),
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFF0F172A),
    textMutedLavender = Color(0xFF64748B),
    textSoftPurple = Color(0xFF94A3B8),
    accentGlow = Color(0xFF2563EB),
    brandColor = Color(0xFF2563EB)
)

val EmeraldLightColors = AppThemeColors(
    background = Color(0xFFF5F5F7),
    backgroundDark = Color(0xFFEBEBF0),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0F4F2),
    surfaceContainer = Color(0xFFE2ECE6),
    surfaceBorder = Color(0xFFE5E7EB),
    primaryAccent = Color(0xFF064E3B),
    primaryAccentLight = Color(0xFF059669),
    onPrimaryAccent = Color(0xFFFFFFFF),
    ctaButton = Color(0xFF064E3B),
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFF0F172A),
    textMutedLavender = Color(0xFF6B7280),
    textSoftPurple = Color(0xFF9CA3AF),
    accentGlow = Color(0xFF059669),
    brandColor = Color(0xFF10B981)
)

val RoseLightColors = AppThemeColors(
    background = Color(0xFFF5F5F7),
    backgroundDark = Color(0xFFEBEBF0),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF6F0F2),
    surfaceContainer = Color(0xFFECE0E4),
    surfaceBorder = Color(0xFFE5E7EB),
    primaryAccent = Color(0xFF881337),
    primaryAccentLight = Color(0xFFE11D48),
    onPrimaryAccent = Color(0xFFFFFFFF),
    ctaButton = Color(0xFF881337),
    onCtaButton = Color(0xFFFFFFFF),
    textCrispWhite = Color(0xFF0F172A),
    textMutedLavender = Color(0xFF6B7280),
    textSoftPurple = Color(0xFF9CA3AF),
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
