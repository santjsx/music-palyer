package com.ipodmodern.audio.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.model.EqualizerPreset
import com.ipodmodern.audio.ui.theme.AudiophileGold
import com.ipodmodern.audio.ui.theme.LocalIpodColors
import com.ipodmodern.audio.ui.theme.LosslessGreen
import com.ipodmodern.audio.ui.theme.iPodSelectionBlue
import java.util.Locale

val BAND_LABELS = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

val PRESETS = listOf(
    EqualizerPreset("Flat", FloatArray(10) { 0.0f }),
    EqualizerPreset("Audiophile", floatArrayOf(2.5f, 2.0f, 1.0f, 0.0f, 0.0f, 0.5f, 1.5f, 2.0f, 2.5f, 3.0f)),
    EqualizerPreset("Bass Boost", floatArrayOf(6.0f, 5.0f, 3.5f, 1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.5f)),
    EqualizerPreset("Vocal", floatArrayOf(-2.0f, -1.0f, 0.0f, 1.0f, 3.0f, 4.0f, 3.0f, 1.0f, 0.0f, -1.0f)),
    EqualizerPreset("Rock", floatArrayOf(4.5f, 3.5f, 2.0f, 0.5f, -1.0f, -0.5f, 2.0f, 3.5f, 4.0f, 4.5f)),
    EqualizerPreset("Classical", floatArrayOf(3.0f, 2.5f, 2.0f, 1.0f, -1.0f, -1.0f, 0.0f, 2.0f, 3.0f, 3.5f))
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
    val colors = LocalIpodColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Preset Name & Dynamic Headroom Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = presetName.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.screenText,
                fontFamily = FontFamily.Monospace
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (dynamicPrecutDb < -0.1f) AudiophileGold.copy(alpha = 0.15f) else LosslessGreen.copy(alpha = 0.15f))
                    .border(1.dp, if (dynamicPrecutDb < -0.1f) AudiophileGold else LosslessGreen, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "HEADROOM: %.1f dB", dynamicPrecutDb),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dynamicPrecutDb < -0.1f) AudiophileGold else LosslessGreen,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Quick Preset Horizontal Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PRESETS.forEach { preset ->
                val isSelected = preset.name.equals(presetName, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) iPodSelectionBlue else Color.White.copy(alpha = 0.08f))
                        .clickable { onPresetSelect(preset) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.Gray
                    )
                }
            }
        }

        // 10 Frequency Band Vertical Interactive Faders
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
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
                        .width(28.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaDb = -dragAmount / 8f
                                val newGain = (gain + deltaDb).coerceIn(-12.0f, 12.0f)
                                onBandGainChange(i, newGain)
                            }
                        }
                ) {
                    // Gain text
                    Text(
                        text = String.format(Locale.US, "%+.0f", gain),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gain != 0f) iPodSelectionBlue else Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Vertical Slider Track
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // 0dB Center line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.5.dp)
                                .align(Alignment.Center)
                                .background(Color.Gray.copy(alpha = 0.6f))
                        )

                        // Thumb knob
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(normalizedGain)
                                .align(Alignment.BottomCenter),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(iPodSelectionBlue)
                                    .border(1.5.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Band label
                    Text(
                        text = BAND_LABELS[i],
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.screenText,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Text(
            text = "Drag faders vertically to tune 10-Band Biquad IIR filters",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
