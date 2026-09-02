package com.ipodmodern.audio.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class ThemePalette(
    val bg: Color = Color(0xFF000000),
    val surface: Color = Color(0xFF0D0E11),
    val elevated: Color = Color(0xFF16181D),
    val pill: Color = Color(0xFF1E2128),
    val trackBg: Color = Color(0xFF262626),
    val border: Color = Color(0x1AFFFFFF),
    val borderSubtle: Color = Color(0x0DFFFFFF),
    val borderHighlight: Color = Color(0x33FFFFFF),
    val accent: Color = Color(0xFFE50914),
    val accentLight: Color = Color(0xFFFF4D5E),
    val accentDark: Color = Color(0xFFB80710),
    val accentGlow: Color = Color(0x33E50914),
    val pillBg: Color = Color(0x2EE50914),
    val textPrimary: Color = Color(0xFFFFFFFF),
    val textSecondary: Color = Color(0xFF9E9EA4),
    val textMuted: Color = Color(0xFF71717A),
    val textDark: Color = Color(0xFF383D48)
)

val LocalThemePalette = staticCompositionLocalOf { ThemePalette() }

object ModernThemeTokens {
    val palette: ThemePalette
        @Composable
        get() = LocalThemePalette.current
}

fun getAccentColor(name: String): Color {
    return when (name) {
        "Apple Red", "Red", "Crimson" -> Color(0xFFE50914)
        "Cyber Gold", "Cyber" -> Color(0xFFFFD159)
        "Electric Cyan", "Electric" -> Color(0xFF00E5FF)
        "Neon Rose", "Neon" -> Color(0xFFFF4081)
        "Ultra Violet", "Ultra" -> Color(0xFFB388FF)
        else -> Color(0xFFE50914) // Default signature Red matching design
    }
}

fun getAccentGlow(name: String): Color {
    return when (name) {
        "Apple Red", "Red", "Crimson" -> Color(0x33E50914)
        "Cyber Gold", "Cyber" -> Color(0x33FFD159)
        "Electric Cyan", "Electric" -> Color(0x3300E5FF)
        "Neon Rose", "Neon" -> Color(0x33FF4081)
        "Ultra Violet", "Ultra" -> Color(0x33B388FF)
        else -> Color(0x33E50914)
    }
}

/**
 * Calculates standard WCAG Relative Luminance for dynamic contrast protection.
 * L = 0.2126 * R + 0.7152 * G + 0.0722 * B
 */
fun calculateRelativeLuminance(color: Color): Float {
    return (0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue)
}

/**
 * Returns high-contrast foreground text color based on background luminance,
 * guaranteeing legibility across dynamic light/dark album art extraction.
 */
fun getOptimalContrastTextColor(backgroundColor: Color): Color {
    val luminance = calculateRelativeLuminance(backgroundColor)
    return if (luminance > 0.45f) Color(0xFF0D0F12) else Color(0xFFFFFFFF)
}

fun buildThemePalette(baseName: String, accentName: String): ThemePalette {
    val accent = getAccentColor(accentName)
    val glow = getAccentGlow(accentName)

    return when (baseName) {
        "Pure OLED Black" -> ThemePalette(
            bg = Color(0xFF000000),
            surface = Color(0xFF0A0A0A),
            elevated = Color(0xFF141414),
            pill = Color(0xFF1E1E1E),
            trackBg = Color(0xFF262626),
            border = Color(0x1AFFFFFF),
            borderSubtle = Color(0x0DFFFFFF),
            borderHighlight = Color(0x40FFFFFF),
            accent = accent,
            accentGlow = glow,
            pillBg = glow
        )
        "Studio Slate" -> ThemePalette(
            bg = Color(0xFF10131A),
            surface = Color(0xFF181D26),
            elevated = Color(0xFF212733),
            pill = Color(0xFF2B3240),
            trackBg = Color(0xFF333B4D),
            border = Color(0x18FFFFFF),
            borderSubtle = Color(0x0CFFFFFF),
            borderHighlight = Color(0x38FFFFFF),
            accent = accent,
            accentGlow = glow,
            pillBg = glow
        )
        else -> ThemePalette( // Flagship Pure OLED Black
            bg = Color(0xFF000000),
            surface = Color(0xFF0D0E11),
            elevated = Color(0xFF16181D),
            pill = Color(0xFF1E2128),
            trackBg = Color(0xFF262626),
            border = Color(0x1AFFFFFF),
            borderSubtle = Color(0x0DFFFFFF),
            borderHighlight = Color(0x33FFFFFF),
            accent = accent,
            accentGlow = glow,
            pillBg = glow
        )
    }
}

// Global Static Token Defaults for direct references
var ObsidianBg = Color(0xFF000000)
var ObsidianSurface = Color(0xFF0D0E11)
var ObsidianElevated = Color(0xFF16181D)
var ObsidianPill = Color(0xFF1E2128)
var ObsidianTrackBg = Color(0xFF2B313D)

val ObsidianBorder = Color(0x1AFFFFFF)
val ObsidianBorderSubtle = Color(0x0DFFFFFF)
val ObsidianBorderHighlight = Color(0x33FFFFFF)

var MintAccent = Color(0xFFE50914)
var MintAccentLight = Color(0xFFFF4D5E)
var MintAccentDark = Color(0xFFB80710)
var MintGlow = Color(0x33E50914)
var MintPillBg = Color(0x2EE50914)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF9299A8)
val TextMuted = Color(0xFF5C6370)
val TextDark = Color(0xFF383D48)

