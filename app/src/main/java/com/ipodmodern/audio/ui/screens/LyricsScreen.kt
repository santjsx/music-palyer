package com.ipodmodern.audio.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.model.LyricLine
import com.ipodmodern.audio.ui.components.TactileIconButton
import com.ipodmodern.audio.ui.theme.ModernAccentCyan
import com.ipodmodern.audio.ui.theme.ModernAccentGold
import com.ipodmodern.audio.ui.theme.ModernTextMuted
import com.ipodmodern.audio.ui.theme.ModernTextPrimary
import com.ipodmodern.audio.ui.theme.ModernTextSecondary

@Composable
fun LyricsScreen(
    lyrics: List<LyricLine>,
    activeLyricIndex: Int,
    songTitle: String,
    onSeekTo: ((Long) -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(activeLyricIndex) {
        if (lyrics.isNotEmpty() && activeLyricIndex >= 0) {
            listState.animateScrollToItem(
                (activeLyricIndex - 2).coerceAtLeast(0)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF101420),
                        Color(0xFF090A0E),
                        Color(0xFF050608)
                    )
                )
            )
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (onBackClick != null) {
                TactileIconButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Back",
                    onClick = onBackClick,
                    size = 42.dp,
                    iconSize = 24.dp
                )
            } else {
                Spacer(modifier = Modifier.size(42.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LYRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ModernAccentCyan,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = songTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextPrimary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.size(42.dp))
        }

        if (lyrics.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "No Synchronized Lyrics",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernTextPrimary
                    )
                    Text(
                        text = "Place a matching .lrc file (e.g. SongName.lrc) in the same folder as your audio track.",
                        fontSize = 13.sp,
                        color = ModernTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isActive = index == activeLyricIndex

                    val textColor by animateColorAsState(
                        targetValue = if (isActive) Color.White else ModernTextMuted.copy(alpha = 0.45f),
                        animationSpec = tween(220),
                        label = "lyric_color"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (isActive) 1.05f else 1.0f,
                        animationSpec = spring(stiffness = 500f),
                        label = "lyric_scale"
                    )

                    Text(
                        text = line.text,
                        fontSize = if (isActive) 24.sp else 19.sp,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = textColor,
                        lineHeight = if (isActive) 32.sp else 26.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale)
                            .clickable {
                                onSeekTo?.invoke(line.timeMs)
                            }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            // Top Gradient Fade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF101420), Color.Transparent)
                        )
                    )
            )

            // Bottom Gradient Fade
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xFF050608))
                        )
                    )
            )
        }
    }
}
