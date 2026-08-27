package com.ipodmodern.audio.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// MARK: - Obsidian & Mint Modern Dark Theme System
val ObsidianBg = Color(0xFF0D0F12)              // Deep obsidian background
val ObsidianSurface = Color(0xFF16191E)         // Surface card fill
val ObsidianElevated = Color(0xFF1E2229)        // Elevated pill & popup fill
val ObsidianPill = Color(0xFF242932)            // Pill button / Search bar background
val ObsidianTrackBg = Color(0xFF2B313D)         // Inactive seekbar & fader background

val ObsidianBorder = Color(0x14FFFFFF)          // 1px Hairline border (8% white)
val ObsidianBorderSubtle = Color(0x0AFFFFFF)    // Faint subtle border (4% white)
val ObsidianBorderHighlight = Color(0x33FFFFFF) // Active border (20% white)

// MARK: - Mint Green Accent Palette
val MintAccent = Color(0xFF7AE898)              // Core vibrant Mint Accent
val MintAccentLight = Color(0xFFA3F2B8)         // Soft Mint
val MintAccentDark = Color(0xFF3EA35E)          // Deep Mint
val MintGlow = Color(0x337AE898)                // 20% Alpha Glow for Waveforms & Knobs
val MintPillBg = Color(0x247AE898)              // Semi-transparent mint badge background

// MARK: - Typography Colors
val TextPrimary = Color(0xFFFFFFFF)             // High-contrast primary title
val TextSecondary = Color(0xFF9299A8)           // Subtitles & artist names
val TextMuted = Color(0xFF5C6370)               // Duration, metadata, captions
val TextDark = Color(0xFF383D48)                // Placeholder text

// MARK: - Standardized Radii
val RadiusSm = RoundedCornerShape(8.dp)
val RadiusMd = RoundedCornerShape(14.dp)
val RadiusLg = RoundedCornerShape(18.dp)
val RadiusXl = RoundedCornerShape(24.dp)
val RadiusFull = RoundedCornerShape(9999.dp)

// Legacy Aliases for Seamless Codebase Compatibility
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
val RaycastSurfaceCard = ObsidianSurface
val RaycastButtonFg = MintAccent
val RaycastHairline = ObsidianBorder
val RaycastHairlineSoft = ObsidianBorderSubtle
val RaycastHairlineStrong = ObsidianBorderHighlight
val RaycastPrimaryWhite = TextPrimary
val RaycastPrimaryPressed = MintAccentLight
val RaycastOnPrimary = ObsidianBg
val RaycastInk = TextPrimary
val RaycastBody = ObsidianSurface
val RaycastCharcoal = TextSecondary
val RaycastMute = TextMuted
val RaycastAsh = ObsidianBg
val RaycastStone = TextSecondary
val RaycastAccentBlue = NeoBlue
val RaycastAccentBlueSoft = NeoBlue.copy(alpha = 0.25f)
val RaycastAccentGreen = MintAccent
val RaycastAccentGreenSoft = MintGlow
val RaycastAccentYellow = MintAccent
val RaycastAccentYellowSoft = MintGlow
val RaycastAccentRed = NeoPink
val RaycastAccentRedSoft = NeoPink.copy(alpha = 0.25f)
val RaycastKeycapGradient = Brush.verticalGradient(listOf(MintAccent, MintAccentDark))

val AetherCanvas = ObsidianBg
val AetherSurface = ObsidianSurface
val AetherSurfaceElevated = ObsidianElevated
val AetherSurfaceCard = ObsidianSurface
val AetherButtonFg = MintAccent
val AetherHairline = ObsidianBorder
val AetherHairlineSoft = ObsidianBorderSubtle
val AetherHairlineStrong = ObsidianBorderHighlight
val AetherPrimaryWhite = TextPrimary
val AetherPrimaryPressed = MintAccentLight
val AetherOnPrimary = ObsidianBg
val AetherInk = TextPrimary
val AetherBody = ObsidianSurface
val AetherCharcoal = TextSecondary
val AetherMute = TextMuted
val AetherAsh = ObsidianBg
val AetherCyan = MintAccent
val AetherCyanGlow = MintGlow
val AetherViolet = NeoPurple
val AetherAmber = MintAccent
val AetherEmerald = MintAccent
val AetherRose = NeoPink
val AetherKeycapGradient = RaycastKeycapGradient

val AetherRadiusXs = RadiusSm
val AetherRadiusSm = RadiusSm
val AetherRadiusMd = RadiusMd
val AetherRadiusLg = RadiusLg
val AetherRadiusXl = RadiusXl
val AetherRadiusFull = RadiusFull

val RaycastRadiusXs = RadiusSm
val RaycastRadiusSm = RadiusSm
val RaycastRadiusMd = RadiusMd
val RaycastRadiusLg = RadiusLg
val RaycastRadiusXl = RadiusXl
val RaycastRadiusFull = RadiusFull

val ModernBgDark = ObsidianBg
val ModernSurfaceDark = ObsidianSurface
val ModernCardDark = ObsidianSurface
val ModernTextPrimary = TextPrimary
val ModernTextSecondary = TextSecondary
val ModernTextMuted = TextMuted
val ModernAccentBlue = NeoBlue
val ModernAccentCyan = MintAccent
val ModernAccentPurple = NeoPurple
val ModernAccentEmerald = MintAccent
val ModernAccentGold = MintAccent
val ModernHeroGradient = Brush.linearGradient(listOf(MintAccent, MintAccentDark))

@Immutable
data class ModernColorSystem(
    val bg: Color = ObsidianBg,
    val surface: Color = ObsidianSurface,
    val elevated: Color = ObsidianElevated,
    val pill: Color = ObsidianPill,
    val border: Color = ObsidianBorder,
    val mint: Color = MintAccent,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val textMuted: Color = TextMuted
)

val LocalModernColors = staticCompositionLocalOf { ModernColorSystem() }

@Composable
fun ModernAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = MintAccent,
        onPrimary = ObsidianBg,
        background = ObsidianBg,
        onBackground = TextPrimary,
        surface = ObsidianSurface,
        onSurface = TextPrimary,
        surfaceVariant = ObsidianElevated,
        onSurfaceVariant = TextSecondary,
        outline = ObsidianBorder
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
