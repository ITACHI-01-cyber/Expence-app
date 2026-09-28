package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun ExpenceTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColorName: String = "purple",
    content: @Composable () -> Unit
) {
    val themeColors = getThemeColors(accentColorName, darkTheme)

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = themeColors.primaryAccent,
            onPrimary = themeColors.onPrimaryAccent,
            primaryContainer = themeColors.surface,
            onPrimaryContainer = themeColors.textCrispWhite,
            secondary = themeColors.ctaButton,
            onSecondary = themeColors.onCtaButton,
            tertiary = themeColors.brandColor,
            background = themeColors.background,
            surface = themeColors.surface,
            surfaceVariant = themeColors.surfaceVariant,
            surfaceContainer = themeColors.surfaceContainer,
            onBackground = themeColors.textCrispWhite,
            onSurface = themeColors.textCrispWhite,
            onSurfaceVariant = themeColors.textMutedLavender,
            outline = themeColors.surfaceBorder
        )
    } else {
        lightColorScheme(
            primary = themeColors.primaryAccent,
            onPrimary = themeColors.onPrimaryAccent,
            primaryContainer = themeColors.surfaceVariant,
            onPrimaryContainer = themeColors.textCrispWhite,
            secondary = themeColors.ctaButton,
            onSecondary = themeColors.onCtaButton,
            tertiary = themeColors.brandColor,
            background = themeColors.background,
            surface = themeColors.surface,
            surfaceVariant = themeColors.surfaceVariant,
            surfaceContainer = themeColors.surfaceContainer,
            onBackground = themeColors.textCrispWhite,
            onSurface = themeColors.textCrispWhite,
            onSurfaceVariant = themeColors.textMutedLavender,
            outline = themeColors.surfaceBorder
        )
    }

    CompositionLocalProvider(LocalAppColors provides themeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
