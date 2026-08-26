package com.ipodmodern.audio.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.sin

/**
 * Studio-Grade Audio Waveform Scrubber with interactive drag seeking,
 * glowing progress beam, and live timestamp tooltip.
 */
@Composable
fun WaveformScrubber(
    positionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDuration = if (durationMs > 0) durationMs.toFloat() else 1f
    var isDragging by remember { mutableStateOf(false) }
    var dragProgressMs by remember { mutableFloatStateOf(0f) }

    val currentPosition = if (isDragging) dragProgressMs else positionMs.toFloat().coerceIn(0f, totalDuration)
    val progressFraction = (currentPosition / totalDuration).coerceIn(0f, 1f)

    val animatedFraction by animateFloatAsState(
        targetValue = progressFraction,
        label = "waveformProgress"
    )

    // Pre-calculated waveform bar heights (36 bars)
    val barCount = 36
    val barHeights = remember {
        List(barCount) { index ->
            val angle1 = (index * 0.45f)
            val angle2 = (index * 0.85f)
            val v = (sin(angle1.toDouble()) * 0.4 + sin(angle2.toDouble()) * 0.35 + 0.55).toFloat()
            v.coerceIn(0.2f, 1.0f)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1015))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Floating Scrub Tooltip
        val tooltipAlpha by animateFloatAsState(
            targetValue = if (isDragging) 1f else 0f,
            label = "tooltipFade"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (tooltipAlpha > 0.01f) {
                val seekSec = (dragProgressMs / 1000).toLong()
                Box(
                    modifier = Modifier
                        .graphicsLayer { alpha = tooltipAlpha }
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0A84FF))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "SEEK: %02d:%02d", seekSec / 60, seekSec % 60),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }
        }

        // Waveform Visualizer & Seek Touch Target
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .pointerInput(totalDuration) {
                    detectTapGestures { offset ->
                        val tappedFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        val targetMs = (tappedFraction * totalDuration).toLong()
                        onSeekTo(targetMs)
                    }
                }
                .pointerInput(totalDuration) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val initialFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            dragProgressMs = initialFraction * totalDuration
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                            dragProgressMs = fraction * totalDuration
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeekTo(dragProgressMs.toLong())
                        },
                        onDragCancel = {
                            isDragging = false
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val totalWidth = size.width
                val canvasHeight = size.height
                val barWidth = (totalWidth / barCount) * 0.65f
                val spacing = (totalWidth - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)

                val activeX = totalWidth * animatedFraction

                for (i in 0 until barCount) {
                    val barHeight = canvasHeight * barHeights[i] * 0.88f
                    val x = i * (barWidth + spacing)
                    val y = (canvasHeight - barHeight) / 2f

                    val isBarActive = (x + barWidth / 2f) <= activeX

                    val barColor = if (isBarActive) {
                        Color(0xFF0A84FF)
                    } else {
                        Color(0xFF262933)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }

                // Playhead glowing scrubber line & pill
                val headX = activeX.coerceIn(4f, totalWidth - 4f)
                drawLine(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF64D2FF), Color(0xFF0A84FF))
                    ),
                    start = Offset(headX, 0f),
                    end = Offset(headX, canvasHeight),
                    strokeWidth = if (isDragging) 3.5f else 2.5f
                )

                drawCircle(
                    color = Color.White,
                    radius = if (isDragging) 7.dp.toPx() else 4.5.dp.toPx(),
                    center = Offset(headX, canvasHeight / 2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time Indicators Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentSec = (currentPosition / 1000).toLong()
            val remainingSec = ((totalDuration - currentPosition) / 1000).toLong().coerceAtLeast(0)

            Text(
                text = String.format(Locale.US, "%02d:%02d", currentSec / 60, currentSec % 60),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF0A84FF)
            )

            Text(
                text = String.format(Locale.US, "-%02d:%02d", remainingSec / 60, remainingSec % 60),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF8F93A3)
            )
        }
    }
}
