package com.ipodmodern.audio.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.AudioQuality
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.AudioQualityBadge
import com.ipodmodern.audio.ui.components.TactileIconButton
import com.ipodmodern.audio.ui.components.TactileTimelineScrubber
import com.ipodmodern.audio.ui.components.TactileTransportRow
import com.ipodmodern.audio.ui.components.TactileVolumeBar
import com.ipodmodern.audio.ui.theme.ModernAccentBlue
import com.ipodmodern.audio.ui.theme.ModernAccentCyan
import com.ipodmodern.audio.ui.theme.ModernAccentPurple
import com.ipodmodern.audio.ui.theme.ModernAccentRose
import com.ipodmodern.audio.ui.theme.ModernTextMuted
import com.ipodmodern.audio.ui.theme.ModernTextPrimary
import com.ipodmodern.audio.ui.theme.ModernTextSecondary
import java.io.File

@Composable
fun ModernNowPlayingScreen(
    currentTrack: Track?,
    allTracks: List<Track>,
    currentTrackIndex: Int,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    volumeLevel: Float,
    isShuffle: Boolean,
    repeatMode: Int,
    isFavorite: Boolean,
    currentLyricText: String?,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onLyricsClick: () -> Unit,
    onEqClick: () -> Unit,
    onCollapseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF131722),
                        Color(0xFF090A0E),
                        Color(0xFF050608)
                    )
                )
            )
    ) {
        // Ambient colorful background glow cast from album art
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .align(Alignment.TopCenter)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ModernAccentBlue.copy(alpha = 0.28f),
                                ModernAccentPurple.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(size.width / 2, size.height * 0.4f),
                            radius = size.width * 0.65f
                        )
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // MARK: - Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TactileIconButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse",
                    onClick = onCollapseClick,
                    size = 42.dp,
                    iconSize = 24.dp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM LIBRARY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernTextMuted,
                        letterSpacing = 1.2.sp
                    )
                    if (currentTrack != null) {
                        Text(
                            text = "${currentTrackIndex} of ${allTracks.size.coerceAtLeast(1)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ModernTextSecondary
                        )
                    }
                }

                TactileIconButton(
                    icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    isActive = isFavorite,
                    activeColor = ModernAccentRose,
                    onClick = onToggleFavorite,
                    size = 42.dp,
                    iconSize = 20.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MARK: - Hero Album Artwork Card
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f)
                    .shadow(
                        elevation = 32.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Color.Black.copy(alpha = 0.8f),
                        spotColor = ModernAccentBlue.copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF141822))
                    .border(1.5.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (currentTrack?.artworkUri != null) {
                    val model = if (currentTrack.artworkUri.startsWith("/")) File(currentTrack.artworkUri) else currentTrack.artworkUri
                    AsyncImage(
                        model = model,
                        contentDescription = currentTrack.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = ModernAccentBlue,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // MARK: - Song Metadata & Hi-Res Badge
            if (currentTrack != null) {
                Text(
                    text = currentTrack.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ModernTextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${currentTrack.artist} • ${currentTrack.album}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ModernTextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // High-Res Audio Badge
                AudioQualityBadge(
                    quality = when {
                        currentTrack.sampleRate > 48000 || currentTrack.bitDepth > 16 -> AudioQuality.HI_RES_LOSSLESS
                        currentTrack.formatName == "MP3" || currentTrack.formatName == "AAC" -> AudioQuality.LOSSY
                        else -> AudioQuality.LOSSLESS
                    },
                    badgeText = currentTrack.badgeText
                )
            } else {
                Text(
                    text = "No Active Track",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextMuted
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // MARK: - Interactive Waveform / Timeline Scrubber
            TactileTimelineScrubber(
                positionMs = positionMs,
                durationMs = durationMs,
                onSeekTo = onSeekTo
            )

            Spacer(modifier = Modifier.height(24.dp))

            // MARK: - Tactile Transport Controls (Shuffle, Prev, Play/Pause, Next, Repeat)
            TactileTransportRow(
                isPlaying = isPlaying,
                isShuffle = isShuffle,
                repeatMode = repeatMode,
                onTogglePlayPause = onPlayPauseClick,
                onPrevClick = onPrevClick,
                onNextClick = onNextClick,
                onToggleShuffle = onToggleShuffle,
                onToggleRepeat = onToggleRepeat
            )

            Spacer(modifier = Modifier.height(16.dp))

            // MARK: - Tactile Volume Deck
            TactileVolumeBar(
                volume = volumeLevel,
                onVolumeChange = onVolumeChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // MARK: - Synchronized Lyric Preview Pill & Shortcuts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Lyrics Sheet Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141822))
                        .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(16.dp))
                        .clickable(onClick = onLyricsClick)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subject,
                            contentDescription = "Lyrics",
                            tint = ModernAccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (!currentLyricText.isNullOrBlank()) currentLyricText else "Synchronized Lyrics",
                            color = if (!currentLyricText.isNullOrBlank()) ModernAccentCyan else ModernTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Quick EQ Studio Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141822))
                        .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(16.dp))
                        .clickable(onClick = onEqClick)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "EQ",
                            tint = ModernAccentPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "10-EQ",
                            color = ModernTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