// Standardized Radii
val RadiusSm = RoundedCornerShape(8.dp)
val RadiusMd = RoundedCornerShape(14.dp)
val RadiusLg = RoundedCornerShape(18.dp)
val RadiusXl = RoundedCornerShape(24.dp)
val RadiusFull = RoundedCornerShape(9999.dp)

// Legacy Aliases
val NeoBlack = ObsidianBg
val NeoWhite = TextPrimary
val NeoBg = ObsidianSurface
val NeoBgDark = ObsidianBg
val NeoSurface = ObsidianSurface
val NeoSurfaceElevated = ObsidianElevated
val NeoYellow = MintAccent
val NeoPurple = Color(0xFFA388EE)
val NeoGreen = MintAccent
val NeoPink = Color(0xFFFF6B6B)
val NeoBlue = Color(0xFF5694FF)
val NeoOrange = Color(0xFFFF8400)
val NeoMuted = TextSecondary

val NeoBorderWidth = 1.dp
val NeoBorderThick = 1.dp
val NeoShadowOffset = 0.dp
val NeoShadowOffsetSmall = 0.dp

val NeoRadiusSm = RadiusSm
val NeoRadiusMd = RadiusMd
val NeoRadiusLg = RadiusLg
val NeoRadiusXl = RadiusXl
val NeoRadiusFull = RadiusFull

val AmberCanvas = ObsidianBg
val AmberSurface = ObsidianSurface
val AmberSurfaceElevated = ObsidianElevated
val AmberSurfaceCard = ObsidianSurface
val AmberButtonFg = MintAccent
val AmberHairline = ObsidianBorder
val AmberHairlineSoft = ObsidianBorderSubtle
val AmberHairlineStrong = ObsidianBorderHighlight
val AmberGold = MintAccent
val AmberGoldGlow = MintGlow
val AmberCognac = NeoOrange
val AmberChampagne = ObsidianSurface
val AmberEmerald = MintAccent
val AmberRose = NeoPink
val AmberPrimaryWhite = TextPrimary
val AmberPrimaryPressed = MintAccentLight
val AmberOnPrimary = ObsidianBg
val AmberInk = TextPrimary
val AmberBody = ObsidianSurface
val AmberCharcoal = TextSecondary
val AmberMute = TextMuted
val AmberAsh = ObsidianBg

val AmberRadiusXs = RadiusSm
val AmberRadiusSm = RadiusSm
val AmberRadiusMd = RadiusMd
val AmberRadiusLg = RadiusLg
val AmberRadiusXl = RadiusXl
val AmberRadiusFull = RadiusFull

val RaycastCanvas = ObsidianBg
val RaycastSurface = ObsidianSurface
val RaycastSurfaceElevated = ObsidianElevated
val RaycastBorder = ObsidianBorder
val RaycastBorderSubtle = ObsidianBorderSubtle
val RaycastBorderHighlight = ObsidianBorderHighlight
val RaycastAccentRed = MintAccent
val RaycastAccentRedLight = MintAccentLight
val RaycastAccentGlow = MintGlow
val RaycastAccentBlue = MintAccent
val RaycastTextPrimary = TextPrimary
val RaycastTextSecondary = TextSecondary
val RaycastTextMuted = TextMuted
val RaycastTrackBg = ObsidianTrackBg
val RaycastInk = TextPrimary
val RaycastMute = TextMuted
val RaycastBody = ObsidianSurface
val RaycastAsh = ObsidianBg
val RaycastPrimaryWhite = TextPrimary

val RaycastRadiusSm = RadiusSm
val RaycastRadiusMd = RadiusMd
val RaycastRadiusLg = RadiusLg
val RaycastRadiusXl = RadiusXl
val RaycastRadiusFull = RadiusFull

// Aether Aliases
val AetherCanvas = ObsidianBg
val AetherSurface = ObsidianSurface
val AetherCyan = MintAccent
val AetherCyanGlow = MintGlow
val AetherMute = TextMuted
val AetherInk = TextPrimary
val AetherPrimaryWhite = TextPrimary
val AetherPrimaryPressed = MintAccentLight
val AetherOnPrimary = ObsidianBg
val AetherSurfaceCard = ObsidianSurface
val AetherSurfaceElevated = ObsidianElevated
val AetherHairline = ObsidianBorder
val AetherHairlineStrong = ObsidianBorderHighlight
val AetherHairlineSoft = ObsidianBorderSubtle
val AetherGold = MintAccent
val AetherGoldGlow = MintGlow
val AetherRose = Color(0xFFFF4081)
val AetherViolet = Color(0xFF7C4DFF)
val AetherKeycapGradient = Brush.verticalGradient(listOf(ObsidianElevated, ObsidianSurface))

val AetherRadiusXs = RadiusSm
val AetherRadiusSm = RadiusSm
val AetherRadiusMd = RadiusMd
val AetherRadiusLg = RadiusLg
val AetherRadiusXl = RadiusXl
val AetherRadiusFull = RadiusFull

@Composable
fun AppTheme(
    baseTheme: String = "Obsidian Dark",
    accentColor: String = "Mint Green",
    content: @Composable () -> Unit
) {
    val palette = buildThemePalette(baseTheme, accentColor)
    ObsidianBg = palette.bg
    ObsidianSurface = palette.surface
    ObsidianElevated = palette.elevated
    ObsidianPill = palette.pill
    ObsidianTrackBg = palette.trackBg
    MintAccent = palette.accent
    MintGlow = palette.accentGlow
    MintPillBg = palette.pillBg

    val colorScheme = darkColorScheme(
        primary = palette.accent,
        background = palette.bg,
        surface = palette.surface,
        onPrimary = palette.bg,
        onBackground = palette.textPrimary,
        onSurface = palette.textPrimary
    )

    CompositionLocalProvider(LocalThemePalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
