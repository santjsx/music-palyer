package com.ipodmodern.audio.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.theme.MintAccent
import com.ipodmodern.audio.ui.theme.MintAccentDark
import com.ipodmodern.audio.ui.theme.MintGlow
import com.ipodmodern.audio.ui.theme.MintPillBg
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.theme.ObsidianBorder
import com.ipodmodern.audio.ui.theme.ObsidianBorderHighlight
import com.ipodmodern.audio.ui.theme.ObsidianBorderSubtle
import com.ipodmodern.audio.ui.theme.ObsidianElevated
import com.ipodmodern.audio.ui.theme.ObsidianPill
import com.ipodmodern.audio.ui.theme.ObsidianSurface
import com.ipodmodern.audio.ui.theme.ObsidianTrackBg
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusMd
import com.ipodmodern.audio.ui.theme.RadiusXl
import com.ipodmodern.audio.ui.theme.TextMuted
import com.ipodmodern.audio.ui.theme.TextPrimary
import com.ipodmodern.audio.ui.theme.TextSecondary
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Sleek Obsidian Card Container with spring mechanics.
 */
@Composable
fun SleekCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ObsidianSurface,
    borderColor: Color = ObsidianBorder,
    shape: Shape = RadiusLg,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 1600f),
        label = "sleekCardScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
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

// Alias for backwards-compatibility
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ObsidianSurface,
    borderColor: Color = ObsidianBorder,
    shadowColor: Color = Color.Transparent,
    shadowOffset: Dp = 0.dp,
    shape: Shape = RadiusLg,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) = SleekCard(
    modifier = modifier,
    backgroundColor = backgroundColor,
    borderColor = borderColor,
    shape = shape,
    onClick = onClick,
    content = content
)

/**
 * Sleek Modern Icon Button with Spring feedback.
 */
@Composable
fun SleekIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp,
    backgroundColor: Color = ObsidianElevated,
    tint: Color = TextPrimary,
    borderColor: Color = ObsidianBorder,
    shape: Shape = CircleShape,
    contentDescription: String? = null
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 1800f),
        label = "iconBtnScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
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
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

// Alias for backwards compatibility
@Composable
fun NeoIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp,
    backgroundColor: Color = ObsidianElevated,
    tint: Color = TextPrimary,
    borderColor: Color = ObsidianBorder,
    shadowColor: Color = Color.Transparent,
    shape: Shape = CircleShape,
    contentDescription: String? = null
) = SleekIconButton(
    icon = icon,
    onClick = onClick,
    modifier = modifier,
    size = size,
    iconSize = iconSize,
    backgroundColor = backgroundColor,
    tint = tint,
    borderColor = borderColor,
    shape = shape,
    contentDescription = contentDescription
)

/**
 * Sleek Modern Play Button (Hero Mint Circle).
 */
@Composable
fun SleekPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    iconSize: Dp = 28.dp
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 1600f),
        label = "playBtnScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(MintAccent)
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
            },
        contentAlignment = Alignment.Center
    ) {
        if (isPlaying) {
            // Two vertical pause bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(iconSize)
                        .clip(RadiusFull)
                        .background(ObsidianBg)
                )
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(iconSize)
                        .clip(RadiusFull)
                        .background(ObsidianBg)
                )
            }
        } else {
            // Play triangle icon (slightly offset right for visual balance)
            Canvas(modifier = Modifier.size(iconSize).offset(x = 2.dp)) {
                val canvasW = this.size.width
                val canvasH = this.size.height
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(canvasW, canvasH / 2f)
                    lineTo(0f, canvasH)
                    close()
                }
                drawPath(path, color = ObsidianBg)
            }
        }
    }
}

/**
 * Interactive Rotary Loudness Knob.
 * Dial with mint radial progress arc, indicator dot, and center percentage label.
 */
