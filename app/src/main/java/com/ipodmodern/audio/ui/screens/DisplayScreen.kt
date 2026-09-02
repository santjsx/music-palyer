package com.ipodmodern.audio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    EFFECTS,
    PLAYING_QUEUE,
    PLAYLISTS,
    PLAYLIST_DETAIL,
    LYRICS,
    SYNC_SERVER,
    SETTINGS,
    SEARCH
}

@Composable
fun DisplayScreen(
    currentScreen: ScreenType,
    screenTitle: String,
    isPlaying: Boolean,
    isHoldActive: Boolean = false,
    batteryLevel: Int = 92,
    onBackClick: (() -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalIpodColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.screenBackground)
            .navigationBarsPadding()
    ) {
        // Classic iPod Glossy Top Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(44.dp)
                .background(
                    Brush.verticalGradient(
                        if (colors.isDarkScreen) {
                            listOf(Color(0xFF26292F), Color(0xFF16181B))
                        } else {
                            listOf(Color(0xFFF6F6F8), Color(0xFFE2E2E6))
                        }
                    )
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Left: Back button or Play State Icon
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBackClick != null && currentScreen != ScreenType.MENU_MAIN) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(44.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onBackClick() }
                            .padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Back",
                            tint = if (colors.isDarkScreen) Color.White else Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "MENU",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.screenText
                        )
                    }
                } else {
                    if (isPlaying) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Playing",
                            tint = if (colors.isDarkScreen) Color.White else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "❚❚",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Center: Screen Title
            Text(
                text = screenTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.screenText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 72.dp)
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
                            .size(14.dp)
                            .padding(end = 6.dp)
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

        // Optional Bottom Mini-Player Bar
        bottomBar?.invoke()
    }
}
