package com.ipodmodern.audio.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// MARK: - Neo-Brutalism Color System (Inspired by neo-brutalism-ui-library)
val NeoBlack = Color(0xFF000000)               // Core Solid Black for 3px borders, shadows, text
val NeoWhite = Color(0xFFFFFFFF)               // Crisp High-Contrast White
val NeoBg = Color(0xFFF4F0EA)                  // Warm Cream Canvas / Paper substrate
val NeoBgDark = Color(0xFF141416)              // Neo-Brutalist Deep Void Alternative
val NeoSurface = Color(0xFFFFFFFF)             // Card fill
val NeoSurfaceElevated = Color(0xFFFFF9E6)     // Light yellow tint surface

// MARK: - Neo-Brutalist Pop Palette
val NeoYellow = Color(0xFFFFE600)              // Electric Yellow (Hero CTA)
val NeoPurple = Color(0xFFA388EE)              // Neo Violet / Lavender
val NeoGreen = Color(0xFF23A094)               // Retro Mint / Emerald
val NeoPink = Color(0xFFFF6B6B)                // Hot Coral Pink
val NeoBlue = Color(0xFF5694FF)                // Electric Sky Blue
val NeoOrange = Color(0xFFFF8400)              // Vibrant Tangerine
val NeoMuted = Color(0xFF71717A)               // Neutral secondary text

// MARK: - Standardized Neo-Brutalism Geometry
val NeoBorderWidth = 2.5.dp
val NeoBorderThick = 3.dp
val NeoShadowOffset = 4.dp
val NeoShadowOffsetSmall = 3.dp

val NeoRadiusSm = RoundedCornerShape(8.dp)
val NeoRadiusMd = RoundedCornerShape(12.dp)
val NeoRadiusLg = RoundedCornerShape(16.dp)
val NeoRadiusXl = RoundedCornerShape(22.dp)
val NeoRadiusFull = RoundedCornerShape(9999.dp)

// Legacy Aliases for Seamless Codebase Compatibility
val AmberCanvas = NeoBgDark
val AmberSurface = NeoBlack
val AmberSurfaceElevated = NeoSurface
val AmberSurfaceCard = NeoSurface
val AmberButtonFg = NeoYellow
val AmberHairline = NeoBlack
val AmberHairlineSoft = NeoBlack
val AmberHairlineStrong = NeoBlack
val AmberGold = NeoYellow
val AmberGoldGlow = NeoYellow.copy(alpha = 0.35f)
val AmberCognac = NeoOrange
val AmberChampagne = NeoBg
val AmberEmerald = NeoGreen
val AmberRose = NeoPink
val AmberPrimaryWhite = NeoWhite
val AmberPrimaryPressed = NeoYellow
val AmberOnPrimary = NeoBlack
val AmberInk = NeoWhite
val AmberBody = NeoBg
val AmberCharcoal = NeoMuted
val AmberMute = NeoMuted
val AmberAsh = NeoBlack

val AmberRadiusXs = NeoRadiusSm
val AmberRadiusSm = NeoRadiusSm
val AmberRadiusMd = NeoRadiusMd
val AmberRadiusLg = NeoRadiusLg
val AmberRadiusXl = NeoRadiusXl
val AmberRadiusFull = NeoRadiusFull

