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

// MARK: - Aether Luxury Amber, Cognac & Warm Obsidian Theme
val AmberCanvas = Color(0xFF0C0906)           // Ultra-deep warm espresso canvas
val AmberSurface = Color(0xFF16100A)          // Dark cognac glassmorphic cards
val AmberSurfaceElevated = Color(0xFF221910)  // Elevated interactive buttons & inputs
val AmberSurfaceCard = Color(0xFF2E2216)      // Active tiles, keycaps, hero containers
val AmberButtonFg = Color(0xFF3B2B1C)         // Secondary button interior

// MARK: - Specular Golden Hairlines & Glass Borders
val AmberHairline = Color(0xFF382717)         // Subtle precision border (1px)
val AmberHairlineSoft = Color(0x29F59E0B)     // Translucent amber highlight
val AmberHairlineStrong = Color(0xFFD97706)   // Active focus/selected state gold border

// MARK: - Luminous Amber & Sunset Accents
val AmberGold = Color(0xFFF59E0B)             // Luminous Sunset Amber
val AmberGoldGlow = Color(0x40F59E0B)         // Radiant Amber Aura
val AmberCognac = Color(0xFFEA580C)           // Rich Deep Cognac
val AmberChampagne = Color(0xFFFEF3C7)        // Champagne highlight text
val AmberEmerald = Color(0xFF10B981)          // Studio Master Green
val AmberRose = Color(0xFFF43F5E)             // Favorite Heart Rose

// MARK: - Primary Action (Solid White Hero Pill with Amber Specular Accent)
val AmberPrimaryWhite = Color(0xFFFFFFFF)     // High-contrast primary CTA
val AmberPrimaryPressed = Color(0xFFFDE68A)   // Warm gold pressed feedback
val AmberOnPrimary = Color(0xFF000000)        // Pure obsidian glyphs on white

// MARK: - Typography Ladder
val AmberInk = Color(0xFFFFFBEB)              // Diamond warm white headlines
val AmberBody = Color(0xFFE2D9CC)             // Primary body text
val AmberCharcoal = Color(0xFFB5A695)         // Secondary body / metadata
val AmberMute = Color(0xFF8C7A68)             // Captions, format badges, labels
val AmberAsh = Color(0xFF5E4E3F)              // Disabled & subtle markers

// MARK: - Ambient Gradients
val AmberAuroraGradient = Brush.linearGradient(
    listOf(AmberGold, AmberCognac)
)
val AmberCardGradient = Brush.verticalGradient(
    listOf(Color(0xFF261C12), Color(0xFF140E08))
)
val AmberKeycapGradient = Brush.verticalGradient(
    listOf(Color(0xFF332314), Color(0xFF1E140B))
)

// Legacy Aliases for backwards compatibility across existing components
val AetherCanvas = AmberCanvas
val AetherSurface = AmberSurface
val AetherSurfaceElevated = AmberSurfaceElevated
val AetherSurfaceCard = AmberSurfaceCard
val AetherButtonFg = AmberButtonFg
val AetherHairline = AmberHairline
val AetherHairlineSoft = AmberHairlineSoft
val AetherHairlineStrong = AmberHairlineStrong
val AetherPrimaryWhite = AmberPrimaryWhite
val AetherPrimaryPressed = AmberPrimaryPressed
val AetherOnPrimary = AmberOnPrimary
val AetherInk = AmberInk
val AetherBody = AmberBody
val AetherCharcoal = AmberCharcoal
val AetherMute = AmberMute
val AetherAsh = AmberAsh
val AetherCyan = AmberGold
val AetherCyanGlow = AmberGoldGlow
val AetherViolet = AmberCognac
val AetherAmber = AmberGold
val AetherEmerald = AmberEmerald
val AetherRose = AmberRose
val AetherKeycapGradient = AmberKeycapGradient

