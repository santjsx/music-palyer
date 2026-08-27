package com.ipodmodern.audio.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// MARK: - Modern Obsidian Palette
val ModernBgDark = Color(0xFF07080A)
val ModernCardDark = Color(0xFF101217)
val ModernCardHighlight = Color(0xFF181B23)
val ModernCardElevated = Color(0xFF1C202A)

// Glass Borders & Hairlines
val ModernGlassBorder = Color(0x22FFFFFF)
val ModernGlassBorderLight = Color(0x44FFFFFF)
val ModernGlassBorderActive = Color(0x883B82F6)

// Accent Colors
val ModernAccentBlue = Color(0xFF3B82F6)
val ModernAccentCyan = Color(0xFF06B6D4)
val ModernAccentPurple = Color(0xFF8B5CF6)
val ModernAccentRose = Color(0xFFF43F5E)
val ModernAccentGold = Color(0xFFF59E0B)
val ModernAccentEmerald = Color(0xFF10B981)

// Text Colors
val ModernTextPrimary = Color(0xFFF8FAFC)
val ModernTextSecondary = Color(0xFF94A3B8)
val ModernTextMuted = Color(0xFF64748B)

// Gradients
val ModernHeroGradient = Brush.horizontalGradient(
    listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
)
val ModernAccentGradient = Brush.linearGradient(
    listOf(Color(0xFF2563EB), Color(0xFF06B6D4))
)
val ModernGoldGradient = Brush.linearGradient(
    listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
)

data class ModernColors(
    val background: Color = ModernBgDark,
    val cardBackground: Color = ModernCardDark,
    val cardHighlight: Color = ModernCardHighlight,
    val cardElevated: Color = ModernCardElevated,
    val glassBorder: Color = ModernGlassBorder,
    val glassBorderLight: Color = ModernGlassBorderLight,
    val accentPrimary: Color = ModernAccentBlue,
    val textPrimary: Color = ModernTextPrimary,
    val textSecondary: Color = ModernTextSecondary,
    val textMuted: Color = ModernTextMuted
)

val LocalModernColors = staticCompositionLocalOf { ModernColors() }

val ModernTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
        color = ModernTextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp,
        color = ModernTextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
        color = ModernTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = ModernTextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = ModernTextSecondary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        color = ModernTextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp,
        color = ModernTextMuted
    )
)

@Composable
fun ModernAppTheme(
    content: @Composable () -> Unit
) {
    val colors = remember { ModernColors() }
    val colorScheme = darkColorScheme(
        primary = ModernAccentBlue,
        secondary = ModernAccentCyan,
        background = ModernBgDark,
        surface = ModernCardDark,
        onPrimary = Color.White,
        onBackground = ModernTextPrimary,
        onSurface = ModernTextPrimary
    )

    CompositionLocalProvider(
        LocalModernColors provides colors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ModernTypography,
            content = content
        )
    }
}