@Composable
fun RotaryLoudnessKnob(
    volumePercent: Float, // 0.0f to 1.0f
    onVolumeChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    val view = LocalView.current
    var currentVolume by remember(volumePercent) { mutableFloatStateOf(volumePercent) }

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                    val touchPos = change.position
                    val angleRad = atan2(touchPos.y - center.y, touchPos.x - center.x)
                    var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                    if (angleDeg < 0) angleDeg += 360f

                    // Map angle (45° to 315°, total 270° range) to 0.0 .. 1.0
                    val startAngle = 45f
                    val sweepRange = 270f
                    val normalizedAngle = (angleDeg - startAngle).coerceIn(0f, sweepRange)
                    val newVolume = (normalizedAngle / sweepRange).coerceIn(0f, 1f)
                    currentVolume = newVolume
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onVolumeChanged(newVolume)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 5.dp.toPx()
            val radius = (this.size.minDimension - strokeWidth * 3) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // 1. Inactive background arc (270 degrees from 135 deg)
            drawArc(
                color = ObsidianTrackBg,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Active Mint Arc
            val activeSweep = 270f * currentVolume
            drawArc(
                color = MintAccent,
                startAngle = 135f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. Indicator Thumb Dot
            val thumbAngleRad = Math.toRadians((135f + activeSweep).toDouble())
            val thumbX = center.x + (radius * cos(thumbAngleRad)).toFloat()
            val thumbY = center.y + (radius * sin(thumbAngleRad)).toFloat()
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }

        // Inner Brushed Surface Card
        Box(
            modifier = Modifier
                .size(size * 0.72f)
                .clip(CircleShape)
                .background(ObsidianElevated)
                .border(1.dp, ObsidianBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LOUDNESS",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${(currentVolume * 100).toInt()}%",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Interactive Audio Waveform Visualizer.
 * Dynamic audio amplitude waveform bars with played-progress color fill.
 */
@Composable
fun WaveformVisualizer(
    progressPercent: Float, // 0.0f to 1.0f
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    barCount: Int = 42
) {
    val view = LocalView.current

    // Seeded simulated audio waveform peaks
    val barHeights = remember {
        val pattern = listOf(0.2f, 0.35f, 0.6f, 0.8f, 0.45f, 0.9f, 0.7f, 0.3f, 0.85f, 1.0f, 0.65f, 0.4f, 0.75f, 0.95f, 0.5f, 0.3f, 0.7f, 0.85f, 0.4f, 0.6f, 0.9f)
        List(barCount) { idx -> pattern[idx % pattern.size] }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onSeek(newProgress)
                }
            }
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.5f)).coerceAtLeast(3f)
        val step = totalWidth / barCount

        for (i in 0 until barCount) {
            val x = i * step + step / 2f
            val barNormalizedH = barHeights[i]
            val barH = (canvasHeight * barNormalizedH).coerceAtLeast(4f)
            val y = (canvasHeight - barH) / 2f

            val isPlayed = (i.toFloat() / barCount) <= progressPercent
            val barColor = if (isPlayed) MintAccent else ObsidianTrackBg

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x - barWidth / 2f, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

/**
 * Parametric Equalizer Bezier Spline Canvas.
 * Smooth spline curve with glowing interactive node points (60Hz, 230Hz, 910Hz, 3kHz, 14kHz).
 */
@Composable
fun ParametricCurveCanvas(
    bandGainsDb: List<Float>, // 5 band gains in range [-12.0f .. +12.0f]
    onGainChanged: (bandIndex: Int, newGainDb: Float) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp
) {
    val view = LocalView.current
    val frequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3K Hz", "14K Hz")

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .pointerInput(bandGainsDb) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val canvasW = size.width
                        val canvasH = size.height
                        val touchX = change.position.x
                        val touchY = change.position.y

                        // Find closest frequency band node
                        val step = canvasW / 6f
                        var closestIndex = 0
                        var minDistance = Float.MAX_VALUE
                        for (i in 0 until 5) {
                            val nodeX = (i + 1) * step
                            val dist = kotlin.math.abs(touchX - nodeX)
                            if (dist < minDistance) {
                                minDistance = dist
                                closestIndex = i
                            }
                        }

                        // Map Y position to -12 dB .. +12 dB
                        val normalizedY = (touchY / canvasH).coerceIn(0.1f, 0.9f)
                        val newDb = ((0.5f - normalizedY) * 24f).coerceIn(-12f, 12f)
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onGainChanged(closestIndex, newDb)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val step = w / 6f

                // Compute Node coordinates
                val points = mutableListOf<Offset>()
                points.add(Offset(0f, h * 0.5f)) // Left edge baseline
                for (i in 0 until 5) {
                    val gain = bandGainsDb.getOrElse(i) { 0f }
                    val nodeX = (i + 1) * step
                    val nodeY = h * 0.5f - (gain / 12f) * (h * 0.4f)
                    points.add(Offset(nodeX, nodeY))
                }
                points.add(Offset(w, h * 0.5f)) // Right edge baseline

                // Build Spline Path
                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlX = (p0.x + p1.x) / 2f
                        cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    }
                }

                // Gradient Underfill Path
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(MintGlow, Color.Transparent)
                    )
                )

                // Spline Stroke
                drawPath(
                    path = path,
                    color = MintAccent,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw Vertical Guide Lines & Nodes
                for (i in 0 until 5) {
                    val nodePoint = points[i + 1]

                    // Faint dotted guideline
                    drawLine(
                        color = ObsidianBorder,
                        start = Offset(nodePoint.x, 0f),
                        end = Offset(nodePoint.x, h),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Node Outer Glow Ring
                    drawCircle(
                        color = MintGlow,
                        radius = 10.dp.toPx(),
                        center = nodePoint
                    )

                    // Node Inner Core Dot
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = nodePoint
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Frequency Labels Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            frequencies.forEach { freq ->
                Text(
                    text = freq,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Sleek Modern Slider.
 */
@Composable
fun SleekSlider(
    value: Float, // 0.0f to 1.0f
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onValueChange(newProgress)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RadiusFull)
                .background(ObsidianTrackBg)
        )

        // Active Track
        Box(
            modifier = Modifier
                .fillMaxWidth(value)
                .height(6.dp)
                .clip(RadiusFull)
                .background(MintAccent)
        )

        // Thumb
        Box(
            modifier = Modifier
                .fillMaxWidth(value)
                .height(28.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, MintAccentDark, CircleShape)
            )
        }
    }
}

/**
 * Sleek Toggle Switch.
 */
@Composable
fun SleekToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 20f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 1600f),
        label = "toggleThumb"
    )

    Box(
        modifier = modifier
            .width(48.dp)
            .height(28.dp)
            .clip(RadiusFull)
            .background(if (checked) MintAccent else ObsidianTrackBg)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onCheckedChange(!checked)
            }
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer { translationX = thumbOffset * density }
                .clip(CircleShape)
                .background(if (checked) ObsidianBg else TextMuted)
        )
    }
}

// Backwards compatibility aliases
@Composable
fun NeoVinylSpinBadge(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) = Box(modifier = modifier)

@Composable
fun NeoVuMeter(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) = Box(modifier = modifier)

@Composable
fun NeoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MintAccent,
    textColor: Color = ObsidianBg,
    borderColor: Color = ObsidianBorder,
    shadowColor: Color = Color.Transparent,
    shape: Shape = RadiusFull
) {
    SleekCard(
        modifier = modifier,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        shape = shape,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
