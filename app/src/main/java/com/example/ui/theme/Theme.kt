package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Expose card gradient colors via staticCompositionLocalOf
val LocalCardGradient = staticCompositionLocalOf {
    Palette(
        primary = Color(0xFF0F9D58),
        secondary = Color(0xFF34A853),
        tertiary = Color(0xFF00796B),
        cardGradientStart = Color(0xFFE8F5E9),
        cardGradientEnd = Color(0xFFC8E6C9)
    )
}

@Composable
fun NoorQuranTheme(
    themeMode: String = "dark",
    accentColor: String = "Emerald",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "light" -> false
        "dark" -> true
        "night" -> true
        else -> isSystemDark
    }

    val palette = AccentPalettes.getPalette(accentColor, isDark)

    val colorScheme = if (isDark) {
        val bg = if (themeMode == "night") NightBackground else DarkBackground
        val surf = if (themeMode == "night") NightSurface else DarkSurface
        val onBg = if (themeMode == "night") NightTextSepia else DarkOnBackground
        darkColorScheme(
            primary = palette.primary,
            secondary = palette.secondary,
            tertiary = palette.tertiary,
            background = bg,
            surface = surf,
            onBackground = onBg,
            onSurface = onBg,
            surfaceVariant = surf,
            onSurfaceVariant = onBg.copy(alpha = 0.8f)
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            secondary = palette.secondary,
            tertiary = palette.tertiary,
            background = LightBackground,
            surface = LightSurface,
            onBackground = LightOnBackground,
            onSurface = LightOnSurface,
            surfaceVariant = LightSurface,
            onSurfaceVariant = LightOnSurface.copy(alpha = 0.8f)
        )
    }

    CompositionLocalProvider(
        LocalCardGradient provides palette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
