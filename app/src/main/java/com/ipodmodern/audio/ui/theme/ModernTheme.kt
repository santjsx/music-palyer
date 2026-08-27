package com.ipodmodern.audio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// MARK: - Raycast Surface Ladder (Dark-Only System)
val RaycastCanvas = Color(0xFF07080A)           // Pure near-black canvas background
val RaycastSurface = Color(0xFF0D0D0D)          // Cards & elevated panels
val RaycastSurfaceElevated = Color(0xFF101111)  // Inputs, buttons, pill-tabs
val RaycastSurfaceCard = Color(0xFF141517)      // App-icon tiles, keycap backgrounds
val RaycastButtonFg = Color(0xFF18191A)         // In-card deep surface variant

// MARK: - Raycast Hairlines (1px Card Borders, No Drop Shadows)
val RaycastHairline = Color(0xFF242728)         // Universal 1px border
val RaycastHairlineSoft = Color(0x14FFFFFF)     // Translucent overlay border (rgba 0.08)
val RaycastHairlineStrong = Color(0x29FFFFFF)   // Stronger divider (rgba 0.16)

// MARK: - Raycast Primary Brand Action (Universal White CTA)
val RaycastPrimaryWhite = Color(0xFFFFFFFF)     // Universal primary action pill
val RaycastPrimaryPressed = Color(0xFFE8E8E8)   // Pressed state
val RaycastOnPrimary = Color(0xFF000000)        // Pure black on white CTA

// MARK: - Raycast Text Ladder
val RaycastInk = Color(0xFFF4F4F6)              // Primary headline text
val RaycastBody = Color(0xFFCDCDCD)             // Default body text
val RaycastCharcoal = Color(0xFFD3D3D4)         // Brighter body
val RaycastMute = Color(0xFF9C9C9D)             // Metadata, captions, secondary
val RaycastAsh = Color(0xFF6A6B6C)              // Low-emphasis utility & disabled
val RaycastStone = Color(0xFF434345)            // Least-emphasis icon color

// MARK: - Raycast Semantic Accents (Reserved for illustrations & key badges)
val RaycastAccentBlue = Color(0xFF57C1FF)
val RaycastAccentBlueSoft = Color(0x2657C1FF)
val RaycastAccentGreen = Color(0xFF59D499)
val RaycastAccentGreenSoft = Color(0x2659D499)
val RaycastAccentYellow = Color(0xFFFFC533)
val RaycastAccentYellowSoft = Color(0x26FFC533)
val RaycastAccentRed = Color(0xFFFF6161)
val RaycastAccentRedSoft = Color(0x26FF6161)

// MARK: - Raycast Keycap Subtle 3D Physical Gradient
val RaycastKeycapGradient = Brush.verticalGradient(
    listOf(Color(0xFF16181B), Color(0xFF0D0E10))
)

// Legacy theme aliases for backwards compatibility
val ModernBgDark = RaycastCanvas
val ModernSurfaceDark = RaycastSurface
val ModernCardDark = RaycastSurfaceCard
val ModernTextPrimary = RaycastInk
val ModernTextSecondary = RaycastBody
val ModernTextMuted = RaycastMute
val ModernAccentBlue = RaycastAccentBlue
val ModernAccentCyan = RaycastAccentBlue
val ModernAccentPurple = Color(0xFF8B5CF6)
val ModernAccentEmerald = RaycastAccentGreen
val ModernAccentGold = RaycastAccentYellow
val ModernHeroGradient = Brush.linearGradient(listOf(RaycastAccentBlue, Color(0xFF6366F1), Color(0xFF8B5CF6)))

// MARK: - Raycast Rounded Corner Scale
val RaycastRadiusXs = RoundedCornerShape(4.dp)      // Keycaps, badges, tags
val RaycastRadiusSm = RoundedCornerShape(6.dp)      // Rows, micro chips
val RaycastRadiusMd = RoundedCornerShape(8.dp)      // Buttons, search inputs, icon tiles
val RaycastRadiusLg = RoundedCornerShape(12.dp)     // Feature cards, panels
val RaycastRadiusXl = RoundedCornerShape(16.dp)     // Large mockup containers, modals
val RaycastRadiusFull = RoundedCornerShape(9999.dp) // Pill-tab chips, primary CTAs

@Immutable
data class RaycastColorSystem(
    val canvas: Color = RaycastCanvas,
    val surface: Color = RaycastSurface,
    val surfaceElevated: Color = RaycastSurfaceElevated,
    val surfaceCard: Color = RaycastSurfaceCard,
    val hairline: Color = RaycastHairline,
    val hairlineStrong: Color = RaycastHairlineStrong,
    val primary: Color = RaycastPrimaryWhite,
    val onPrimary: Color = RaycastOnPrimary,
    val ink: Color = RaycastInk,
    val body: Color = RaycastBody,
    val mute: Color = RaycastMute,
    val ash: Color = RaycastAsh
)

val LocalRaycastColors = staticCompositionLocalOf { RaycastColorSystem() }

@Composable
fun ModernAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = RaycastPrimaryWhite,
        onPrimary = RaycastOnPrimary,
        background = RaycastCanvas,
        onBackground = RaycastInk,
        surface = RaycastSurface,
        onSurface = RaycastInk,
        surfaceVariant = RaycastSurfaceElevated,
        onSurfaceVariant = RaycastBody,
        outline = RaycastHairline
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
