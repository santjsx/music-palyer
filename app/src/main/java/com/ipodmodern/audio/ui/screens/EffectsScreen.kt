package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.components.SleekCard
import com.ipodmodern.audio.ui.components.SleekIconButton
import com.ipodmodern.audio.ui.components.SleekSlider
import com.ipodmodern.audio.ui.components.SleekToggle
import com.ipodmodern.audio.ui.theme.MintAccent
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.theme.ObsidianBorder
import com.ipodmodern.audio.ui.theme.ObsidianElevated
import com.ipodmodern.audio.ui.theme.ObsidianSurface
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.TextMuted
import com.ipodmodern.audio.ui.theme.TextPrimary
import com.ipodmodern.audio.ui.theme.TextSecondary

@Composable
fun EffectsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isMasterEnabled by remember { mutableStateOf(true) }

    var spatialAudioEnabled by remember { mutableStateOf(true) }
    var bassEnhancement by remember { mutableFloatStateOf(0.70f) }
    var vocalBoost by remember { mutableFloatStateOf(0.40f) }
    var reverb by remember { mutableFloatStateOf(0.30f) }
    var stereoWidth by remember { mutableFloatStateOf(0.60f) }

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
        // 1. Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
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
                text = "EFFECTS",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            SleekToggle(
                checked = isMasterEnabled,
                onCheckedChange = { isMasterEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Spatial Audio Toggle Card
        SleekCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = ObsidianSurface,
            shape = RadiusLg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SurroundSound,
                    contentDescription = null,
                    tint = MintAccent,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "Spatial Audio",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                SleekToggle(
                    checked = spatialAudioEnabled && isMasterEnabled,
                    onCheckedChange = { spatialAudioEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Bass Enhancement
        EffectSliderCard(
            icon = Icons.Default.GraphicEq,
            title = "Bass Enhancement",
            value = bassEnhancement,
            onValueChange = { bassEnhancement = it },
            isEnabled = isMasterEnabled
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Vocal Boost
        EffectSliderCard(
            icon = Icons.Default.Mic,
            title = "Vocal Boost",
            value = vocalBoost,
            onValueChange = { vocalBoost = it },
            isEnabled = isMasterEnabled
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Reverb
        EffectSliderCard(
            icon = Icons.Default.VolumeUp,
            title = "Reverb",
            value = reverb,
            onValueChange = { reverb = it },
            isEnabled = isMasterEnabled
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Stereo Width
        EffectSliderCard(
            icon = Icons.Default.Headphones,
            title = "Stereo Width",
            value = stereoWidth,
            onValueChange = { stereoWidth = it },
            isEnabled = isMasterEnabled
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 7. Reset Effects Button
        Box(
            modifier = Modifier
                .clip(RadiusFull)
                .background(ObsidianElevated)
                .border(1.dp, ObsidianBorder, RadiusFull)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    spatialAudioEnabled = false
                    bassEnhancement = 0.0f
                    vocalBoost = 0.0f
                    reverb = 0.0f
                    stereoWidth = 0.5f
                }
                .padding(horizontal = 24.dp, vertical = 11.dp),
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
                    text = "Reset Effects",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EffectSliderCard(
    icon: ImageVector,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    isEnabled: Boolean
) {
    SleekCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = ObsidianSurface,
        shape = RadiusLg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MintAccent,
                    modifier = Modifier.size(22.dp)
                )

                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${(value * 100).toInt()}%",
                    color = MintAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            SleekSlider(
                value = if (isEnabled) value else 0f,
                onValueChange = onValueChange
            )
        }
    }
}
