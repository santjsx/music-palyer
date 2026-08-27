package com.ipodmodern.audio.ui.components

import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.screens.ScreenType
import com.ipodmodern.audio.ui.theme.RaycastAccentBlue
import com.ipodmodern.audio.ui.theme.RaycastBody
import com.ipodmodern.audio.ui.theme.RaycastHairline
import com.ipodmodern.audio.ui.theme.RaycastHairlineStrong
import com.ipodmodern.audio.ui.theme.RaycastInk
import com.ipodmodern.audio.ui.theme.RaycastMute
import com.ipodmodern.audio.ui.theme.RaycastPrimaryWhite
import com.ipodmodern.audio.ui.theme.RaycastRadiusLg
import com.ipodmodern.audio.ui.theme.RaycastRadiusXl
import com.ipodmodern.audio.ui.theme.RaycastSurface
import com.ipodmodern.audio.ui.theme.RaycastSurfaceElevated

enum class ModernTab(val title: String, val icon: ImageVector) {
    LIBRARY("Library", Icons.Default.QueueMusic),
    NOW_PLAYING("Playing", Icons.Default.PlayCircleOutline),
    EQUALIZER("DSP Studio", Icons.Default.Equalizer),
    SYNC("Wi-Fi Sync", Icons.Default.Wifi)
}

@Composable
fun ModernBottomNavIsland(
    currentScreen: ScreenType,
    onTabSelected: (ModernTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    val activeTab = when (currentScreen) {
        ScreenType.NOW_PLAYING, ScreenType.COVER_FLOW -> ModernTab.NOW_PLAYING
        ScreenType.EQUALIZER -> ModernTab.EQUALIZER
        ScreenType.SYNC_SERVER -> ModernTab.SYNC
        else -> ModernTab.LIBRARY
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RaycastRadiusXl)
            .background(RaycastSurface)
            .border(1.dp, RaycastHairline, RaycastRadiusXl)
            .padding(vertical = 6.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModernTab.values().forEach { tab ->
                val isSelected = tab == activeTab

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RaycastRadiusLg)
                        .background(if (isSelected) RaycastSurfaceElevated else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) RaycastHairlineStrong else Color.Transparent,
                            RaycastRadiusLg
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onTabSelected(tab)
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) RaycastPrimaryWhite else RaycastMute,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) RaycastPrimaryWhite else RaycastMute,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }
    }
}
