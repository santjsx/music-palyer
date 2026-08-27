package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.model.EqualizerPreset
import com.ipodmodern.audio.ui.components.RaycastCard
import com.ipodmodern.audio.ui.components.RaycastKeycapBadge
import com.ipodmodern.audio.ui.theme.AetherAmber
import com.ipodmodern.audio.ui.theme.AetherAsh
import com.ipodmodern.audio.ui.theme.AetherCanvas
import com.ipodmodern.audio.ui.theme.AetherCyan
import com.ipodmodern.audio.ui.theme.AetherCyanGlow
import com.ipodmodern.audio.ui.theme.AetherEmerald
import com.ipodmodern.audio.ui.theme.AetherHairline
import com.ipodmodern.audio.ui.theme.AetherHairlineStrong
import com.ipodmodern.audio.ui.theme.AetherInk
import com.ipodmodern.audio.ui.theme.AetherMute
import com.ipodmodern.audio.ui.theme.AetherPrimaryWhite
import com.ipodmodern.audio.ui.theme.AetherRadiusMd
import com.ipodmodern.audio.ui.theme.AetherRadiusSm
import com.ipodmodern.audio.ui.theme.AetherSurface
import com.ipodmodern.audio.ui.theme.AetherSurfaceElevated
import com.ipodmodern.audio.ui.theme.AetherViolet
import java.util.Locale

val AETHER_BAND_LABELS = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

val AETHER_STUDIO_PRESETS = listOf(
    EqualizerPreset("Flat", FloatArray(10) { 0.0f }),
    EqualizerPreset("Master", floatArrayOf(2.5f, 2.0f, 1.0f, 0.0f, 0.0f, 0.5f, 1.5f, 2.0f, 2.5f, 3.0f)),
    EqualizerPreset("Bass Surge", floatArrayOf(6.0f, 5.0f, 3.5f, 1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.5f)),
    EqualizerPreset("Vocal Focus", floatArrayOf(-2.0f, -1.0f, 0.0f, 1.0f, 3.0f, 4.0f, 3.0f, 1.0f, 0.0f, -1.0f)),
    EqualizerPreset("Electronic", floatArrayOf(5.0f, 4.0f, 1.5f, 0.0f, -1.5f, 1.5f, 2.0f, 3.0f, 4.5f, 5.0f)),
    EqualizerPreset("Acoustic", floatArrayOf(2.5f, 2.0f, 1.0f, 1.0f, 1.5f, 2.0f, 3.0f, 3.0f, 2.5f, 2.0f)),
    EqualizerPreset("Rock", floatArrayOf(4.5f, 3.5f, 2.0f, 0.5f, -1.0f, -0.5f, 2.0f, 3.5f, 4.0f, 4.5f))
)

