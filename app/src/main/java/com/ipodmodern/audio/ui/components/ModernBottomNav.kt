package com.ipodmodern.audio.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.screens.ScreenType
import com.ipodmodern.audio.ui.theme.MintAccent
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.theme.ObsidianBorder
import com.ipodmodern.audio.ui.theme.ObsidianElevated
import com.ipodmodern.audio.ui.theme.ObsidianSurface
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusXl
import com.ipodmodern.audio.ui.theme.TextMuted
import com.ipodmodern.audio.ui.theme.TextPrimary
import com.ipodmodern.audio.ui.theme.TextSecondary

enum class ModernTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    SEARCH("Search", Icons.Default.Search),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun ModernBottomNavIsland(
    currentScreen: ScreenType,
    onTabSelected: (ModernTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val palette = com.ipodmodern.audio.ui.theme.LocalThemePalette.current

    val activeTab = when (currentScreen) {
        ScreenType.MENU_MAIN -> ModernTab.HOME
        ScreenType.SEARCH -> ModernTab.SEARCH
        ScreenType.SETTINGS, ScreenType.SYNC_SERVER, ScreenType.EQUALIZER -> ModernTab.SETTINGS
        else -> ModernTab.LIBRARY
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
            .shadow(20.dp, RadiusFull, spotColor = Color(0x88000000))
            .clip(RadiusFull)
            .background(palette.surface.copy(alpha = 0.92f))
            .border(1.dp, palette.borderSubtle, RadiusFull)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeAccent = palette.accent
            val activePillBg = palette.pillBg
            val activePillBorder = palette.borderHighlight

            ModernTab.values().forEach { tab ->
                val isSelected = tab == activeTab

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.05f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 1400f),
                    label = "tab_scale"
                )

                val pillBg = if (isSelected) activePillBg else Color.Transparent
                val pillBorder = if (isSelected) activePillBorder else Color.Transparent

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RadiusFull)
                        .background(pillBg)
                        .border(1.dp, pillBorder, RadiusFull)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onTabSelected(tab)
                        }
                        .padding(horizontal = if (isSelected) 18.dp else 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) activeAccent else palette.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.title,
                            color = if (isSelected) activeAccent else palette.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
