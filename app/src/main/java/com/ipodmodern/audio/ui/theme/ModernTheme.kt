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
    val bg: Color = Color(0xFF080E0A), // Near-black green primary canvas
    val surface: Color = Color(0xFF121914), // Dark olive cards and panels
    val elevated: Color = Color(0xFF1A261E), // Lighter olive floating controls
    val surfaceElevated: Color = Color(0xFF1A261E),
    val pill: Color = Color(0xFF1C2720),
    val trackBg: Color = Color(0xFF141D17),
    val border: Color = Color(0x1F22E559),
    val borderSubtle: Color = Color(0x14FFFFFF),
    val borderHighlight: Color = Color(0x3322E559),
    val accent: Color = Color(0xFF22E559), // Electric Lime
    val accentLight: Color = Color(0xFF45F075),
    val accentDark: Color = Color(0xFF16A34A),
    val accentGlow: Color = Color(0x3322E559),
    val pillBg: Color = Color(0x2E22E559),
    val textPrimary: Color = Color(0xFFF3F5F4),
    val textSecondary: Color = Color(0xFF9AA59D),
    val textMuted: Color = Color(0xFF5B6B60),
    val textDark: Color = Color(0xFF2A362E)
)

val LocalThemePalette = staticCompositionLocalOf { ThemePalette() }

object ModernThemeTokens {
    val palette: ThemePalette
        @Composable
        get() = LocalThemePalette.current
}

fun getAccentColor(name: String): Color {
    return when (name) {
        "Electric Lime", "Lime", "Neon Green", "Green" -> Color(0xFF22E559)
        "Cyber Gold", "Cyber", "Gold" -> Color(0xFFFFD159)
        "Electric Cyan", "Electric", "Cyan" -> Color(0xFF00E5FF)
        "Neon Rose", "Neon", "Rose" -> Color(0xFFFF4081)
        "Ultra Violet", "Ultra", "Violet" -> Color(0xFFB388FF)
        "Apple Red", "Red", "Crimson" -> Color(0xFFE50914)
        else -> Color(0xFF22E559) // Flagship Electric Lime
    }
}

fun getAccentGlow(name: String): Color {
    return when (name) {
        "Electric Lime", "Lime", "Neon Green", "Green" -> Color(0x3322E559)
        "Cyber Gold", "Cyber", "Gold" -> Color(0x33FFD159)
        "Electric Cyan", "Electric", "Cyan" -> Color(0x3300E5FF)
        "Neon Rose", "Neon", "Rose" -> Color(0x33FF4081)
        "Ultra Violet", "Ultra", "Violet" -> Color(0x33B388FF)
        "Apple Red", "Red", "Crimson" -> Color(0x33E50914)
        else -> Color(0x3322E559)
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
    return if (luminance > 0.45f) Color(0xFF080E0A) else Color(0xFFF3F5F4)
}

fun buildThemePalette(baseName: String, accentName: String): ThemePalette {
    val accent = getAccentColor(accentName)
    val glow = getAccentGlow(accentName)

    return when (baseName) {
        "Pure OLED Black" -> ThemePalette(
            bg = Color(0xFF000000),
            surface = Color(0xFF0A0D0B),
            elevated = Color(0xFF131A15),
            surfaceElevated = Color(0xFF131A15),
            pill = Color(0xFF19241C),
            trackBg = Color(0xFF141D17),
            border = Color(0x1F22E559),
            borderSubtle = Color(0x0DFFFFFF),
            borderHighlight = Color(0x4022E559),
            accent = accent,
            accentGlow = glow,
            pillBg = Color(0x2E22E559)
        )
        "Studio Slate" -> ThemePalette(
            bg = Color(0xFF0D1410),
            surface = Color(0xFF141F18),
            elevated = Color(0xFF1D2C22),
            surfaceElevated = Color(0xFF1D2C22),
            pill = Color(0xFF24362B),
            trackBg = Color(0xFF1D2C22),
            border = Color(0x2222E559),
            borderSubtle = Color(0x0CFFFFFF),
            borderHighlight = Color(0x3822E559),
            accent = accent,
            accentGlow = glow,
            pillBg = Color(0x2E22E559)
        )
        else -> ThemePalette( // Flagship Ambient Dark Near-Black Green (PRD 6.1)
            bg = Color(0xFF080E0A),
            surface = Color(0xFF121914),
            elevated = Color(0xFF1A261E),
            surfaceElevated = Color(0xFF1A261E),
            pill = Color(0xFF1C2720),
            trackBg = Color(0xFF141D17),
            border = Color(0x1F22E559),
            borderSubtle = Color(0x14FFFFFF),
            borderHighlight = Color(0x3322E559),
            accent = accent,
            accentGlow = glow,
            pillBg = Color(0x2E22E559)
        )
    }
}

// Global Static Token Defaults for direct references (PRD 6.1)
var ObsidianBg = Color(0xFF080E0A)
var ObsidianSurface = Color(0xFF121914)
var ObsidianElevated = Color(0xFF1A261E)
var ObsidianPill = Color(0xFF1C2720)
var ObsidianTrackBg = Color(0xFF141D17)

val ObsidianBorder = Color(0x1F22E559)
val ObsidianBorderSubtle = Color(0x14FFFFFF)
val ObsidianBorderHighlight = Color(0x3322E559)

var MintAccent = Color(0xFF22E559) // Flagship Electric Lime
var MintAccentLight = Color(0xFF45F075)
var MintAccentDark = Color(0xFF16A34A)
var MintGlow = Color(0x3322E559)
var MintPillBg = Color(0x2E22E559)

val TextPrimary = Color(0xFFF3F5F4)
val TextSecondary = Color(0xFF9AA59D)
val TextMuted = Color(0xFF5B6B60)
val TextDark = Color(0xFF2A362E)

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
    accentColor: String = "Electric Lime",
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
