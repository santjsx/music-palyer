package com.ipodmodern.audio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.log10

/**
 * Tactile Studio Hi-Fi Volume Deck with percentage / dB readout and quick mute toggle.
 */
@Composable
fun VolumeDeck(
    volumeLevel: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var previousVolume by remember { mutableFloatStateOf(0.85f) }

    val isMuted = volumeLevel <= 0.001f
    val percent = (volumeLevel * 100).toInt()
    val dbValue = if (volumeLevel > 0.001f) (20 * log10(volumeLevel.toDouble())).toFloat() else -60.0f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1015))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Quick Mute / Unmute Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isMuted) Color(0xFFFF453A).copy(alpha = 0.2f) else Color(0xFF1E212B))
                .border(
                    1.dp,
                    if (isMuted) Color(0xFFFF453A).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                    CircleShape
                )
                .clickable {
                    if (isMuted) {
                        onVolumeChange(if (previousVolume > 0.05f) previousVolume else 0.85f)
                    } else {
                        previousVolume = volumeLevel
                        onVolumeChange(0f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isMuted -> Icons.Default.VolumeOff
                    volumeLevel < 0.35f -> Icons.AutoMirrored.Filled.VolumeMute
                    volumeLevel < 0.7f -> Icons.AutoMirrored.Filled.VolumeDown
                    else -> Icons.AutoMirrored.Filled.VolumeUp
                },
                contentDescription = "Mute Toggle",
                tint = if (isMuted) Color(0xFFFF453A) else Color(0xFFCACFD9),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Center Volume Slider
        Slider(
            value = volumeLevel,
            onValueChange = onVolumeChange,
            valueRange = 0.0f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF0A84FF),
                inactiveTrackColor = Color(0xFF262933)
            ),
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Readout Pill (e.g. 85%)
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = String.format(Locale.US, "%d%%", percent),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isMuted) Color(0xFFFF453A) else Color(0xFF0A84FF)
            )
            Text(
                text = if (isMuted) "MUTED" else String.format(Locale.US, "%.1fdB", dbValue),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF6B7280)
            )
        }
    }
}
