package com.ipodmodern.audio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.theme.AudiophileGold
import com.ipodmodern.audio.ui.theme.LocalIpodColors
import com.ipodmodern.audio.ui.theme.LosslessGreen
import com.ipodmodern.audio.ui.theme.iPodSelectionBlue
import java.util.Locale

val BAND_LABELS = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

@Composable
fun EqualizerScreen(
    bandGains: FloatArray, // 10 values in dB [-12.0 .. +12.0]
    selectedBandIndex: Int,
    onBandSelect: (Int) -> Unit,
    dynamicPrecutDb: Float,
    presetName: String = "Audiophile Custom",
    modifier: Modifier = Modifier
) {
    val colors = LocalIpodColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header: Preset Name & Dynamic Headroom Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = presetName.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.screenText,
                fontFamily = FontFamily.Monospace
            )

            // Dynamic Headroom Regulator Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (dynamicPrecutDb < -0.1f) AudiophileGold.copy(alpha = 0.15f) else LosslessGreen.copy(alpha = 0.15f))
                    .border(1.dp, if (dynamicPrecutDb < -0.1f) AudiophileGold else LosslessGreen, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "HEADROOM: %.1f dB", dynamicPrecutDb),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dynamicPrecutDb < -0.1f) AudiophileGold else LosslessGreen,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 10 Frequency Band Vertical Faders
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 10) {
                val gain = bandGains.getOrElse(i) { 0.0f }
                val isSelected = i == selectedBandIndex

                // Normalize gain [-12..+12] to [0.0..1.0] fraction for thumb position
                val normalizedGain = ((gain + 12.0f) / 24.0f).coerceIn(0.0f, 1.0f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable { onBandSelect(i) }
                        .padding(horizontal = 1.dp)
                ) {
                    // Gain text (dB)
                    Text(
                        text = String.format(Locale.US, "%+.0f", gain),
                        fontSize = 8.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) iPodSelectionBlue else Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Vertical Slider Track
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f))
                            .border(
                                1.dp,
                                if (isSelected) iPodSelectionBlue else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(9.dp)
                            ),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Center 0dB reference line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .align(Alignment.Center)
                                .background(Color.Gray.copy(alpha = 0.5f))
                        )

                        // Slider Thumb Knob
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
                                    .background(
                                        if (isSelected) iPodSelectionBlue else Color.White.copy(alpha = 0.85f)
                                    )
                                    .border(1.dp, Color.Black.copy(alpha = 0.4f), CircleShape)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Frequency Label
                    Text(
                        text = BAND_LABELS[i],
                        fontSize = 8.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) colors.screenText else Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Instruction Footer
        Text(
            text = "Rotate Click Wheel to Adjust Selected Band Gain",
            fontSize = 9.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
