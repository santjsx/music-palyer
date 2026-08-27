package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.components.SleekCard
import com.ipodmodern.audio.ui.theme.MintAccent
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.theme.ObsidianBorder
import com.ipodmodern.audio.ui.theme.ObsidianSurface
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusXl
import com.ipodmodern.audio.ui.theme.TextMuted
import com.ipodmodern.audio.ui.theme.TextPrimary
import com.ipodmodern.audio.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onOpenEqualizer: () -> Unit,
    onOpenEffects: () -> Unit,
    onOpenSyncHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // 1. Top Bar
        item {
            Text(
                text = "Settings",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 2. Settings Group Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RadiusXl)
                    .background(ObsidianSurface)
                    .border(1.dp, ObsidianBorder, RadiusXl)
                    .padding(vertical = 4.dp)
            ) {
                // Audio
                SettingsItemRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Audio",
                    subtitle = "Bitrate, Sample Rate, Engine",
                    onClick = onOpenEffects
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                // Equalizer
                SettingsItemRow(
                    icon = Icons.Default.Equalizer,
                    title = "Equalizer",
                    subtitle = "Customize your sound & curves",
                    onClick = onOpenEqualizer
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                // Playback
                SettingsItemRow(
                    icon = Icons.Default.PlayCircleOutline,
                    title = "Playback",
                    subtitle = "Gapless, Crossfade duration",
                    onClick = {}
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                // Appearance
                SettingsItemRow(
                    icon = Icons.Default.Palette,
                    title = "Appearance",
                    subtitle = "Obsidian Dark, Mint Accents",
                    onClick = {}
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                // Wi-Fi Sync Hub
                SettingsItemRow(
                    icon = Icons.Default.Wifi,
                    title = "Wi-Fi Sync Hub",
                    subtitle = "High-speed PC & browser audio import",
                    onClick = onOpenSyncHub
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                // Notifications
                SettingsItemRow(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Lockscreen media player & shade",
                    onClick = {}
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                // About
                SettingsItemRow(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Aether Audio v1.2.0 (Audiophile Lossless)",
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val view = LocalView.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MintAccent,
            modifier = Modifier.size(22.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