val RaycastCanvas = NeoBgDark
val RaycastSurface = NeoSurface
val RaycastSurfaceElevated = NeoSurfaceElevated
val RaycastSurfaceCard = NeoSurface
val RaycastButtonFg = NeoYellow
val RaycastHairline = NeoBlack
val RaycastHairlineSoft = NeoBlack
val RaycastHairlineStrong = NeoBlack
val RaycastPrimaryWhite = NeoWhite
val RaycastPrimaryPressed = NeoYellow
val RaycastOnPrimary = NeoBlack
val RaycastInk = NeoWhite
val RaycastBody = NeoBg
val RaycastCharcoal = NeoMuted
val RaycastMute = NeoMuted
val RaycastAsh = NeoBlack
val RaycastStone = NeoMuted
val RaycastAccentBlue = NeoBlue
val RaycastAccentBlueSoft = NeoBlue.copy(alpha = 0.25f)
val RaycastAccentGreen = NeoGreen
val RaycastAccentGreenSoft = NeoGreen.copy(alpha = 0.25f)
val RaycastAccentYellow = NeoYellow
val RaycastAccentYellowSoft = NeoYellow.copy(alpha = 0.25f)
val RaycastAccentRed = NeoPink
val RaycastAccentRedSoft = NeoPink.copy(alpha = 0.25f)
val RaycastKeycapGradient = Brush.verticalGradient(listOf(NeoYellow, NeoOrange))

val AetherCanvas = NeoBgDark
val AetherSurface = NeoSurface
val AetherSurfaceElevated = NeoSurfaceElevated
val AetherSurfaceCard = NeoSurface
val AetherButtonFg = NeoYellow
val AetherHairline = NeoBlack
val AetherHairlineSoft = NeoBlack
val AetherHairlineStrong = NeoBlack
val AetherPrimaryWhite = NeoWhite
val AetherPrimaryPressed = NeoYellow
val AetherOnPrimary = NeoBlack
val AetherInk = NeoWhite
val AetherBody = NeoBg
val AetherCharcoal = NeoMuted
val AetherMute = NeoMuted
val AetherAsh = NeoBlack
val AetherCyan = NeoYellow
val AetherCyanGlow = NeoYellow.copy(alpha = 0.35f)
val AetherViolet = NeoPurple
val AetherAmber = NeoYellow
val AetherEmerald = NeoGreen
val AetherRose = NeoPink
val AetherKeycapGradient = RaycastKeycapGradient

val AetherRadiusXs = NeoRadiusSm
val AetherRadiusSm = NeoRadiusSm
val AetherRadiusMd = NeoRadiusMd
val AetherRadiusLg = NeoRadiusLg
val AetherRadiusXl = NeoRadiusXl
val AetherRadiusFull = NeoRadiusFull

val RaycastRadiusXs = NeoRadiusSm
val RaycastRadiusSm = NeoRadiusSm
val RaycastRadiusMd = NeoRadiusMd
val RaycastRadiusLg = NeoRadiusLg
val RaycastRadiusXl = NeoRadiusXl
val RaycastRadiusFull = NeoRadiusFull

val ModernBgDark = NeoBgDark
val ModernSurfaceDark = NeoSurface
val ModernCardDark = NeoSurface
val ModernTextPrimary = NeoWhite
val ModernTextSecondary = NeoBg
val ModernTextMuted = NeoMuted
val ModernAccentBlue = NeoBlue
val ModernAccentCyan = NeoYellow
val ModernAccentPurple = NeoPurple
val ModernAccentEmerald = NeoGreen
val ModernAccentGold = NeoYellow
val ModernHeroGradient = Brush.linearGradient(listOf(NeoYellow, NeoOrange))

@Immutable
data class NeoBrutalismColorSystem(
    val bg: Color = NeoBgDark,
    val surface: Color = NeoSurface,
    val black: Color = NeoBlack,
    val white: Color = NeoWhite,
    val yellow: Color = NeoYellow,
    val purple: Color = NeoPurple,
    val green: Color = NeoGreen,
    val pink: Color = NeoPink,
    val blue: Color = NeoBlue,
    val orange: Color = NeoOrange
)

val LocalNeoColors = staticCompositionLocalOf { NeoBrutalismColorSystem() }

@Composable
fun ModernAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = NeoYellow,
        onPrimary = NeoBlack,
        background = NeoBgDark,
        onBackground = NeoWhite,
        surface = NeoBgDark,
        onSurface = NeoWhite,
        surfaceVariant = NeoSurface,
        onSurfaceVariant = NeoBlack,
        outline = NeoBlack
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
