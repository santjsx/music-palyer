package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.RotaryLoudnessKnob
import com.ipodmodern.audio.ui.components.SleekIconButton
import com.ipodmodern.audio.ui.components.SleekPlayButton
import com.ipodmodern.audio.ui.components.WaveformVisualizer
import com.ipodmodern.audio.ui.theme.MintAccent
import com.ipodmodern.audio.ui.theme.MintGlow
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.theme.ObsidianBorder
import com.ipodmodern.audio.ui.theme.ObsidianElevated
import com.ipodmodern.audio.ui.theme.ObsidianSurface
import com.ipodmodern.audio.ui.theme.ObsidianTrackBg
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusMd
import com.ipodmodern.audio.ui.theme.RadiusXl
import com.ipodmodern.audio.ui.theme.TextMuted
import com.ipodmodern.audio.ui.theme.TextPrimary
import com.ipodmodern.audio.ui.theme.TextSecondary
import com.ipodmodern.audio.ui.viewmodel.PlayerViewModel
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
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
    val progressState by playerViewModel.playbackProgress.collectAsState()
    val view = LocalView.current

    val currentTrack = uiState.currentTrack
    val queue = uiState.allTracks.ifEmpty { listOfNotNull(currentTrack) }

    // Toggle between Rotary Knob mode and Waveform Mode
    var showWaveformDeck by remember { mutableStateOf(false) }

    val initialIndex = remember(currentTrack, queue) {
        val found = queue.indexOfFirst { it.id == currentTrack?.id }
        if (found >= 0) found else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { queue.size.coerceAtLeast(1) }
    )

    // Sync pager when active track changes externally
    LaunchedEffect(currentTrack?.id) {
        val target = queue.indexOfFirst { it.id == currentTrack?.id }
        if (target >= 0 && target != pagerState.currentPage) {
            pagerState.animateScrollToPage(target)
        }
    }

    // Trigger track play on swipe settle
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settledPage ->
            if (settledPage in queue.indices) {
                val targetTrack = queue[settledPage]
                if (targetTrack.id != uiState.currentTrack?.id) {
                    playerViewModel.playTrack(targetTrack)
                }
            }
        }
    }

    val progress = if (progressState.durationMs > 0) {
        (progressState.positionMs.toFloat() / progressState.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val posMin = (progressState.positionMs / 1000) / 60
    val posSec = (progressState.positionMs / 1000) % 60
    val durMin = (progressState.durationMs / 1000) / 60
    val durSec = (progressState.durationMs / 1000) % 60
    val elapsedText = String.format("%d:%02d", posMin, posSec)
    val totalText = String.format("%d:%02d", durMin, durSec)

    val isCurrentFav = currentTrack?.let { uiState.favoriteTrackIds.contains(it.id) } == true

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MintGlow.copy(alpha = 0.08f),
                        ObsidianBg,
                        ObsidianBg
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SleekIconButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    onClick = onBackClick,
                    size = 40.dp,
                    iconSize = 24.dp,
                    contentDescription = "Collapse"
                )

                Text(
                    text = "NOW PLAYING",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                SleekIconButton(
                    icon = Icons.Default.MoreVert,
                    onClick = { onOpenLyrics() },
                    size = 40.dp,
                    iconSize = 20.dp,
                    contentDescription = "More Options"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Crisp 1:1 Aspect Ratio Artwork Pager Deck
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                pageSpacing = 24.dp,
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) { page ->
                val pageTrack = queue.getOrNull(page)
                val artworkFile = remember(pageTrack?.artworkUri) {
                    pageTrack?.artworkUri?.let { File(it) }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RadiusXl)
                        .background(ObsidianSurface)
                        .border(1.dp, ObsidianBorder, RadiusXl),
                    contentAlignment = Alignment.Center
                ) {
                    if (artworkFile != null && artworkFile.exists()) {
                        AsyncImage(
                            model = artworkFile,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MintAccent,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Track Info + Heart Favorite Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = currentTrack?.title ?: "No Track Playing",
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "track_title_anim"
                    ) { title ->
                        Text(
                            text = title,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentTrack?.artist ?: "Aether Lossless Engine",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentFav) MintAccent.copy(alpha = 0.15f) else ObsidianElevated)
                        .border(1.dp, if (isCurrentFav) MintAccent.copy(alpha = 0.3f) else ObsidianBorder, CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            playerViewModel.toggleFavorite()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCurrentFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isCurrentFav) MintAccent else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Audiophile Progress Seek Scrubber
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .pointerInput(progressState.durationMs) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val widthPx = size.width.toFloat()
                                if (widthPx > 0) {
                                    val pct = (change.position.x / widthPx).coerceIn(0f, 1f)
                                    val targetMs = (pct * progressState.durationMs).toLong()
                                    playerViewModel.seekTo(targetMs)
                                }
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Inactive Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RadiusFull)
                            .background(ObsidianTrackBg)
                    )

                    // Active Mint Glow Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(5.dp)
                            .clip(RadiusFull)
                            .background(MintAccent)
                    )

                    // Audiophile Precision Thumb (Outer Mint Ring + Inner White Center)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(28.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(MintAccent)
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }
                }

                // Time Readouts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = elapsedText,
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = totalText,
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Center Feature: Swiss Rotary Loudness Knob or Full-Bleed Waveform Deck
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clickable {
                        showWaveformDeck = !showWaveformDeck
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!showWaveformDeck) {
                    // Precision Rotary Loudness Dial with live percentage
                    RotaryLoudnessKnob(
                        volumePercent = uiState.volume,
                        onVolumeChanged = { newVol ->
                            playerViewModel.setVolume(newVol)
                        },
                        size = 140.dp
                    )
                } else {
                    // Large Waveform Visualizer Deck
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RadiusLg)
                            .background(ObsidianSurface)
                            .border(1.dp, ObsidianBorder, RadiusLg)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DYNAMIC WAVEFORM • TAP TO SCRUB",
                            color = MintAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        WaveformVisualizer(
                            progressPercent = progress,
                            onSeek = { seekPct ->
                                val targetMs = (seekPct * progressState.durationMs).toLong()
                                playerViewModel.seekTo(targetMs)
                            },
                            height = 54.dp,
                            barCount = 42
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Playback Transport Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Equalizer Button
                SleekIconButton(
                    icon = Icons.Default.Equalizer,
                    onClick = onOpenEqualizer,
                    size = 46.dp,
                    iconSize = 22.dp,
                    contentDescription = "Equalizer"
                )

                // Previous Button
                SleekIconButton(
                    icon = Icons.Default.SkipPrevious,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        playerViewModel.prevTrack()
                    },
                    size = 48.dp,
                    iconSize = 26.dp,
                    contentDescription = "Previous"
                )

                // Hero Mint Play / Pause Button with Glow
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(MintAccent)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            playerViewModel.togglePlayPause()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        tint = ObsidianBg,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Next Button
                SleekIconButton(
                    icon = Icons.Default.SkipNext,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        playerViewModel.nextTrack()
                    },
                    size = 48.dp,
                    iconSize = 26.dp,
                    contentDescription = "Next"
                )

                // Repeat / Shuffle Action
                SleekIconButton(
                    icon = if (uiState.isShuffle) Icons.Default.Shuffle else when (uiState.repeatMode) {
                        2 -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        playerViewModel.toggleRepeat()
                    },
                    tint = if (uiState.repeatMode > 0 || uiState.isShuffle) MintAccent else TextSecondary,
                    size = 46.dp,
                    iconSize = 22.dp,
                    contentDescription = "Repeat"
                )
            }
        }
    }
}
