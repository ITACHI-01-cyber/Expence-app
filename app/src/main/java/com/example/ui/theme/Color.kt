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

data class CoordinatedPalette(
    val letter: String,
    val name: String,
    val colors: List<Color>
) {
    val primary: Color get() = colors.getOrElse(0) { Color(0xFF6366F1) }
    val secondary: Color get() = colors.getOrElse(1) { Color(0xFF818CF8) }
    val accent: Color get() = colors.getOrElse(2) { Color(0xFFA855F7) }
    val surface: Color get() = colors.getOrElse(3) { Color(0xFF151821) }
}

val AllCoordinatedPalettes = listOf(
    CoordinatedPalette("A", "Plum Velvet", listOf(Color(0xFF4D3A4D), Color(0xFFBE5CA9), Color(0xFFD59CC5), Color(0xFFEADADA))),
    CoordinatedPalette("B", "Cyan Ocean", listOf(Color(0xFF43D9E7), Color(0xFF3EC4D0), Color(0xFF393C83), Color(0xFF2A2C5F))),
    CoordinatedPalette("C", "Crimson Ruby", listOf(Color(0xFF2B2024), Color(0xFFA80139), Color(0xFFFD0053), Color(0xFFFFFFFF))),
    CoordinatedPalette("D", "Emerald Gold", listOf(Color(0xFFFFD716), Color(0xFF0DA574), Color(0xFF073358), Color(0xFF001F3E))),
    CoordinatedPalette("E", "Electric Sky", listOf(Color(0xFF3B4A6B), Color(0xFF23B2DA), Color(0xFFF1D43A), Color(0xFFF23456))),
    CoordinatedPalette("F", "Lavender Mint", listOf(Color(0xFF7A56D0), Color(0xFF4FC0E8), Color(0xFF5BE7C4), Color(0xFFF6F7FB))),
    CoordinatedPalette("G", "Sapphire Pink", listOf(Color(0xFFDAEAF7), Color(0xFF0C8ABC), Color(0xFF124E96), Color(0xFFFF008E))),
    CoordinatedPalette("H", "Lime Teal", listOf(Color(0xFFFFF5F4), Color(0xFFB1D430), Color(0xFF1A8B9D), Color(0xFF000000))),
    CoordinatedPalette("I", "Slate Rust", listOf(Color(0xFF1A273A), Color(0xFF3E4A62), Color(0xFFC24D2C), Color(0xFFD9D9D7))),
    CoordinatedPalette("J", "Citrus Leaf", listOf(Color(0xFFFAFFA3), Color(0xFFC6E772), Color(0xFF66D47E), Color(0xFF073835))),
    CoordinatedPalette("K", "Peach Teal", listOf(Color(0xFFFFEED0), Color(0xFFF4C180), Color(0xFF00A79D), Color(0xFF007064))),
    CoordinatedPalette("L", "Purple Mint", listOf(Color(0xFF9765C8), Color(0xFF099A97), Color(0xFF15CDA9), Color(0xFFF0E4E4))),
    CoordinatedPalette("M", "Espresso Spice", listOf(Color(0xFFE2DED3), Color(0xFF857671), Color(0xFF4E413B), Color(0xFFFF6D24))),
    CoordinatedPalette("N", "Sky Neon", listOf(Color(0xFF4A89AC), Color(0xFFACE5F6), Color(0xFFE3FCF9), Color(0xFFFAEE5A))),
    CoordinatedPalette("O", "Sunset Violet", listOf(Color(0xFFED743F), Color(0xFFFFAF50), Color(0xFF423465), Color(0xFF824C97))),
    CoordinatedPalette("P", "Forest Sage", listOf(Color(0xFF3F3F3F), Color(0xFF437A5B), Color(0xFFB4CD93), Color(0xFFFBF5B7)))
)

fun parseColorSafe(colorHex: String?, fallback: Color): Color {
    if (colorHex.isNullOrBlank()) return fallback
    return try {
        val clean = colorHex.trim()
        val hex = if (clean.startsWith("#")) clean else "#$clean"
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}

fun getCardGradient(designId: String, primaryColorHex: String = "", secondaryColorHex: String = ""): androidx.compose.ui.graphics.Brush {
    val pCol = parseColorSafe(primaryColorHex, Color.Unspecified)
    val sCol = parseColorSafe(secondaryColorHex, Color.Unspecified)

    if (pCol != Color.Unspecified && sCol != Color.Unspecified) {
        return androidx.compose.ui.graphics.Brush.linearGradient(listOf(pCol, sCol))
    }

    return when (designId.lowercase().trim()) {
        "sunset" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(CardSunsetStart, CardSunsetEnd))
        "aurora" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(CardAuroraStart, CardAuroraEnd))
        "berry" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(CardBerryStart, CardBerryEnd))
        "ocean", "ocean-gradient" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(CardOceanStart, CardOceanEnd))
        "mint" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(CardMintStart, CardMintEnd))
        "lavender" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(CardLavenderStart, CardLavenderEnd))
        "glass" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(CardGlassStart, CardGlassEnd))
        "silver", "minimal" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF94A3B8), Color(0xFF475569)))
        "scenic" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF7A56D0), Color(0xFF4FC0E8)))
        "fampay", "neon" -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF9B6DFF), Color(0xFF38BDF8)))
        else -> androidx.compose.ui.graphics.Brush.linearGradient(listOf(CardMidnightStart, CardMidnightEnd))
    }
}

/**
 * Returns the theme palette based on the selected accent name and mode.
 */
fun getThemeColors(accentColorName: String, isDark: Boolean = true): AppThemeColors {
    val matchedPalette = AllCoordinatedPalettes.firstOrNull { it.letter.equals(accentColorName, ignoreCase = true) }
    val customAccent = matchedPalette?.primary

    return if (isDark) {
        val base = when (accentColorName.lowercase()) {
            "blue" -> BlueThemeColors
            "emerald", "green" -> EmeraldThemeColors
            "rose", "red" -> RoseThemeColors
            else -> PurpleThemeColors
        }
        if (customAccent != null) {
            base.copy(
                primaryAccent = customAccent,
                primaryAccentLight = matchedPalette.secondary,
                ctaButton = customAccent,
                accentGlow = customAccent,
                brandColor = matchedPalette.accent
            )
        } else base
    } else {
        val base = when (accentColorName.lowercase()) {
            "blue" -> BlueLightColors
            "emerald", "green" -> EmeraldLightColors
            "rose", "red" -> RoseLightColors
            else -> PurpleLightColors
        }
        if (customAccent != null) {
            base.copy(
                primaryAccent = customAccent,
                primaryAccentLight = matchedPalette.secondary,
                ctaButton = customAccent,
                accentGlow = customAccent,
                brandColor = matchedPalette.accent
            )
        } else base
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
