package com.ipodmodern.audio.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.screens.ScreenType
import com.ipodmodern.audio.ui.theme.ModernAccentBlue
import com.ipodmodern.audio.ui.theme.ModernAccentCyan
import com.ipodmodern.audio.ui.theme.ModernTextMuted
import com.ipodmodern.audio.ui.theme.ModernTextSecondary

enum class ModernTab(
    val title: String,
    val icon: ImageVector,
    val targetScreen: ScreenType
) {
    LIBRARY("Library", Icons.Default.LibraryMusic, ScreenType.MENU_MAIN),
    NOW_PLAYING("Playing", Icons.Default.PlayCircle, ScreenType.NOW_PLAYING),
    EQUALIZER("DSP Studio", Icons.Default.GraphicEq, ScreenType.EQUALIZER),
    SYNC("Wi-Fi Sync", Icons.Default.Wifi, ScreenType.SYNC_SERVER)
}

/**
 * Floating glassmorphic navigation island.
 */
@Composable
fun ModernBottomNavIsland(
    currentScreen: ScreenType,
    onTabSelected: (ModernTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color.Black.copy(alpha = 0.7f),
                spotColor = Color.Black.copy(alpha = 0.9f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF161A23), Color(0xFF0D0F14))
                )
            )
            .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(32.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModernTab.entries.forEach { tab ->
                val isSelected = when (tab) {
                    ModernTab.LIBRARY -> currentScreen == ScreenType.MENU_MAIN ||
                            currentScreen == ScreenType.MENU_MUSIC ||
                            currentScreen == ScreenType.MENU_SONGS ||
                            currentScreen == ScreenType.MENU_ALBUMS ||
                            currentScreen == ScreenType.MENU_ARTISTS
                    ModernTab.NOW_PLAYING -> currentScreen == ScreenType.NOW_PLAYING || currentScreen == ScreenType.COVER_FLOW || currentScreen == ScreenType.LYRICS
                    ModernTab.EQUALIZER -> currentScreen == ScreenType.EQUALIZER
                    ModernTab.SYNC -> currentScreen == ScreenType.SYNC_SERVER
                }

                ModernNavItem(
                    tab = tab,
                    isSelected = isSelected,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun ModernNavItem(
    tab: ModernTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 600f),
        label = "nav_item_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) ModernAccentBlue else ModernTextMuted,
        animationSpec = tween(200),
        label = "nav_icon_color"
    )

    val bgBrush = if (isSelected) {
        Brush.radialGradient(
            listOf(ModernAccentBlue.copy(alpha = 0.20f), Color.Transparent)
        )
    } else {
        Brush.radialGradient(
            listOf(Color.Transparent, Color.Transparent)
        )
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bgBrush)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = tab.title,
                color = if (isSelected) Color.White else ModernTextMuted,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
