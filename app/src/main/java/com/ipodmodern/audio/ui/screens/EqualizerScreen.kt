package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.model.EqualizerPreset
import com.ipodmodern.audio.ui.components.ParametricCurveCanvas
import com.ipodmodern.audio.ui.components.SleekCard
import com.ipodmodern.audio.ui.components.SleekIconButton
import com.ipodmodern.audio.ui.components.SleekSlider
import com.ipodmodern.audio.ui.components.SleekToggle
import com.ipodmodern.audio.ui.theme.MintAccent
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.theme.ObsidianBorder
import com.ipodmodern.audio.ui.theme.ObsidianElevated
import com.ipodmodern.audio.ui.theme.ObsidianPill
import com.ipodmodern.audio.ui.theme.ObsidianSurface
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusMd
import com.ipodmodern.audio.ui.theme.RadiusXl
import com.ipodmodern.audio.ui.theme.TextMuted
import com.ipodmodern.audio.ui.theme.TextPrimary
import com.ipodmodern.audio.ui.theme.TextSecondary
import java.util.Locale

val MODERN_PRESETS = listOf(
    EqualizerPreset("Flat", FloatArray(10) { 0.0f }),
    EqualizerPreset("Bass Boost", floatArrayOf(6.0f, 5.0f, 3.5f, 1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.5f)),
    EqualizerPreset("Vocal", floatArrayOf(-2.0f, -1.0f, 0.0f, 1.0f, 3.0f, 4.0f, 3.0f, 1.0f, 0.0f, -1.0f)),
    EqualizerPreset("Rock", floatArrayOf(4.5f, 3.5f, 2.0f, 0.5f, -1.0f, -0.5f, 2.0f, 3.5f, 4.0f, 4.5f)),
    EqualizerPreset("Custom", FloatArray(10) { 0.0f })
)

@Composable
fun EqualizerScreen(
    bandGains: FloatArray,
    selectedBandIndex: Int = 0,
    onBandGainChange: (Int, Float) -> Unit,
    onPresetSelect: (EqualizerPreset) -> Unit,
    dynamicPrecutDb: Float = 0.0f,
    presetName: String = "Flat",
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isEnabled by remember { mutableStateOf(true) }
    var preampDb by remember { mutableFloatStateOf(4.0f) }

    // Map 10-band array to 5 representative bands for curve display (60, 230, 910, 3k, 14k)
    val displayGains = remember(bandGains) {
        listOf(
            bandGains.getOrElse(1) { 0f }, // ~60Hz
            bandGains.getOrElse(3) { 0f }, // ~230Hz
            bandGains.getOrElse(4) { 0f }, // ~910Hz
            bandGains.getOrElse(6) { 0f }, // ~3kHz
            bandGains.getOrElse(9) { 0f }  // ~14kHz
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .padding(bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Bar: Back Chevron + "EQUALIZER" Title + Master Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SleekIconButton(
                icon = Icons.Default.ChevronLeft,
                onClick = onBackClick,
                size = 38.dp,
                iconSize = 22.dp,
                contentDescription = "Back"
            )

            Text(
                text = "EQUALIZER",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            SleekToggle(
                checked = isEnabled,
                onCheckedChange = { isEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Preset Pills Horizontal Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MODERN_PRESETS.forEach { preset ->
                val isSelected = preset.name.equals(presetName, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RadiusFull)
                        .background(if (isSelected) MintAccent else ObsidianPill)
                        .border(1.dp, if (isSelected) MintAccent else ObsidianBorder, RadiusFull)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onPresetSelect(preset)
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset.name,
                        color = if (isSelected) ObsidianBg else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Parametric Spline Curve Deck
        SleekCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = ObsidianSurface,
            shape = RadiusXl
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ParametricCurveCanvas(
                    bandGainsDb = displayGains,
                    onGainChanged = { displayIdx, newGain ->
                        if (isEnabled) {
                            val actualBand = when (displayIdx) {
                                0 -> 1
                                1 -> 3
                                2 -> 4
                                3 -> 6
                                else -> 9
                            }
                            onBandGainChange(actualBand, newGain)
                        }
                    },
                    height = 180.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. PREAMP Gain Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RadiusLg)
                .background(ObsidianSurface)
                .border(1.dp, ObsidianBorder, RadiusLg)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PREAMP",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val sign = if (preampDb > 0) "+" else ""
                Text(
                    text = String.format(Locale.US, "%s%.1f dB", sign, preampDb),
                    color = MintAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Slider mapping -10 dB .. +10 dB to 0.0 .. 1.0
            val sliderValue = ((preampDb + 10f) / 20f).coerceIn(0f, 1f)
            SleekSlider(
                value = sliderValue,
                onValueChange = { norm ->
                    val newDb = (norm * 20f) - 10f
                    preampDb = newDb
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "-10 dB", color = TextMuted, fontSize = 11.sp)
                Text(text = "+10 dB", color = TextMuted, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Reset Button
        Box(
            modifier = Modifier
                .clip(RadiusFull)
                .background(ObsidianElevated)
                .border(1.dp, ObsidianBorder, RadiusFull)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onPresetSelect(MODERN_PRESETS[0]) // Flat
                    preampDb = 0f
                }
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Reset",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
