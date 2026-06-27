package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Neutral Base Colors
val LightBackground = Color(0xFFFAF7F2) // Warm soft cream paper background
val LightSurface = Color(0xFFFFFFFF) // Fully opaque clean white surface
val LightOnBackground = Color(0xFF262A30) // Soft warm charcoal instead of harsh pure black
val LightOnSurface = Color(0xFF262A30)

val DarkBackground = Color(0xFF14171A) // Elegant soft matte slate background, soothing for dark mode
val DarkSurface = Color(0xFF1F2226) // Fully opaque dark charcoal surface
val DarkOnBackground = Color(0xFFE2E4E7) // Gentle off-white primary text
val DarkOnSurface = Color(0xFFE2E4E7)

val NightBackground = Color(0xFF0C0E12) // Soothing deep dark midnight
val NightSurface = Color(0xFF14181E) // Fully opaque deep dark slate surface
val NightOnBackground = Color(0xFFDCDFE3)
val NightOnSurface = Color(0xFFDCDFE3)
val NightTextSepia = Color(0xFFEEDCCB) // Rich soft sepia text for comfortable reading

// Dynamic Accent Color Palettes (Primary, Secondary, Tertiary)
object AccentPalettes {
    // 1. Emerald Green (Traditional, Peaceful)
    val EmeraldLight = Palette(
        primary = Color(0xFF1E6F4A), // Rich forest emerald
        secondary = Color(0xFF2D825A),
        tertiary = Color(0xFF145435),
        cardGradientStart = Color(0xFFF0F7F3),
        cardGradientEnd = Color(0xFFE2F0E7)
    )
    val EmeraldDark = Palette(
        primary = Color(0xFF5EDAA4), // Softer, highly eye-friendly mint emerald
        secondary = Color(0xFF3BAE7C),
        tertiary = Color(0xFF206143),
        cardGradientStart = Color(0xFF18231E),
        cardGradientEnd = Color(0xFF101A16)
    )

    // 2. Imperial Gold (Royal, Premium)
    val GoldLight = Palette(
        primary = Color(0xFFB59410), // Softer warm golden bronze
        secondary = Color(0xFF9E800A),
        tertiary = Color(0xFF7E6504),
        cardGradientStart = Color(0xFFFAF8ED),
        cardGradientEnd = Color(0xFFF3EECF)
    )
    val GoldDark = Palette(
        primary = Color(0xFFE6C343), // Comforting golden amber
        secondary = Color(0xFFC7A221),
        tertiary = Color(0xFF94760D),
        cardGradientStart = Color(0xFF222018),
        cardGradientEnd = Color(0xFF1A1811)
    )

    // 3. Ocean Teal (Sober, Minimalist)
    val TealLight = Palette(
        primary = Color(0xFF0F7A7A), // Serene deep teal
        secondary = Color(0xFF1C8F8F),
        tertiary = Color(0xFF095252),
        cardGradientStart = Color(0xFFEDF8F8),
        cardGradientEnd = Color(0xFFD3EBEB)
    )
    val TealDark = Palette(
        primary = Color(0xFF4FD1C5), // Softer, soothing ocean teal
        secondary = Color(0xFF319795),
        tertiary = Color(0xFF234E52),
        cardGradientStart = Color(0xFF152222),
        cardGradientEnd = Color(0xFF0D1818)
    )

    // 4. Royal Blue (Elegant, Professional)
    val BlueLight = Palette(
        primary = Color(0xFF2B55BA), // Softer deep blue
        secondary = Color(0xFF416EDD),
        tertiary = Color(0xFF1B3D93),
        cardGradientStart = Color(0xFFF0F4FC),
        cardGradientEnd = Color(0xFFDCE5F7)
    )
    val BlueDark = Palette(
        primary = Color(0xFF7EA6F6), // Peaceful periwinkle blue
        secondary = Color(0xFF568AF2),
        tertiary = Color(0xFF2D57C1),
        cardGradientStart = Color(0xFF171E2D),
        cardGradientEnd = Color(0xFF101520)
    )

    // 5. Rich Crimson (Vibrant, Bold)
    val CrimsonLight = Palette(
        primary = Color(0xFFA51D24), // Muted deep crimson/burgundy
        secondary = Color(0xFFC92A31),
        tertiary = Color(0xFF7A1116),
        cardGradientStart = Color(0xFFFAF0F1),
        cardGradientEnd = Color(0xFFF5D6D8)
    )
    val CrimsonDark = Palette(
        primary = Color(0xFFF87171), // Soft clay coral/red
        secondary = Color(0xFFEF4444),
        tertiary = Color(0xFFB91C1C),
        cardGradientStart = Color(0xFF281818),
        cardGradientEnd = Color(0xFF1D1010)
    )

    fun getPalette(accentName: String, isDark: Boolean): Palette {
        return when (accentName) {
            "Gold" -> if (isDark) GoldDark else GoldLight
            "Teal" -> if (isDark) TealDark else TealLight
            "RoyalBlue" -> if (isDark) BlueDark else BlueLight
            "Crimson" -> if (isDark) CrimsonDark else CrimsonLight
            else -> if (isDark) EmeraldDark else EmeraldLight // Default to Emerald
        }
    }
}

data class Palette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val cardGradientStart: Color,
    val cardGradientEnd: Color
)
