package com.ipodmodern.audio.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.components.BatteryIndicator
import com.ipodmodern.audio.ui.theme.LocalIpodColors

enum class ScreenType {
    MENU_MAIN,
    MENU_MUSIC,
    MENU_ARTISTS,
    MENU_ALBUMS,
    MENU_SONGS,
    NOW_PLAYING,
    COVER_FLOW,
    EQUALIZER,
    LYRICS,
    SYNC_SERVER,
    SETTINGS
}

@Composable
fun DisplayScreen(
    currentScreen: ScreenType,
    screenTitle: String,
    isPlaying: Boolean,
    isHoldActive: Boolean,
    batteryLevel: Int = 92,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalIpodColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenBackground)
    ) {
        // Classic iPod Top Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .background(
                    Brush.verticalGradient(
                        if (colors.isDarkScreen) {
                            listOf(Color(0xFF22242A), Color(0xFF141518))
                        } else {
                            listOf(Color(0xFFF2F2F7), Color(0xFFE5E5EA))
                        }
                    )
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Left: Playback State Icon
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPlaying) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Playing",
                        tint = if (colors.isDarkScreen) Color.White else Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = "❚❚",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }

            // Center: Screen Title
            Text(
                text = screenTitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.screenText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            // Right: Hold Lock Icon + Battery
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isHoldActive) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Hold Active",
                        tint = Color(0xFFFF9500),
                        modifier = Modifier
                            .size(12.dp)
                            .padding(end = 4.dp)
                    )
                }
                BatteryIndicator(levelPercent = batteryLevel)
            }
        }

        // Active Screen Body Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content()
        }
    }
}