@Composable
fun EqualizerScreen(
    bandGains: FloatArray,
    selectedBandIndex: Int,
    onBandGainChange: (Int, Float) -> Unit = { _, _ -> },
    onPresetSelect: (EqualizerPreset) -> Unit = {},
    dynamicPrecutDb: Float,
    presetName: String = "Audiophile Custom",
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(AetherCanvas)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // MARK: - Header Bar & Headroom Monitor
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AETHER DSP STUDIO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AetherCyan,
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = presetName.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AetherInk,
                    letterSpacing = 0.2.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reset to Flat Action Pill
                Box(
                    modifier = Modifier
                        .clip(AetherRadiusMd)
                        .background(AetherSurfaceElevated)
                        .border(1.dp, AetherHairline, AetherRadiusMd)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onPresetSelect(AETHER_STUDIO_PRESETS.first())
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Flat",
                            tint = AetherInk,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "FLAT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AetherInk,
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                // Headroom Keycap Badge
                RaycastKeycapBadge(
                    text = String.format(Locale.US, "HEADROOM: %.1f dB", dynamicPrecutDb),
                    textColor = if (dynamicPrecutDb < -0.1f) AetherAmber else AetherEmerald,
                    accentColor = if (dynamicPrecutDb < -0.1f) AetherAmber else AetherEmerald
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MARK: - Parametric Response Card
        RaycastCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp),
            shape = AetherRadiusMd,
            backgroundColor = AetherSurface
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
                val w = size.width
                val h = size.height
                val midY = h / 2f

                // 0dB Center reference line
                drawLine(
                    color = AetherHairlineStrong,
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 1.dp.toPx()
                )

                // Grid lines at +6dB and -6dB
                drawLine(
                    color = AetherHairline,
                    start = Offset(0f, midY - h * 0.28f),
                    end = Offset(w, midY - h * 0.28f),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = AetherHairline,
                    start = Offset(0f, midY + h * 0.28f),
                    end = Offset(w, midY + h * 0.28f),
                    strokeWidth = 1.dp.toPx()
                )

                // Compute frequency curve points
                val points = mutableListOf<Offset>()
                val numBands = 10
                val dx = w / (numBands - 1)

                for (i in 0 until numBands) {
                    val gain = bandGains.getOrElse(i) { 0.0f }
                    val y = midY - (gain / 12.0f) * (midY * 0.82f)
                    points.add(Offset(i * dx, y))
                }

                // Construct smooth spline path
                val strokePath = Path()
                val fillPath = Path()

                strokePath.moveTo(points[0].x, points[0].y)
                fillPath.moveTo(points[0].x, points[0].y)

                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val cx = (p0.x + p1.x) / 2f
                    strokePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }

                fillPath.lineTo(w, h)
                fillPath.lineTo(0f, h)
                fillPath.close()

                // Subtle Under-curve Gradient Fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            AetherCyan.copy(alpha = 0.25f),
                            AetherViolet.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )

                // Crisp Cyan Stroke Line
                drawPath(
                    path = strokePath,
                    color = AetherCyan,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Band Anchor Dots
                points.forEach { pt ->
                    drawCircle(color = AetherPrimaryWhite, radius = 2.5.dp.toPx(), center = pt)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // MARK: - Preset Chips Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AETHER_STUDIO_PRESETS.forEach { preset ->
                val isSelected = preset.name.equals(presetName, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) AetherSurfaceElevated else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) AetherCyan else AetherHairline,
                            CircleShape
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onPresetSelect(preset)
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AetherPrimaryWhite else AetherMute
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // MARK: - 10 Frequency Band Precision Vertical Faders Card
        RaycastCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            shape = AetherRadiusMd,
            backgroundColor = AetherSurface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 10) {
                    val gain = bandGains.getOrElse(i) { 0.0f }
                    val normalizedGain = ((gain + 12.0f) / 24.0f).coerceIn(0.0f, 1.0f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { change, dragAmount ->
                                    change.consume()
                                    val deltaDb = -dragAmount / 8f
                                    val newGain = (gain + deltaDb).coerceIn(-12.0f, 12.0f)
                                    onBandGainChange(i, newGain)
                                }
                            }
                    ) {
                        // Gain Decibel Readout Text
                        Text(
                            text = String.format(Locale.US, "%+.0f", gain),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                gain > 0.1f -> AetherCyan
                                gain < -0.1f -> AetherAmber
                                else -> AetherMute
                            },
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Vertical Fader Track
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .weight(1f)
                                .clip(RoundedCornerShape(7.dp))
                                .background(AetherSurfaceElevated)
                                .border(1.dp, AetherHairline, RoundedCornerShape(7.dp)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // 0dB Center Reference Dash
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .align(Alignment.Center)
                                    .background(AetherHairlineStrong)
                            )

                            // Active Fill Track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(normalizedGain)
                                    .background(
                                        if (gain > 0.1f) AetherCyan.copy(alpha = 0.5f)
                                        else if (gain < -0.1f) AetherAmber.copy(alpha = 0.35f)
                                        else AetherHairlineStrong
                                    )
                            )

                            // Tactile White Circular Fader Knob
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(normalizedGain)
                                    .align(Alignment.BottomCenter),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(AetherPrimaryWhite)
                                        .border(1.dp, AetherCyan, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Frequency Band Label
                        Text(
                            text = AETHER_BAND_LABELS[i],
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AetherInk,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Sculpt 10-Band Biquad DSP response curve in real-time",
            fontSize = 11.sp,
            color = AetherAsh,
            textAlign = TextAlign.Center
        )

        // Bottom spacing so nothing is ever clipped by floating Mini Player or Nav Bar
        Spacer(modifier = Modifier.height(140.dp))
    }
}
