package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.RotaryLoudnessKnob
import com.ipodmodern.audio.ui.components.SleekCard
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
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.absoluteValue

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
    val coroutineScope = rememberCoroutineScope()

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SleekIconButton(
                icon = Icons.Default.KeyboardArrowDown,
                onClick = onBackClick,
                size = 38.dp,
                iconSize = 22.dp,
                contentDescription = "Collapse"
            )

            Text(
                text = "NOW PLAYING",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            SleekIconButton(
                icon = Icons.Default.MoreVert,
                onClick = { onOpenLyrics() },
                size = 38.dp,
                iconSize = 20.dp,
                contentDescription = "More Options"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Hardware-Accelerated 120fps Artwork Pager Deck
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) { page ->
            val pageTrack = queue.getOrNull(page)
            val artworkFile = remember(pageTrack?.artworkUri) {
                pageTrack?.artworkUri?.let { File(it) }
            }

            val pageOffset by remember(pagerState) {
                derivedStateOf {
                    ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val absOffset = pageOffset.absoluteValue
                        scaleX = 1f - (absOffset * 0.12f).coerceIn(0f, 0.25f)
                        scaleY = 1f - (absOffset * 0.12f).coerceIn(0f, 0.25f)
                        alpha = 1f - (absOffset * 0.4f).coerceIn(0f, 0.7f)
                        rotationY = pageOffset * -15f
                        transformOrigin = TransformOrigin(
                            pivotFractionX = if (pageOffset < 0) 1f else 0f,
                            pivotFractionY = 0.5f
                        )
                    }
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
                        modifier = Modifier.size(64.dp)
                    )
                }

                // Subtle mini-waveform overlay at bottom of album artwork card
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(ObsidianBg.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    WaveformVisualizer(
                        progressPercent = progress,
                        onSeek = { seekPct ->
                            val targetMs = (seekPct * progressState.durationMs).toLong()
                            playerViewModel.seekTo(targetMs)
                        },
                        height = 28.dp,
                        barCount = 36
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Track Info + Heart Favorite Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentTrack?.title ?: "No Track Playing",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = currentTrack?.artist ?: "Aether Lossless Engine",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Favorite Button
            Icon(
                imageVector = if (isCurrentFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isCurrentFav) MintAccent else TextMuted,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        playerViewModel.toggleFavorite()
                    }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Progress Seek Scrubber
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
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
                        .height(4.dp)
                        .clip(RadiusFull)
                        .background(ObsidianTrackBg)
                )

                // Active Mint Track
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .clip(RadiusFull)
                        .background(MintAccent)
                )

                // White Thumb Dot
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            // Time Readouts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = elapsedText, color = TextMuted, fontSize = 11.sp)
                Text(text = totalText, color = TextMuted, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Center Feature: Rotary Loudness Knob or Full-Bleed Waveform Deck
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.0f)
                .clickable {
                    showWaveformDeck = !showWaveformDeck
                },
            contentAlignment = Alignment.Center
        ) {
            if (!showWaveformDeck) {
                // Rotary Loudness Dial with live percentage
                RotaryLoudnessKnob(
                    volumePercent = uiState.volume,
                    onVolumeChanged = { newVol ->
                        playerViewModel.setVolume(newVol)
                    },
                    size = 150.dp
                )
            } else {
                // Large Waveform Visualizer Deck
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RadiusLg)
                        .background(ObsidianSurface)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DYNAMIC WAVEFORM",
                        color = MintAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    WaveformVisualizer(
                        progressPercent = progress,
                        onSeek = { seekPct ->
                            val targetMs = (seekPct * progressState.durationMs).toLong()
                            playerViewModel.seekTo(targetMs)
                        },
                        height = 64.dp,
                        barCount = 44
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Playback Transport Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Equalizer Button
            SleekIconButton(
                icon = Icons.Default.Equalizer,
                onClick = onOpenEqualizer,
                size = 44.dp,
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

            // Hero Mint Play / Pause Button
            SleekPlayButton(
                isPlaying = uiState.isPlaying,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    playerViewModel.togglePlayPause()
                },
                size = 64.dp,
                iconSize = 26.dp
            )

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
                size = 44.dp,
                iconSize = 22.dp,
                contentDescription = "Repeat"
            )
        }
    }
}
