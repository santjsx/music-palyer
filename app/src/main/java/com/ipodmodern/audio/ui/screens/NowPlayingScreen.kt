package com.ipodmodern.audio.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.AudioQuality
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.AudioQualityBadge
import com.ipodmodern.audio.ui.theme.AudiophileGold
import com.ipodmodern.audio.ui.theme.LocalIpodColors
import com.ipodmodern.audio.ui.theme.iPodSelectionBlue
import java.util.Locale

@Composable
fun NowPlayingScreen(
    track: Track?,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    currentTrackIndex: Int = 1,
    totalTracks: Int = 1,
    currentLyricText: String? = null,
    volumeLevel: Float = 1.0f,
    onPlayPauseClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onPrevClick: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    onVolumeChange: (Float) -> Unit = {},
    onLyricsClick: () -> Unit = {},
    onEqClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalIpodColors.current

    val progress = if (durationMs > 0) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    if (track == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nothing Playing",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Track Index Counter & Lossless Badge Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$currentTrackIndex of $totalTracks",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace
            )
            AudioQualityBadge(
                quality = when {
                    track.sampleRate > 48000 || track.bitDepth > 16 -> AudioQuality.HI_RES_LOSSLESS
                    track.formatName == "MP3" || track.formatName == "AAC" -> AudioQuality.LOSSY
                    else -> AudioQuality.LOSSLESS
                },
                badgeText = track.badgeText
            )
        }

        // Center Album Artwork with Glass Bevel & Glow
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .shadow(24.dp, RoundedCornerShape(16.dp))
                .background(Color(0xFF181A1E))
                .border(1.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!track.artworkUri.isNullOrEmpty()) {
                AsyncImage(
                    model = track.artworkUri,
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(80.dp)
                )
            }

            // Glossy glass diagonal reflection
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Track Details (Title, Artist, Album)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = track.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.screenText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = track.artist,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.screenText.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.album,
                fontSize = 13.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // Synchronized Lyric Preview Snippet
            if (!currentLyricText.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onLyricsClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♪ $currentLyricText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AudiophileGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Scrubber / Seek Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = progress,
                onValueChange = { frac ->
                    val targetMs = (frac * durationMs).toLong()
                    onSeekTo(targetMs)
                },
                colors = SliderDefaults.colors(
                    thumbColor = iPodSelectionBlue,
                    activeTrackColor = iPodSelectionBlue,
                    inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(positionMs),
                    fontSize = 12.sp,
                    color = colors.screenText.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "-${formatTime((durationMs - positionMs).coerceAtLeast(0L))}",
                    fontSize = 12.sp,
                    color = colors.screenText.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Playback Transport Controls (Prev, Play/Pause, Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onEqClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = "Equalizer",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Previous
            IconButton(
                onClick = onPrevClick,
                modifier = Modifier.size(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Previous",
                    tint = colors.screenText,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Play / Pause Large Center Button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(iPodSelectionBlue, Color(0xFF0055B3))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onPlayPauseClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            // Next
            IconButton(
                onClick = onNextClick,
                modifier = Modifier.size(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Next",
                    tint = colors.screenText,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Lyrics
            IconButton(
                onClick = onLyricsClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = "Lyrics",
                    tint = AudiophileGold,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Volume Control Slider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Icon(
                imageVector = Icons.Default.VolumeDown,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = volumeLevel,
                onValueChange = onVolumeChange,
                colors = SliderDefaults.colors(
                    thumbColor = Color.LightGray,
                    activeTrackColor = Color.White.copy(alpha = 0.8f),
                    inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.US, "%02d:%02d", min, sec)
}
