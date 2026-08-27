package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Headset
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
import androidx.compose.material.icons.filled.Tune
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
import com.ipodmodern.audio.ui.components.SkeuomorphicVolumeKnob
import com.ipodmodern.audio.ui.components.SleekCard
import com.ipodmodern.audio.ui.components.SleekIconButton
import com.ipodmodern.audio.ui.components.WaveformVisualizer
import com.ipodmodern.audio.ui.theme.MintAccent
import com.ipodmodern.audio.ui.theme.MintGlow
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.theme.ObsidianBorder
import com.ipodmodern.audio.ui.theme.ObsidianElevated
import com.ipodmodern.audio.ui.theme.ObsidianPill
import com.ipodmodern.audio.ui.theme.ObsidianSurface
import com.ipodmodern.audio.ui.theme.ObsidianTrackBg
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusMd
import com.ipodmodern.audio.ui.theme.RadiusSm
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

    // Center Console Display Mode: 0 = Rotary Knob, 1 = Live Waveform Spectrum
    var centerModeIndex by remember { mutableStateOf(0) }

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

    val durationMs = if (progressState.durationMs > 0) progressState.durationMs else (currentTrack?.durationMs ?: 0L)
    val positionMs = progressState.positionMs.coerceIn(0L, durationMs.coerceAtLeast(0L))

    val progress = if (durationMs > 0) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val posMin = (positionMs / 1000) / 60
    val posSec = (positionMs / 1000) % 60
    val elapsedText = String.format("%02d:%02d", posMin, posSec)

    val remainingMs = (durationMs - positionMs).coerceAtLeast(0L)
    val remMin = (remainingMs / 1000) / 60
    val remSec = (remainingMs / 1000) % 60
    val remainingText = String.format("-%02d:%02d", remMin, remSec)

    val isCurrentFav = currentTrack?.let { uiState.favoriteTrackIds.contains(it.id) } == true

    // Audio format spec info
    val formatTag = remember(currentTrack) {
        when {
            (currentTrack?.sampleRate ?: 0) > 48000 -> "HI-RES LOSSLESS • 24-BIT"
            (currentTrack?.sampleRate ?: 0) > 44100 -> "96 kHz • 24-BIT FLAC"
            else -> "320 KBPS • AETHER CORE"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MintGlow.copy(alpha = 0.12f),
                        ObsidianBg.copy(alpha = 0.95f),
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
                .padding(bottom = 36.dp),
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

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM LIBRARY",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentTrack?.album?.takeIf { it.isNotBlank() } ?: "Audiophile Deck",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                SleekIconButton(
                    icon = Icons.Default.MoreVert,
                    onClick = { onOpenLyrics() },
                    size = 40.dp,
                    iconSize = 20.dp,
                    contentDescription = "More Options"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Luxury Floating 3D Artwork Card with Format Badge
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
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
                        // Procedural Audiophile Gradient Background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(MintGlow.copy(alpha = 0.3f), ObsidianSurface)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = MintAccent,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }

                    // Top-Left Floating Hi-Res Lossless Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RadiusFull)
                            .background(ObsidianBg.copy(alpha = 0.75f))
                            .border(1.dp, MintGlow, RadiusFull)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MintAccent)
                            )
                            Text(
                                text = formatTag,
                                color = TextPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Track Title, Artist & Heart Favorite Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = currentTrack?.title ?: "Select Track to Play",
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
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = currentTrack?.artist ?: "Aether Lossless Engine",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite Heart Action
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentFav) MintAccent.copy(alpha = 0.15f) else ObsidianElevated)
                        .border(1.dp, if (isCurrentFav) MintAccent.copy(alpha = 0.35f) else ObsidianBorder, CircleShape)
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

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Capacitive Glowing Rail Seekbar with Dual-Ring Thumb
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .pointerInput(durationMs) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val widthPx = size.width.toFloat()
                                if (widthPx > 0) {
                                    val pct = (change.position.x / widthPx).coerceIn(0f, 1f)
                                    val targetMs = (pct * durationMs).toLong()
                                    playerViewModel.seekTo(targetMs)
                                }
                            }
                        }
                        .pointerInput(durationMs) {
                            detectTapGestures { offset ->
                                val widthPx = size.width.toFloat()
                                if (widthPx > 0) {
                                    val pct = (offset.x / widthPx).coerceIn(0f, 1f)
                                    val targetMs = (pct * durationMs).toLong()
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.seekTo(targetMs)
                                }
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Inactive Track Base
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RadiusFull)
                            .background(ObsidianTrackBg)
                    )

                    // Active Glowing Mint Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(5.dp)
                            .clip(RadiusFull)
                            .background(MintAccent)
                    )

                    // Precision Capacitive Thumb Dot (Outer Mint Ring + Inner White Center)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(30.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MintAccent)
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }
                }

                // Elapsed & Remaining Monospaced Time Readouts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = elapsedText,
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = remainingText,
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Dual-Mode Centerpiece Console: Mode Switcher Chip + [Swiss Knob / Live Spectrum]
            Row(
                modifier = Modifier
                    .clip(RadiusFull)
                    .background(ObsidianPill)
                    .border(1.dp, ObsidianBorder, RadiusFull)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RadiusFull)
                        .background(if (centerModeIndex == 0) MintAccent else Color.Transparent)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            centerModeIndex = 0
                        }
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "LOUDNESS DIAL",
                        color = if (centerModeIndex == 0) ObsidianBg else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RadiusFull)
                        .background(if (centerModeIndex == 1) MintAccent else Color.Transparent)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            centerModeIndex = 1
                        }
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "LIVE SPECTRUM",
                        color = if (centerModeIndex == 1) ObsidianBg else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Console Container (155dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp),
                contentAlignment = Alignment.Center
            ) {
                if (centerModeIndex == 0) {
                    // Swiss Hi-Fi Master 300° Skeuomorphic Loudness Knob
                    SkeuomorphicVolumeKnob(
                        volumePercent = uiState.volume,
                        onVolumeChanged = { newVol ->
                            playerViewModel.setVolume(newVol)
                        },
                        size = 145.dp,
                        accentColor = MintAccent
                    )
                } else {
                    // Dynamic FFT Spectrum Analyzer & Interactive Scrubber
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RadiusLg)
                            .background(ObsidianSurface)
                            .border(1.dp, ObsidianBorder, RadiusLg)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REAL-TIME PEAK METERS",
                                color = MintAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "${(progress * 100).toInt()}% PLAYED",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        WaveformVisualizer(
                            progressPercent = progress,
                            onSeek = { seekPct ->
                                val targetMs = (seekPct * durationMs).toLong()
                                playerViewModel.seekTo(targetMs)
                            },
                            height = 54.dp,
                            barCount = 44
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Master Reactor Playback Transport Deck
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Parametric Equalizer Shortcut
                SleekIconButton(
                    icon = Icons.Default.Equalizer,
                    onClick = onOpenEqualizer,
                    size = 46.dp,
                    iconSize = 22.dp,
                    contentDescription = "Equalizer"
                )

                // Previous Track Button
                SleekIconButton(
                    icon = Icons.Default.SkipPrevious,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        playerViewModel.prevTrack()
                    },
                    size = 50.dp,
                    iconSize = 28.dp,
                    contentDescription = "Previous"
                )

                // Master Reactor Play / Pause Button (70dp with Outer Pulsating Ring)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MintGlow)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
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
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Next Track Button
                SleekIconButton(
                    icon = Icons.Default.SkipNext,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        playerViewModel.nextTrack()
                    },
                    size = 50.dp,
                    iconSize = 28.dp,
                    contentDescription = "Next"
                )

                // Shuffle / Repeat Mode Switcher
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

            Spacer(modifier = Modifier.height(10.dp))

            // 7. Bottom Audiophile Utility Bar: Queue • Lyrics • DSP Quick Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RadiusFull)
                    .background(ObsidianSurface)
                    .border(1.dp, ObsidianBorder, RadiusFull)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Queue Shortcut with Track Count
                Row(
                    modifier = Modifier.clickable { onOpenQueue() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        tint = MintAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Queue (${uiState.currentTrackIndex}/${queue.size})",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(ObsidianBorder)
                )

                // Synchronized Lyrics Shortcut
                Row(
                    modifier = Modifier.clickable { onOpenLyrics() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Lyrics",
                        tint = MintAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Lyrics",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
