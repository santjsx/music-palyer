package com.ipodmodern.audio.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.components.SleekCard
import com.ipodmodern.audio.ui.components.SleekIconButton
import com.ipodmodern.audio.ui.components.SleekToggle
import com.ipodmodern.audio.ui.theme.MintAccent
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.theme.ObsidianBorder
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

enum class SettingsSheetType {
    PLAYBACK,
    APPEARANCE,
    NOTIFICATIONS,
    ABOUT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    playerViewModel: com.ipodmodern.audio.ui.viewmodel.PlayerViewModel? = null,
    onOpenEqualizer: () -> Unit,
    onOpenEffects: () -> Unit,
    onOpenSyncHub: () -> Unit,
    onCheckUpdates: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val context = LocalContext.current
    var activeSheet by remember { mutableStateOf<SettingsSheetType?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val playerUiState = playerViewModel?.uiState?.collectAsState()?.value
    val selectedTheme = playerUiState?.themeBase ?: "Obsidian Dark"
    val selectedAccentColor = playerUiState?.accentColor ?: "Mint Green"

    // Playback Settings State
    var gaplessEnabled by remember { mutableStateOf(true) }
    var crossfadeSeconds by remember { mutableFloatStateOf(2f) }
    var replayGainEnabled by remember { mutableStateOf(true) }
    var autoResumeHeadphones by remember { mutableStateOf(true) }

    // Notification Settings State
    var lockscreenControlsEnabled by remember { mutableStateOf(true) }
    var highResArtworkInNotification by remember { mutableStateOf(true) }
    var fastSeekActionsEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(com.ipodmodern.audio.ui.theme.LocalThemePalette.current.bg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 180.dp)
    ) {
        // 1. Top Title
        item {
            Text(
                text = "Settings",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // 2. Main Settings Card Group
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
                    subtitle = "32-bit Float Engine, DSP & Effects",
                    onClick = onOpenEffects
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                // Equalizer
                SettingsItemRow(
                    icon = Icons.Default.Equalizer,
                    title = "Equalizer",
                    subtitle = "5-Band Parametric Bezier Spline & Preamp",
                    onClick = onOpenEqualizer
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                // Playback
                SettingsItemRow(
                    icon = Icons.Default.PlayCircleOutline,
                    title = "Playback",
                    subtitle = "Gapless, Crossfade (${crossfadeSeconds.toInt()}s), ReplayGain",
                    onClick = { activeSheet = SettingsSheetType.PLAYBACK }
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                // Appearance
                SettingsItemRow(
                    icon = Icons.Default.Palette,
                    title = "Appearance",
                    subtitle = "$selectedTheme • $selectedAccentColor",
                    onClick = { activeSheet = SettingsSheetType.APPEARANCE }
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                // Wi-Fi Sync Hub
                SettingsItemRow(
                    icon = Icons.Default.Wifi,
                    title = "Wi-Fi Sync Hub",
                    subtitle = "High-speed PC & browser wireless audio importer",
                    onClick = onOpenSyncHub
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                // Notifications
                SettingsItemRow(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "MediaStyle player shade & lockscreen controls",
                    onClick = { activeSheet = SettingsSheetType.NOTIFICATIONS }
                )

                // Software Updates
                SettingsItemRow(
                    icon = Icons.Default.SystemUpdate,
                    title = "Software Updates",
                    subtitle = "Version 2.4.1 • Check for OTA updates",
                    onClick = { onCheckUpdates() }
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                // About
                SettingsItemRow(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Aether Lossless Engine v2.4.1 • Flagship Core",
                    onClick = { activeSheet = SettingsSheetType.ABOUT }
                )
            }
        }
    }

    // MODAL BOTTOM SHEETS FOR INTERACTIVE SETTINGS
    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = sheetState,
            containerColor = ObsidianSurface,
            scrimColor = ObsidianBg.copy(alpha = 0.75f),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RadiusFull)
                        .background(TextMuted)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp)
            ) {
                when (activeSheet) {
                    SettingsSheetType.PLAYBACK -> {
                        Text(
                            text = "Playback Settings",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Gapless Playback Toggle
                        SettingToggleRow(
                            title = "Gapless Playback",
                            subtitle = "Seamless continuous track transitions",
                            isChecked = gaplessEnabled,
                            onCheckedChange = { gaplessEnabled = it }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Crossfade Duration Slider
                        val activeAccent = com.ipodmodern.audio.ui.theme.LocalThemePalette.current.accent
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Crossfade Duration", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${crossfadeSeconds.toInt()}s", color = activeAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = crossfadeSeconds,
                                onValueChange = { crossfadeSeconds = it },
                                valueRange = 0f..12f,
                                steps = 11,
                                colors = SliderDefaults.colors(
                                    thumbColor = activeAccent,
                                    activeTrackColor = activeAccent,
                                    inactiveTrackColor = ObsidianTrackBg
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // ReplayGain Normalization
                        SettingToggleRow(
                            title = "ReplayGain Normalization",
                            subtitle = "Match loudness across diverse albums",
                            isChecked = replayGainEnabled,
                            onCheckedChange = { replayGainEnabled = it }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Auto-resume on headphones
                        SettingToggleRow(
                            title = "Auto-Resume on Connect",
                            subtitle = "Resume playback when headphones reconnect",
                            isChecked = autoResumeHeadphones,
                            onCheckedChange = { autoResumeHeadphones = it }
                        )
                    }

                    SettingsSheetType.APPEARANCE -> {
                        Text(
                            text = "Appearance & Theming",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "ACCENT COLOR", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        val accents = listOf("Mint Green", "Cyber Gold", "Electric Cyan", "Neon Rose", "Ultra Violet")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            accents.forEach { accent ->
                                val isSelected = selectedAccentColor.equals(accent, ignoreCase = true) || selectedAccentColor.startsWith(accent.split(" ")[0], ignoreCase = true)
                                val accentClr = com.ipodmodern.audio.ui.theme.getAccentColor(accent)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RadiusMd)
                                        .background(if (isSelected) accentClr else ObsidianElevated)
                                        .clickable {
                                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                            playerViewModel?.setAccentColor(accent)
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = accent.split(" ")[0],
                                        color = if (isSelected) ObsidianBg else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(text = "THEME BASE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        val themes = listOf("Obsidian Dark", "Pure OLED Black", "Studio Slate")
                        themes.forEach { theme ->
                            val isSelected = selectedTheme.equals(theme, ignoreCase = true)
                            SleekCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                backgroundColor = if (isSelected) ObsidianElevated else ObsidianBg,
                                borderColor = if (isSelected) MintAccent else ObsidianBorder,
                                shape = RadiusLg,
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel?.setThemeBase(theme)
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = theme, color = if (isSelected) MintAccent else TextPrimary, fontWeight = FontWeight.SemiBold)
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(MintAccent)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    SettingsSheetType.NOTIFICATIONS -> {
                        Text(
                            text = "Notification Player",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        SettingToggleRow(
                            title = "Lockscreen MediaStyle",
                            subtitle = "Android system notification with interactive seekbar",
                            isChecked = lockscreenControlsEnabled,
                            onCheckedChange = { lockscreenControlsEnabled = it }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SettingToggleRow(
                            title = "High-Res Album Art",
                            subtitle = "Show full cover art in system notification shade",
                            isChecked = highResArtworkInNotification,
                            onCheckedChange = { highResArtworkInNotification = it }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        SettingToggleRow(
                            title = "Skip / Scrub Controls",
                            subtitle = "Enable quick 10s skip and previous/next buttons",
                            isChecked = fastSeekActionsEnabled,
                            onCheckedChange = { fastSeekActionsEnabled = it }
                        )
                    }

                    SettingsSheetType.ABOUT -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianElevated)
                                    .border(1.dp, MintAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = MintAccent,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aether Lossless Audio",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Version 2.4.1 • Flagship Edition",
                                color = com.ipodmodern.audio.ui.theme.LocalThemePalette.current.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RadiusLg)
                                    .background(ObsidianElevated)
                                    .border(1.dp, ObsidianBorder, RadiusLg)
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AboutSpecRow(label = "Native Engine", value = "C++20 / AAudio / OpenSL ES")
                                AboutSpecRow(label = "Precision", value = "32-Bit Float / 192 kHz Hi-Res")
                                AboutSpecRow(label = "DSP Filter", value = "5-Band Biquad IIR Spline")
                                AboutSpecRow(label = "Decoders", value = "FLAC, DSD, ALAC, WAV, MP3, AAC")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RadiusFull)
                                        .background(ObsidianElevated)
                                        .border(1.dp, ObsidianBorder, RadiusFull)
                                        .clickable {
                                            activeSheet = null
                                            onCheckUpdates()
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Check Updates",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RadiusFull)
                                        .background(MintAccent)
                                        .clickable {
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/santjsx/music-palyer"))
                                            context.startActivity(browserIntent)
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "GitHub",
                                        color = ObsidianBg,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    null -> {}
                }
            }
        }
    }
}

@Composable
private fun AboutSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = TextMuted, fontSize = 12.sp)
        }

        SleekToggle(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
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
            tint = com.ipodmodern.audio.ui.theme.LocalThemePalette.current.accent,
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
