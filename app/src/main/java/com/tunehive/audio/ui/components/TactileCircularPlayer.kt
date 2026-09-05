package com.tunehive.audio.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tunehive.audio.ui.haptics.Haptic
import com.tunehive.audio.ui.haptics.rememberHaptics
import com.tunehive.audio.ui.theme.DarkOliveElevated
import com.tunehive.audio.ui.theme.DarkOliveSurface
import com.tunehive.audio.ui.theme.DeepForestOutline
import com.tunehive.audio.ui.theme.ElectricLime
import com.tunehive.audio.ui.theme.ElectricLimeGlow

/**
 * Concentric Glowing Tactile Circular Player
 *
 * Implements Section 18 of the TuneHive specification:
 * - Layer 1: Ambient breathing glow pulse behind the player disc
 * - Layer 2: Outer concentric tactile ring with dark-olive finish
 * - Layer 3: Concentric neon rim with subtle highlight
 * - Layer 4: Inner tactile disc with spring-physics touch reaction
 * - Layer 5: High-contrast electric lime transport icon or buffering spinner
 */
@Composable
fun TactileCircularPlayer(
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 76.dp,
    accentColor: Color = ElectricLime,
) {
    val haptics = rememberHaptics()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth press spring scale
    val pressScale = remember { Animatable(1f) }
    LaunchedEffect(isPressed) {
        pressScale.animateTo(
            targetValue = if (isPressed) 0.90f else 1f,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        )
    }

    // Ambient breathing pulse when active
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (isPlaying) 0.65f else 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_glow",
    )

    Box(
        modifier = modifier
            .size(size + 16.dp)
            .drawBehind {
                // Layer 1: Ambient breathing glow
                val glowAlpha = if (isPlaying) pulseGlow else 0.12f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = glowAlpha),
                            accentColor.copy(alpha = glowAlpha * 0.4f),
                            Color.Transparent,
                        ),
                        radius = size.toPx() * 0.72f,
                    ),
                    radius = size.toPx() * 0.72f,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        // Layer 2 & 3: Outer concentric tactile disc with neon rim
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = pressScale.value
                    scaleY = pressScale.value
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            DarkOliveElevated,
                            DarkOliveSurface,
                        ),
                    ),
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (isPlaying) 0.85f else 0.35f),
                            DeepForestOutline,
                        ),
                    ),
                    shape = CircleShape,
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptics.play(if (isPlaying) Haptic.Pause else Haptic.Resume)
                        onPlayPause()
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Layer 4: Inner concentric groove
            Box(
                modifier = Modifier
                    .size(size - 14.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                if (isPlaying) accentColor.copy(alpha = 0.22f) else DarkOliveSurface.copy(alpha = 0.8f),
                                if (isPlaying) accentColor.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.5f),
                            ),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        color = if (isPlaying) accentColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.06f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // Layer 5: State Glyph / Loading Spinner
                if (isLoading) {
                    CircularProgressIndicator(
                        color = accentColor,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(size * 0.42f),
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) accentColor else Color.White,
                        modifier = Modifier.size(size * 0.48f),
                    )
                }
            }
        }
    }
}
