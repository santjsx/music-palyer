package com.ipodmodern.audio.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ipodmodern.audio.ui.theme.LocalThemePalette
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.drawBehind
import com.ipodmodern.audio.ui.viewmodel.PlaybackProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * CircularPlayer implements the 6-layer concentric glowing circular player
 * specified in PRD Section 18:
 * - Layer 1: Ambient glow (zero-recomposition GPU-composited pulse)
 * - Layer 2: Outer dark circular ring
 * - Layer 3: Neon lime progress ring (rotational touch seeking)
 * - Layer 4: Inner subtle glow & bevel
 * - Layer 5: Play/pause tactile button
 * - Layer 6: Crisp play/pause icon
 */
@Composable
fun CircularPlayer(
    isPlaying: Boolean,
    progressFlow: StateFlow<PlaybackProgress>? = null,
    progressFraction: Float = 0f, // Fallback if progressFlow is not supplied
    onPlayPauseClick: () -> Unit,
    onSeekFraction: (Float) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 210.dp,
    buttonSize: Dp = 92.dp
) {
    val palette = LocalThemePalette.current
    val view = LocalView.current
    var isDragging by remember { mutableStateOf(false) }
    var dragAngleFraction by remember { mutableFloatStateOf(0f) }
    var isButtonPressed by remember { mutableStateOf(false) }

    // Pulse animation for playing state ambient glow (Layer 1)
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow_pulse")
    val glowPulse = if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0.88f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_scale"
        )
    } else null

    // Spring press scale for center play/pause button (Layer 5)
    val buttonScale by animateFloatAsState(
        targetValue = if (isButtonPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 1800f),
        label = "play_btn_spring"
    )

    val glowColors = remember(palette.accent) {
        listOf(
            palette.accent.copy(alpha = 0.32f),
            palette.accent.copy(alpha = 0.10f),
            Color.Transparent
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                // Rotational circular drag gesture to seek around the circumference (PRD 21)
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val center = Offset(this.size.width / 2f, this.size.height / 2f)
                        val angle = calculateAngleFraction(offset, center)
                        dragAngleFraction = angle
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val center = Offset(this.size.width / 2f, this.size.height / 2f)
                        val angle = calculateAngleFraction(change.position, center)
                        val oldStep = (dragAngleFraction * 60).toInt()
                        val newStep = (angle * 60).toInt()
                        if (oldStep != newStep) {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        }
                        dragAngleFraction = angle
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeekFraction(dragAngleFraction)
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Layer 1: Atmospheric Outer Ambient Glow (Composited on GPU, zero recomposition)
        Spacer(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    val scale = if (isPlaying && glowPulse != null) glowPulse.value else 0.85f
                    scaleX = scale
                    scaleY = scale
                    alpha = if (isPlaying) 1.0f else 0.35f
                }
                .drawBehind {
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    val strokeWidth = 5.dp.toPx()
                    val radius = (this.size.minDimension - strokeWidth * 3) / 2f
                    val glowR = radius * 1.35f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = glowColors,
                            center = center,
                            radius = glowR
                        ),
                        radius = glowR,
                        center = center
                    )
                }
        )

        // Layers 2, 3, 4: Inactive Ring + Isolated Progress Arc + Inner Bevel
        CircularProgressCanvas(
            progressFlow = progressFlow,
            fallbackFraction = progressFraction,
            isDragging = isDragging,
            dragAngleFraction = dragAngleFraction,
            palette = palette,
            modifier = Modifier.size(size)
        )

        // Layer 5 & 6: Center Play / Pause Tactile Button with Spring Press
        Box(
            modifier = Modifier
                .size(buttonSize)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .clip(CircleShape)
                .background(palette.surfaceElevated)
                .border(2.dp, palette.borderHighlight, CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { _ ->
                            isButtonPressed = true
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            tryAwaitRelease()
                            isButtonPressed = false
                        },
                        onTap = {
                            onPlayPauseClick()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Subtle inner glow
            Box(
                modifier = Modifier
                    .size(buttonSize * 0.72f)
                    .clip(CircleShape)
                    .background(palette.pillBg)
            )

            // Animated Play/Pause Icon
            AnimatedContent(
                targetState = isPlaying,
                transitionSpec = {
                    (scaleIn(initialScale = 0.7f, animationSpec = tween(220)) + fadeIn(animationSpec = tween(220)))
                        .togetherWith(scaleOut(targetScale = 0.7f, animationSpec = tween(220)) + fadeOut(animationSpec = tween(220)))
                },
                label = "circular_play_pause_icon"
            ) { playing ->
                Icon(
                    imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                    tint = palette.accent,
                    modifier = Modifier
                        .size(38.dp)
                        .offset(x = if (playing) 0.dp else 2.dp)
                )
            }
        }
    }
}

/**
 * Isolated progress and ring track canvas to prevent recomposition of parent button and controls.
 */
@Composable
private fun CircularProgressCanvas(
    progressFlow: StateFlow<PlaybackProgress>?,
    fallbackFraction: Float,
    isDragging: Boolean,
    dragAngleFraction: Float,
    palette: com.ipodmodern.audio.ui.theme.ThemePalette,
    modifier: Modifier = Modifier
) {
    val currentProgress by (progressFlow ?: remember { MutableStateFlow(PlaybackProgress()) }).collectAsState()
    val progressFraction = if (currentProgress.durationMs > 0) {
        (currentProgress.positionMs.toFloat() / currentProgress.durationMs.toFloat()).coerceIn(0f, 1f)
    } else fallbackFraction.coerceIn(0f, 1f)

    val displayFraction = if (isDragging) dragAngleFraction else progressFraction

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val strokeWidth = 5.dp.toPx()
        val radius = (size.minDimension - strokeWidth * 3) / 2f

        // Layer 2: Inactive Outer Circular Ring Track
        drawArc(
            color = palette.surfaceElevated,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Layer 3: Active Neon Lime Circular Progress Ring
        val sweepAngle = 360f * displayFraction
        if (sweepAngle > 0.5f) {
            // Soft glow underneath the arc
            drawArc(
                color = palette.accentGlow,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth + 6.dp.toPx(), cap = StrokeCap.Round)
            )

            // Crisp electric lime arc
            drawArc(
                color = palette.accent,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Tactile Dial Thumb dot
            val thumbAngleRad = Math.toRadians((-90f + sweepAngle).toDouble())
            val thumbX = center.x + (radius * cos(thumbAngleRad)).toFloat()
            val thumbY = center.y + (radius * sin(thumbAngleRad)).toFloat()

            // Glow around thumb
            drawCircle(
                color = palette.accent.copy(alpha = 0.4f),
                radius = 10.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            // Crisp thumb dot
            drawCircle(
                color = palette.accent,
                radius = 6.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }

        // Layer 4: Inner Bevel Ring
        drawCircle(
            color = palette.borderSubtle,
            radius = radius - 14.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

/**
 * Calculates normalized fraction (0.0 to 1.0) clockwise from 12 o'clock (-90 degrees).
 */
private fun calculateAngleFraction(touchPos: Offset, center: Offset): Float {
    val dx = touchPos.x - center.x
    val dy = touchPos.y - center.y
    var rad = atan2(dy.toDouble(), dx.toDouble())
    // Offset by +PI/2 so 12 o'clock is 0
    rad += PI / 2.0
    if (rad < 0) {
        rad += 2 * PI
    }
    return (rad / (2 * PI)).toFloat().coerceIn(0f, 1f)
}
