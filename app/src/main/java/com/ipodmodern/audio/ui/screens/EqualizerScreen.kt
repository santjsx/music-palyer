package com.ipodmodern.audio.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.model.EqualizerPreset
import com.ipodmodern.audio.ui.theme.AudiophileGold
import com.ipodmodern.audio.ui.theme.LosslessGreen
import com.ipodmodern.audio.ui.theme.iPodSelectionBlue
import java.util.Locale

val BAND_LABELS = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

val STUDIO_PRESETS = listOf(
    EqualizerPreset("Flat", FloatArray(10) { 0.0f }),
    EqualizerPreset("Audiophile", floatArrayOf(2.5f, 2.0f, 1.0f, 0.0f, 0.0f, 0.5f, 1.5f, 2.0f, 2.5f, 3.0f)),
    EqualizerPreset("Bass Boost", floatArrayOf(6.0f, 5.0f, 3.5f, 1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.5f)),
    EqualizerPreset("Rock", floatArrayOf(4.5f, 3.5f, 2.0f, 0.5f, -1.0f, -0.5f, 2.0f, 3.5f, 4.0f, 4.5f)),
    EqualizerPreset("Vocal", floatArrayOf(-2.0f, -1.0f, 0.0f, 1.0f, 3.0f, 4.0f, 3.0f, 1.0f, 0.0f, -1.0f)),
    EqualizerPreset("Classical", floatArrayOf(3.0f, 2.5f, 2.0f, 1.0f, -1.0f, -1.0f, 0.0f, 2.0f, 3.0f, 3.5f)),
    EqualizerPreset("Electronic", floatArrayOf(5.0f, 4.0f, 1.5f, 0.0f, -1.5f, 1.5f, 2.0f, 3.0f, 4.5f, 5.0f)),
    EqualizerPreset("Acoustic", floatArrayOf(2.5f, 2.0f, 1.0f, 1.0f, 1.5f, 2.0f, 3.0f, 3.0f, 2.5f, 2.0f))
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06070A))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ==========================================
        // 1. TOP HEADER & HEADROOM INDICATOR
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "10-BAND CASCADED BIQUAD DSP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00C7BE),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = presetName.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Reset Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF141720))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .clickable { onPresetSelect(STUDIO_PRESETS.first()) }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Flat",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FLAT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Headroom Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (dynamicPrecutDb < -0.1f) AudiophileGold.copy(alpha = 0.15f) else LosslessGreen.copy(alpha = 0.15f))
                        .border(1.dp, if (dynamicPrecutDb < -0.1f) AudiophileGold.copy(alpha = 0.6f) else LosslessGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "HEADROOM: %.1f dB", dynamicPrecutDb),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dynamicPrecutDb < -0.1f) AudiophileGold else LosslessGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // ==========================================
        // 2. REAL-TIME FREQUENCY RESPONSE CURVE CANVAS
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F1118))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h / 2f

                // 0dB Center line
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 1.dp.toPx()
                )

                // Grid lines at +6dB and -6dB
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, midY - h * 0.25f),
                    end = Offset(w, midY - h * 0.25f),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, midY + h * 0.25f),
                    end = Offset(w, midY + h * 0.25f),
                    strokeWidth = 1.dp.toPx()
                )

                // Compute frequency curve points
                val points = mutableListOf<Offset>()
                val numBands = 10
                val dx = w / (numBands - 1)

                for (i in 0 until numBands) {
                    val gain = bandGains.getOrElse(i) { 0.0f }
                    val y = midY - (gain / 12.0f) * (midY * 0.85f)
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

                // Draw gradient under-curve fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF007AFF).copy(alpha = 0.35f),
                            Color(0xFF00C7BE).copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )

                // Draw glowing stroke line
                drawPath(
                    path = strokePath,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF007AFF), Color(0xFF00C7BE), Color(0xFF30D158))
                    ),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw band anchor dots
                points.forEach { pt ->
                    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = pt)
                    drawCircle(color = Color(0xFF007AFF), radius = 1.5.dp.toPx(), center = pt)
                }
            }
        }

        // ==========================================
        // 3. PRESET CHIP SELECTOR BAR
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            STUDIO_PRESETS.forEach { preset ->
                val isSelected = preset.name.equals(presetName, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF007AFF), Color(0xFF0055D4))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF141720), Color(0xFF101218))
                                )
                            }
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onPresetSelect(preset) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF9EACB9)
                    )
                }
            }
        }

        // ==========================================
        // 4. 10 FREQUENCY BAND VERTICAL STUDIO FADERS
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 10) {
                val gain = bandGains.getOrElse(i) { 0.0f }
                val normalizedGain = ((gain + 12.0f) / 24.0f).coerceIn(0.0f, 1.0f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(30.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaDb = -dragAmount / 7f
                                val newGain = (gain + deltaDb).coerceIn(-12.0f, 12.0f)
                                onBandGainChange(i, newGain)
                            }
                        }
                ) {
                    // Gain Decibel Readout Text
                    Text(
                        text = String.format(Locale.US, "%+.1f", gain),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            gain > 0.1f -> Color(0xFF00C7BE)
                            gain < -0.1f -> Color(0xFFFF9F0A)
                            else -> Color.Gray
                        },
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Vertical Slider Track
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(Color(0xFF13151D))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(11.dp)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // 0dB Center Reference Line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.5.dp)
                                .align(Alignment.Center)
                                .background(Color.White.copy(alpha = 0.35f))
                        )

                        // Glowing Track Fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(normalizedGain)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF007AFF).copy(alpha = 0.55f),
                                            Color(0xFF007AFF).copy(alpha = 0.15f)
                                        )
                                    )
                                )
                        )

                        // Tactile Metallic Fader Knob
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(normalizedGain)
                                .align(Alignment.BottomCenter),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .shadow(6.dp, CircleShape, spotColor = Color(0xFF007AFF))
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                Color(0xFFFFFFFF),
                                                Color(0xFF007AFF),
                                                Color(0xFF0044B0)
                                            )
                                        )
                                    )
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Frequency Band Label
                    Text(
                        text = BAND_LABELS[i],
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9EACB9),
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Text(
            text = "Drag faders vertically to tune hardware/software 10-Band EQ filters",
            fontSize = 11.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}
