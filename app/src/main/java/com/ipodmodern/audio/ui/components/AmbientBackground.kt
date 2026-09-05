package com.ipodmodern.audio.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.theme.LocalThemePalette
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusMd

/**
 * AmbientBackground provides the flagship TuneHive dark-ambient atmosphere:
 * - Near-black green primary canvas (#080E0A)
 * - Dynamic radial ambient bloom influenced by artwork dominant color or Electric Lime
 * - Smooth color interpolation during track transitions
 */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    ambientColor: Color? = null,
    ambientAlpha: Float = 0.22f,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalThemePalette.current
    val targetGlowColor = ambientColor ?: palette.accent

    val animatedGlowColor by animateColorAsState(
        targetValue = targetGlowColor,
        animationSpec = tween(durationMillis = 800),
        label = "ambient_glow_anim"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg)
    ) {
        // Soft atmospheric radial glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Primary top-center ambient bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedGlowColor.copy(alpha = ambientAlpha),
                        animatedGlowColor.copy(alpha = ambientAlpha * 0.45f),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.5f, canvasH * 0.28f),
                    radius = canvasW * 0.85f
                )
            )

            // Secondary subtle bottom bloom
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedGlowColor.copy(alpha = ambientAlpha * 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(canvasW * 0.8f, canvasH * 0.85f),
                    radius = canvasW * 0.65f
                )
            )
        }

        content()
    }
}

/**
 * GlowSurface provides a modern card container with a subtle ambient glow border
 * and refined dark olive backdrop.
 */
@Composable
fun GlowSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RadiusLg,
    backgroundColor: Color? = null,
    glowColor: Color? = null,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val palette = LocalThemePalette.current
    val bg = backgroundColor ?: palette.surface
    val glow = glowColor ?: palette.border
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 1600f),
        label = "glow_surface_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(bg)
            .border(borderWidth, glow, shape)
            .then(
                if (onClick != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = { onClick() }
                        )
                    }
                } else Modifier
            )
    ) {
        content()
    }
}

/**
 * GlassSurface provides a frosted translucent dark olive panel for floating navigation,
 * mini player, or overlays.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RadiusFull,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable () -> Unit
) {
    val palette = LocalThemePalette.current
    val bg = backgroundColor ?: palette.surface.copy(alpha = 0.88f)
    val border = borderColor ?: palette.borderSubtle

    Box(
        modifier = modifier
            .shadow(16.dp, shape, spotColor = Color(0x66000000))
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape)
    ) {
        content()
    }
}

/**
 * NeonAccentPill provides an interactive tactile button with electric lime fill or outline.
 */
@Composable
fun NeonAccentPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 1800f),
        label = "pill_scale"
    )

    val bg = if (isSelected) palette.accent else palette.surfaceElevated
    val fg = if (isSelected) palette.bg else palette.textPrimary
    val border = if (isSelected) palette.accentLight else palette.borderSubtle

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RadiusFull)
            .background(bg)
            .border(1.dp, border, RadiusFull)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fg,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
