package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.LyricLine
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusSm
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusMd
import com.ipodmodern.audio.ui.theme.RadiusXl
import com.ipodmodern.audio.ui.viewmodel.PlaybackProgress
import com.ipodmodern.audio.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Flagship Now Playing Screen directly inspired by reference Screenshot 1:
 * - Dynamic ambient mesh backdrop derived from the album art
 * - Immersive top album artwork display with rounded corners
 * - Bold track title & subtitle with circular glassmorphic 3-dots menu button
 * - Live synchronized lyrics snippet preview bar with trailing '>' chevron
 * - Minimalist progress scrubber with '0:00' elapsed and '-0:00' remaining time
 * - Centered playback controls (Prev, large Play/Pause, Next)
 * - Integrated volume slider with speaker low & high icons
 * - Bottom utility toolbar: Shuffle, Repeat, Infinity / Autoplay toggle, Queue
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernNowPlayingScreen(
    playerViewModel: PlayerViewModel,
    onBackClick: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenQueue: () -> Unit = {},
    onOpenLyrics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by playerViewModel.uiState.collectAsState()
    val view = LocalView.current

    val currentTrack = uiState.currentTrack
    val artworkFile = remember(currentTrack?.artworkUri) {
        currentTrack?.artworkUri?.let { File(it) }
    }

    // Options Modal Sheet
    var showOptionsMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Autoplay / Infinity toggle state
    var isInfinityAutoplay by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF14130F))
    ) {
        // LAYER 0: Ambient Blurred Backdrop from Album Art
        if (artworkFile != null) {
            AsyncImage(
                model = artworkFile,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(60.dp)
            )
        }

        // LAYER 1: Warm Olive / Dynamic Scrim Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x55000000),
                            Color(0x882A2518),
                            Color(0xDD201C12),
                            Color(0xFF14120C)
                        )
                    )
                )
        )

        // LAYER 2: Main Interactive Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar: Down Arrow Dismiss
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onBackClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // 2. Center Album Artwork Container
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(1f)
                    .clip(RadiusXl)
                    .shadow(16.dp, RadiusXl)
                    .background(Color(0x33000000)),
                contentAlignment = Alignment.Center
            ) {
                if (artworkFile != null) {
                    AsyncImage(
                        model = artworkFile,
                        contentDescription = currentTrack?.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(90.dp)
                    )
                }
            }

            // 3. Track Title, Artist & 3-Dots Action Button
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        AnimatedContent(
                            targetState = currentTrack?.title ?: "Dil Se",
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "np_title"
                        ) { title ->
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentTrack?.artist?.ifBlank { "Karthik, Shweta Mohan" } ?: "Karthik, Shweta Mohan",
                            color = Color(0xFFD4D4D8).copy(alpha = 0.85f),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 3-Dots Action Button (Circular glassmorphic button)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                showOptionsMenu = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Options",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Apple Music / Tidal Grade Audio Quality Badge
                if (currentTrack != null) {
                    val isHiRes = currentTrack.isHiRes
                    val isLossless = currentTrack.isLossless
                    val badgeBg = when {
                        isHiRes -> Color(0xFF2C2411)
                        isLossless -> Color(0x2EFFFFFF)
                        else -> Color(0x22FFFFFF)
                    }
                    val badgeBorder = when {
                        isHiRes -> Color(0xFFFFD159)
                        isLossless -> Color(0x55FFFFFF)
                        else -> Color(0x33FFFFFF)
                    }
                    val badgeTextColor = when {
                        isHiRes -> Color(0xFFFFD159)
                        else -> Color.White
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RadiusSm)
                                .background(badgeBg)
                                .border(1.dp, badgeBorder, RadiusSm)
                                .padding(horizontal = 7.dp, vertical = 2.5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentTrack.displayBadge,
                                color = badgeTextColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                        }

                        Text(
                            text = currentTrack.audioSpecText,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Live Lyrics Preview directly over the Scrubber Progress Bar (Isolated State Flow)
                NowPlayingLiveLyricsRow(
                    progressFlow = playerViewModel.playbackProgress,
                    lyrics = uiState.lyrics,
                    onOpenLyrics = onOpenLyrics
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Scrubber Bar with Elapsed & Remaining Time (Isolated State Flow)
                NowPlayingScrubberBar(
                    progressFlow = playerViewModel.playbackProgress,
                    fallbackDurationMs = currentTrack?.durationMs ?: 0L,
                    onSeek = { targetMs -> playerViewModel.seekTo(targetMs) }
                )
            }

            // 6. Central Playback Transport Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Previous
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            playerViewModel.prevTrack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Main Play / Pause Button (Large Solid White)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            playerViewModel.togglePlayPause()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }

                // Skip Next
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            playerViewModel.nextTrack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // 7. Inline Volume Slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeDown,
                    contentDescription = "Volume Low",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )

                Slider(
                    value = uiState.volume,
                    onValueChange = { newVol ->
                        playerViewModel.setVolume(newVol)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White.copy(alpha = 0.85f),
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Volume High",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // 8. Bottom Utility Ribbon: Shuffle, Repeat, Infinity Autoplay, Queue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            playerViewModel.toggleShuffle()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (uiState.isShuffle) Color(0xFFE50914) else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Repeat
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            playerViewModel.toggleRepeat()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.repeatMode == 2) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (uiState.repeatMode > 0) Color(0xFFE50914) else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Infinity / Autoplay Pill Button
                Box(
                    modifier = Modifier
                        .clip(RadiusFull)
                        .background(if (isInfinityAutoplay) Color(0x55FFFFFF) else Color.Transparent)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            isInfinityAutoplay = !isInfinityAutoplay
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = "Autoplay",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Queue / Playlist
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onOpenQueue()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    // Options Bottom Sheet
    if (showOptionsMenu) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsMenu = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1C1D22)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = currentTrack?.title ?: "Options",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RadiusLg)
                        .clickable {
                            showOptionsMenu = false
                            onOpenEqualizer()
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFFE50914))
                    Text(text = "Audio Equalizer & DSP", color = Color.White, fontSize = 15.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RadiusLg)
                        .clickable {
                            showOptionsMenu = false
                            onOpenLyrics()
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFFE50914))
                    Text(text = "Full Synchronized Lyrics", color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun NowPlayingLiveLyricsRow(
    progressFlow: StateFlow<PlaybackProgress>,
    lyrics: List<LyricLine>,
    onOpenLyrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressState by progressFlow.collectAsState()
    val view = LocalView.current
    val activeIdx = progressState.activeLyricIndex
    val liveLyricSnippet = remember(lyrics, activeIdx, progressState.currentLyricText, progressState.positionMs) {
        val current = progressState.currentLyricText
        if (!current.isNullOrBlank()) {
            current
        } else if (lyrics.isNotEmpty()) {
            if (activeIdx >= 0 && activeIdx < lyrics.size) {
                lyrics[activeIdx].text
            } else if (progressState.positionMs < (lyrics.firstOrNull()?.timeMs ?: 0L)) {
                "♪ ♪ ♪"
            } else {
                lyrics.lastOrNull()?.text ?: "♪ ♪ ♪"
            }
        } else {
            "Lyrics not available"
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RadiusFull)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onOpenLyrics()
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        AnimatedContent(
            targetState = liveLyricSnippet,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + slideInVertically { it / 3 })
                    .togetherWith(fadeOut(animationSpec = tween(180)) + slideOutVertically { -it / 3 })
            },
            modifier = Modifier.weight(1f, fill = false),
            label = "live_lyric_anim"
        ) { targetSnippet ->
            Text(
                text = targetSnippet,
                color = if (lyrics.isNotEmpty()) Color.White.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.5f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View Lyrics",
            tint = Color.White.copy(alpha = 0.65f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun NowPlayingScrubberBar(
    progressFlow: StateFlow<PlaybackProgress>,
    fallbackDurationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val progressState by progressFlow.collectAsState()
    val durationMs = if (progressState.durationMs > 0) progressState.durationMs else fallbackDurationMs
    val positionMs = progressState.positionMs.coerceIn(0L, durationMs.coerceAtLeast(0L))

    val elapsedSeconds = positionMs / 1000
    val elapsedText = remember(elapsedSeconds) {
        val elapsedMin = elapsedSeconds / 60
        val elapsedSec = elapsedSeconds % 60
        String.format("%d:%02d", elapsedMin, elapsedSec)
    }

    val remainingSeconds = (durationMs - positionMs).coerceAtLeast(0L) / 1000
    val remainingText = remember(remainingSeconds) {
        val remMin = remainingSeconds / 60
        val remSec = remainingSeconds % 60
        String.format("-%d:%02d", remMin, remSec)
    }

    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragSliderValue by remember { mutableFloatStateOf(0f) }

    val currentProgressFraction = if (durationMs > 0) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val sliderProgress = if (isDraggingSlider) dragSliderValue else currentProgressFraction

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = sliderProgress,
            onValueChange = { newValue ->
                isDraggingSlider = true
                dragSliderValue = newValue
            },
            onValueChangeFinished = {
                isDraggingSlider = false
                val targetMs = (dragSliderValue * durationMs).toLong()
                onSeek(targetMs)
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White.copy(alpha = 0.85f),
                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = elapsedText,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = remainingText,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