val RaycastCanvas = AmberCanvas
val RaycastSurface = AmberSurface
val RaycastSurfaceElevated = AmberSurfaceElevated
val RaycastSurfaceCard = AmberSurfaceCard
val RaycastButtonFg = AmberButtonFg
val RaycastHairline = AmberHairline
val RaycastHairlineSoft = AmberHairlineSoft
val RaycastHairlineStrong = AmberHairlineStrong
val RaycastPrimaryWhite = AmberPrimaryWhite
val RaycastPrimaryPressed = AmberPrimaryPressed
val RaycastOnPrimary = AmberOnPrimary
val RaycastInk = AmberInk
val RaycastBody = AmberBody
val RaycastCharcoal = AmberCharcoal
val RaycastMute = AmberMute
val RaycastAsh = AmberAsh
val RaycastStone = Color(0xFF4A3C2F)
val RaycastAccentBlue = AmberGold
val RaycastAccentBlueSoft = AmberGoldGlow
val RaycastAccentGreen = AmberEmerald
val RaycastAccentGreenSoft = Color(0x2610B981)
val RaycastAccentYellow = AmberGold
val RaycastAccentYellowSoft = Color(0x26F59E0B)
val RaycastAccentRed = AmberRose
val RaycastAccentRedSoft = Color(0x26F43F5E)
val RaycastKeycapGradient = AmberKeycapGradient

val ModernBgDark = AmberCanvas
val ModernSurfaceDark = AmberSurface
val ModernCardDark = AmberSurfaceCard
val ModernTextPrimary = AmberInk
val ModernTextSecondary = AmberBody
val ModernTextMuted = AmberMute
val ModernAccentBlue = AmberGold
val ModernAccentCyan = AmberGold
val ModernAccentPurple = AmberCognac
val ModernAccentEmerald = AmberEmerald
val ModernAccentGold = AmberGold
val ModernHeroGradient = AmberAuroraGradient

// MARK: - Corner Radii
val AmberRadiusXs = RoundedCornerShape(6.dp)
val AmberRadiusSm = RoundedCornerShape(10.dp)
val AmberRadiusMd = RoundedCornerShape(14.dp)
val AmberRadiusLg = RoundedCornerShape(20.dp)
val AmberRadiusXl = RoundedCornerShape(26.dp)
val AmberRadiusFull = RoundedCornerShape(9999.dp)

val RaycastRadiusXs = AmberRadiusXs
val RaycastRadiusSm = AmberRadiusSm
val RaycastRadiusMd = AmberRadiusMd
val RaycastRadiusLg = AmberRadiusLg
val RaycastRadiusXl = AmberRadiusXl
val RaycastRadiusFull = AmberRadiusFull

val AetherRadiusXs = AmberRadiusXs
val AetherRadiusSm = AmberRadiusSm
val AetherRadiusMd = AmberRadiusMd
val AetherRadiusLg = AmberRadiusLg
val AetherRadiusXl = AmberRadiusXl
val AetherRadiusFull = AmberRadiusFull

@Immutable
data class AmberColorSystem(
    val canvas: Color = AmberCanvas,
    val surface: Color = AmberSurface,
    val surfaceElevated: Color = AmberSurfaceElevated,
    val surfaceCard: Color = AmberSurfaceCard,
    val hairline: Color = AmberHairline,
    val hairlineStrong: Color = AmberHairlineStrong,
    val primary: Color = AmberPrimaryWhite,
    val onPrimary: Color = AmberOnPrimary,
    val gold: Color = AmberGold,
    val cognac: Color = AmberCognac,
    val ink: Color = AmberInk,
    val body: Color = AmberBody,
    val mute: Color = AmberMute,
    val ash: Color = AmberAsh
)

val LocalAmberColors = staticCompositionLocalOf { AmberColorSystem() }
val LocalAetherColors = LocalAmberColors
val LocalRaycastColors = LocalAmberColors

@Composable
fun ModernAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = AmberPrimaryWhite,
        onPrimary = AmberOnPrimary,
        background = AmberCanvas,
        onBackground = AmberInk,
        surface = AmberSurface,
        onSurface = AmberInk,
        surfaceVariant = AmberSurfaceElevated,
        onSurfaceVariant = AmberBody,
        outline = AmberHairline
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
