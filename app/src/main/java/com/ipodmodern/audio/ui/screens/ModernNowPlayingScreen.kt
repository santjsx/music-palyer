package com.ipodmodern.audio.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.RaycastCard
import com.ipodmodern.audio.ui.components.RaycastKeycapBadge
import com.ipodmodern.audio.ui.components.TactileIconButton
import com.ipodmodern.audio.ui.components.TactileTimelineScrubber
import com.ipodmodern.audio.ui.components.TactileTransportRow
import com.ipodmodern.audio.ui.components.TactileVolumeBar
import com.ipodmodern.audio.ui.theme.RaycastAccentBlue
import com.ipodmodern.audio.ui.theme.RaycastAccentRed
import com.ipodmodern.audio.ui.theme.RaycastAccentYellow
import com.ipodmodern.audio.ui.theme.RaycastBody
import com.ipodmodern.audio.ui.theme.RaycastCanvas
import com.ipodmodern.audio.ui.theme.RaycastHairline
import com.ipodmodern.audio.ui.theme.RaycastHairlineStrong
import com.ipodmodern.audio.ui.theme.RaycastInk
import com.ipodmodern.audio.ui.theme.RaycastMute
import com.ipodmodern.audio.ui.theme.RaycastPrimaryWhite
import com.ipodmodern.audio.ui.theme.RaycastRadiusLg
import com.ipodmodern.audio.ui.theme.RaycastRadiusMd
import com.ipodmodern.audio.ui.theme.RaycastSurface
import com.ipodmodern.audio.ui.theme.RaycastSurfaceCard
import com.ipodmodern.audio.ui.theme.RaycastSurfaceElevated

@Composable
fun ModernNowPlayingScreen(
    currentTrack: Track?,
    allTracks: List<Track>,
    currentTrackIndex: Int,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    volumeLevel: Float,
    isShuffle: Boolean = false,
    repeatMode: Int = 0,
    isFavorite: Boolean = false,
    currentLyricText: String? = null,
    onPlayPauseClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onPrevClick: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    onVolumeChange: (Float) -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onToggleRepeat: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onLyricsClick: () -> Unit = {},
    onEqClick: () -> Unit = {},
    onCollapseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val artworkBitmap = remember(currentTrack?.artworkUri) {
        currentTrack?.artworkUri?.let { path ->
            try {
                BitmapFactory.decodeFile(path)
            } catch (e: Exception) {
                null
            }
        }
    }

    val totalQueueCount = allTracks.size.coerceAtLeast(1)
    val displayIndex = if (currentTrackIndex > 0) currentTrackIndex else 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(RaycastCanvas)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // MARK: - Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TactileIconButton(
                icon = Icons.Default.KeyboardArrowDown,
                contentDescription = "Collapse",
                onClick = onCollapseClick,
                size = 42.dp,
                iconSize = 22.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PLAYING FROM LIBRARY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaycastMute,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$displayIndex of $totalQueueCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = RaycastBody,
                    fontFamily = FontFamily.Monospace
                )
            }

            TactileIconButton(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                onClick = onToggleFavorite,
                size = 42.dp,
                iconSize = 20.dp,
                tint = if (isFavorite) RaycastAccentRed else RaycastInk
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // MARK: - Center Album Artwork Card
        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .aspectRatio(1.0f)
                .clip(RaycastRadiusLg)
                .background(RaycastSurface)
                .border(1.dp, RaycastHairline, RaycastRadiusLg),
            contentAlignment = Alignment.Center
        ) {
            if (artworkBitmap != null) {
                Image(
                    bitmap = artworkBitmap.asImageBitmap(),
                    contentDescription = currentTrack?.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = RaycastMute,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Lossless Audio",
                        fontSize = 12.sp,
                        color = RaycastMute,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // MARK: - Track Title & Artist
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = currentTrack?.title ?: "Select a Track",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = RaycastInk,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = 0.2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = currentTrack?.artist ?: "Unknown Artist",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = RaycastBody,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Hi-Res Audio Keycap Badge
            val badgeText = currentTrack?.badgeText ?: "LOSSLESS AUDIO"
            RaycastKeycapBadge(
                text = badgeText,
                textColor = RaycastAccentYellow,
                accentColor = RaycastAccentYellow
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MARK: - Precision Waveform Scrubber
        TactileTimelineScrubber(
            positionMs = positionMs,
            durationMs = durationMs,
            onSeekTo = onSeekTo
        )

        Spacer(modifier = Modifier.height(14.dp))

        // MARK: - Tactile Transport Row (Hero White CTA)
        TactileTransportRow(
            isPlaying = isPlaying,
            isShuffle = isShuffle,
            repeatMode = repeatMode,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPrevClick = onPrevClick,
            onToggleShuffle = onToggleShuffle,
            onToggleRepeat = onToggleRepeat
        )

        Spacer(modifier = Modifier.height(12.dp))

        // MARK: - Precision Volume Slider
        TactileVolumeBar(
            volume = volumeLevel,
            onVolumeChange = onVolumeChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        // MARK: - Quick Raycast Bottom Pill Actions (Lyrics & 10-EQ)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Lyrics Pill
            Box(
                modifier = Modifier
                    .weight(1.4f)
                    .clip(RaycastRadiusMd)
                    .background(RaycastSurfaceElevated)
                    .border(1.dp, RaycastHairline, RaycastRadiusMd)
                    .clickable { onLyricsClick() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Subject,
                        contentDescription = "Lyrics",
                        tint = RaycastAccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (!currentLyricText.isNullOrBlank()) currentLyricText else "Synchronized Lyrics",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = RaycastBody,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 10-Band EQ Pill
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .clip(RaycastRadiusMd)
                    .background(RaycastSurfaceElevated)
                    .border(1.dp, RaycastHairline, RaycastRadiusMd)
                    .clickable { onEqClick() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "EQ",
                        tint = RaycastPrimaryWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "10-EQ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaycastInk
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
