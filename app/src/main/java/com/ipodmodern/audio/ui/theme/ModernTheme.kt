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

// MARK: - Aether Luxury Obsidian & Stellar Void Canvas
val AetherCanvas = Color(0xFF050608)           // Deepest stellar black canvas
val AetherSurface = Color(0xFF0C0E14)          // Glassmorphic surface panels
val AetherSurfaceElevated = Color(0xFF131722)  // Interactive inputs, pill-tabs, elevated cards
val AetherSurfaceCard = Color(0xFF181D2B)      // Active tiles, keycaps, hero containers
val AetherButtonFg = Color(0xFF1E2333)         // Secondary button interior

// MARK: - Aether Specular Hairlines & Glass Borders
val AetherHairline = Color(0xFF1E2536)         // Subtle precision border (1px)
val AetherHairlineSoft = Color(0x1AFFFFFF)     // Translucent specular highlight
val AetherHairlineStrong = Color(0xFF323D57)   // Active focus/selected state border

// MARK: - Aether Luminous Brand Accents
val AetherCyan = Color(0xFF00E5FF)             // Luminous Ethereal Cyan (Hi-Res Lossless)
val AetherCyanGlow = Color(0x3300E5FF)         // Ambient Cyan Aura
val AetherViolet = Color(0xFF8B5CF6)           // Cosmic Lossless Violet
val AetherVioletGlow = Color(0x338B5CF6)       // Ambient Violet Aura
val AetherEmerald = Color(0xFF10B981)          // Studio Master Green
val AetherAmber = Color(0xFFF59E0B)            // Valve Warmth Amber
val AetherRose = Color(0xFFF43F5E)             // Favorite Heart Rose

// MARK: - Aether Primary Action (Solid White Hero Pill)
val AetherPrimaryWhite = Color(0xFFFFFFFF)     // High-contrast primary CTA
val AetherPrimaryPressed = Color(0xFFE2E8F0)   // Pressed feedback
val AetherOnPrimary = Color(0xFF000000)        // Pure obsidian glyphs on white

// MARK: - Aether Typography Ladder
val AetherInk = Color(0xFFF8FAFC)              // Pure diamond white headlines
val AetherBody = Color(0xFFCBD5E1)             // Primary body text
val AetherCharcoal = Color(0xFF94A3B8)         // Secondary body / metadata
val AetherMute = Color(0xFF64748B)             // Captions, format badges, labels
val AetherAsh = Color(0xFF475569)              // Disabled & subtle markers

// MARK: - Aether Gradients
val AetherAuroraGradient = Brush.linearGradient(
    listOf(AetherCyan, AetherViolet)
)
val AetherCardGradient = Brush.verticalGradient(
    listOf(Color(0xFF131722), Color(0xFF0C0E14))
)
val AetherKeycapGradient = Brush.verticalGradient(
    listOf(Color(0xFF1C2233), Color(0xFF10141F))
)

// Legacy Aliases for backwards compatibility across existing components
val RaycastCanvas = AetherCanvas
val RaycastSurface = AetherSurface
val RaycastSurfaceElevated = AetherSurfaceElevated
val RaycastSurfaceCard = AetherSurfaceCard
val RaycastButtonFg = AetherButtonFg
val RaycastHairline = AetherHairline
val RaycastHairlineSoft = AetherHairlineSoft
val RaycastHairlineStrong = AetherHairlineStrong
val RaycastPrimaryWhite = AetherPrimaryWhite
val RaycastPrimaryPressed = AetherPrimaryPressed
val RaycastOnPrimary = AetherOnPrimary
val RaycastInk = AetherInk
val RaycastBody = AetherBody
val RaycastCharcoal = AetherCharcoal
val RaycastMute = AetherMute
val RaycastAsh = AetherAsh
val RaycastStone = Color(0xFF334155)
val RaycastAccentBlue = AetherCyan
val RaycastAccentBlueSoft = AetherCyanGlow
val RaycastAccentGreen = AetherEmerald
val RaycastAccentGreenSoft = Color(0x2610B981)
val RaycastAccentYellow = AetherAmber
val RaycastAccentYellowSoft = Color(0x26F59E0B)
val RaycastAccentRed = AetherRose
val RaycastAccentRedSoft = Color(0x26F43F5E)
val RaycastKeycapGradient = AetherKeycapGradient

val ModernBgDark = AetherCanvas
val ModernSurfaceDark = AetherSurface
val ModernCardDark = AetherSurfaceCard
val ModernTextPrimary = AetherInk
val ModernTextSecondary = AetherBody
val ModernTextMuted = AetherMute
val ModernAccentBlue = AetherCyan
val ModernAccentCyan = AetherCyan
val ModernAccentPurple = AetherViolet
val ModernAccentEmerald = AetherEmerald
val ModernAccentGold = AetherAmber
val ModernHeroGradient = AetherAuroraGradient

// MARK: - Aether Corner Radii
val RaycastRadiusXs = RoundedCornerShape(4.dp)
val RaycastRadiusSm = RoundedCornerShape(8.dp)
val RaycastRadiusMd = RoundedCornerShape(12.dp)
val RaycastRadiusLg = RoundedCornerShape(16.dp)
val RaycastRadiusXl = RoundedCornerShape(20.dp)
val RaycastRadiusFull = RoundedCornerShape(9999.dp)

val AetherRadiusXs = RaycastRadiusXs
val AetherRadiusSm = RaycastRadiusSm
val AetherRadiusMd = RaycastRadiusMd
val AetherRadiusLg = RaycastRadiusLg
val AetherRadiusXl = RaycastRadiusXl
val AetherRadiusFull = RaycastRadiusFull

@Immutable
data class AetherColorSystem(
    val canvas: Color = AetherCanvas,
    val surface: Color = AetherSurface,
    val surfaceElevated: Color = AetherSurfaceElevated,
    val surfaceCard: Color = AetherSurfaceCard,
    val hairline: Color = AetherHairline,
    val hairlineStrong: Color = AetherHairlineStrong,
    val primary: Color = AetherPrimaryWhite,
    val onPrimary: Color = AetherOnPrimary,
    val cyan: Color = AetherCyan,
    val violet: Color = AetherViolet,
    val ink: Color = AetherInk,
    val body: Color = AetherBody,
    val mute: Color = AetherMute,
    val ash: Color = AetherAsh
)

val LocalAetherColors = staticCompositionLocalOf { AetherColorSystem() }
val LocalRaycastColors = LocalAetherColors

@Composable
fun ModernAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = AetherPrimaryWhite,
        onPrimary = AetherOnPrimary,
        background = AetherCanvas,
        onBackground = AetherInk,
        surface = AetherSurface,
        onSurface = AetherInk,
        surfaceVariant = AetherSurfaceElevated,
        onSurfaceVariant = AetherBody,
        outline = AetherHairline
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
