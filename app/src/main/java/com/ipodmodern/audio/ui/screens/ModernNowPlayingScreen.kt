package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.NeoBadge
import com.ipodmodern.audio.ui.components.NeoCard
import com.ipodmodern.audio.ui.components.NeoIconButton
import com.ipodmodern.audio.ui.theme.NeoBgDark
import com.ipodmodern.audio.ui.theme.NeoBlack
import com.ipodmodern.audio.ui.theme.NeoBlue
import com.ipodmodern.audio.ui.theme.NeoBorderThick
import com.ipodmodern.audio.ui.theme.NeoBorderWidth
import com.ipodmodern.audio.ui.theme.NeoGreen
import com.ipodmodern.audio.ui.theme.NeoPink
import com.ipodmodern.audio.ui.theme.NeoPurple
import com.ipodmodern.audio.ui.theme.NeoWhite
import com.ipodmodern.audio.ui.theme.NeoYellow
import com.ipodmodern.audio.ui.viewmodel.PlaybackProgress
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.math.abs

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ModernNowPlayingScreen(
    currentTrack: Track?,
    allTracks: List<Track>,
    currentTrackIndex: Int,
    isPlaying: Boolean,
    volumeLevel: Float,
    isShuffle: Boolean = false,
    repeatMode: Int = 0,
    isFavorite: Boolean = false,
    playbackProgressFlow: StateFlow<PlaybackProgress>? = null,
    onPlayPauseClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onPrevClick: () -> Unit = {},
    onTrackSelect: (Int) -> Unit = {},
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
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    val queue = if (allTracks.isNotEmpty()) allTracks else listOfNotNull(currentTrack)
    val activeIndex = if (currentTrackIndex > 0) (currentTrackIndex - 1).coerceIn(0, queue.size - 1) else 0

    val pagerState = rememberPagerState(
        initialPage = activeIndex,
        pageCount = { queue.size.coerceAtLeast(1) }
    )

    // Synchronize pager with external track changes (e.g. next/prev)
    LaunchedEffect(activeIndex) {
        if (pagerState.currentPage != activeIndex && activeIndex < pagerState.pageCount) {
            pagerState.animateScrollToPage(activeIndex)
        }
    }

    // Trigger track select when user settles on a new page via swipe
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settledPage ->
            if (settledPage != activeIndex && settledPage in queue.indices) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onTrackSelect(settledPage)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(NeoBgDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // MARK: - Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeoIconButton(
                icon = Icons.Default.KeyboardArrowDown,
                contentDescription = "Collapse",
                onClick = onCollapseClick,
                backgroundColor = NeoYellow,
                size = 44.dp
            )

            NeoBadge(
                text = "${(pagerState.currentPage + 1).coerceAtMost(queue.size)} / ${queue.size.coerceAtLeast(1)}",
                backgroundColor = NeoWhite
            )

            NeoIconButton(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                onClick = onToggleFavorite,
                backgroundColor = if (isFavorite) NeoPink else NeoWhite,
                tint = if (isFavorite) NeoWhite else NeoBlack,
                size = 44.dp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MARK: - 120fps Hardware-Accelerated 3D Cover Flow Pager
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 70.dp),
            pageSpacing = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
        ) { page ->
            val trackItem = queue.getOrNull(page) ?: return@HorizontalPager

            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val absOffset = abs(pageOffset)
            val isCurrentPage = page == pagerState.currentPage

            val scale = (1.0f - absOffset * 0.12f).coerceIn(0.78f, 1.0f)
            val rotationY = (pageOffset * -24f).coerceIn(-48f, 48f)
            val zIndexVal = 100f - absOffset * 10f

            NeoCoverCard(
                track = trackItem,
                isCenter = isCurrentPage,
                modifier = Modifier
                    .zIndex(zIndexVal)
                    .graphicsLayer {
                        this.scaleX = scale
                        this.scaleY = scale
                        this.rotationY = rotationY
                        this.cameraDistance = 14f * density
                        this.transformOrigin = TransformOrigin(0.5f, 0.5f)
                    }
                    .clickable {
                        if (page != pagerState.currentPage) {
                            coroutineScope.launch {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                pagerState.animateScrollToPage(page)
                            }
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Lossless Tag
        val badgeText = currentTrack?.badgeText ?: "24-BIT • 96kHz LOSSLESS"
        NeoBadge(
            text = badgeText,
            backgroundColor = NeoGreen,
            textColor = NeoWhite
        )

        Spacer(modifier = Modifier.height(12.dp))

        // MARK: - Decoupled Progress Scrubber (Only recomposes itself)
        NeoScrubberContainer(
            playbackProgressFlow = playbackProgressFlow,
            onSeekTo = onSeekTo
        )

        Spacer(modifier = Modifier.height(12.dp))

        // MARK: - Neo-Brutalist Transport Deck
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            NeoIconButton(
                icon = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                onClick = onToggleShuffle,
                backgroundColor = if (isShuffle) NeoPurple else NeoWhite,
                tint = if (isShuffle) NeoWhite else NeoBlack,
                size = 46.dp
            )

            // Previous
            NeoIconButton(
                icon = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                onClick = onPrevClick,
                backgroundColor = NeoWhite,
                size = 52.dp,
                iconSize = 26.dp
            )

            // Hero Play/Pause
            NeoIconButton(
                icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                onClick = onPlayPauseClick,
                backgroundColor = NeoYellow,
                size = 68.dp,
                iconSize = 36.dp,
                isCircle = true
            )

            // Next
            NeoIconButton(
                icon = Icons.Default.SkipNext,
                contentDescription = "Next",
                onClick = onNextClick,
                backgroundColor = NeoWhite,
                size = 52.dp,
                iconSize = 26.dp
            )

            // Repeat
            NeoIconButton(
                icon = Icons.Default.Repeat,
                contentDescription = "Repeat",
                onClick = onToggleRepeat,
                backgroundColor = if (repeatMode > 0) NeoBlue else NeoWhite,
                tint = if (repeatMode > 0) NeoWhite else NeoBlack,
                size = 46.dp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // MARK: - Volume Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                contentDescription = null,
                tint = NeoWhite,
                modifier = Modifier.size(20.dp)
            )

            Slider(
                value = volumeLevel,
                onValueChange = onVolumeChange,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = NeoYellow,
                    activeTrackColor = NeoYellow,
                    inactiveTrackColor = NeoWhite.copy(alpha = 0.3f)
                )
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = NeoWhite,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // MARK: - Bottom Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NeoCard(
                backgroundColor = NeoPurple,
                modifier = Modifier.weight(1.3f),
                onClick = onLyricsClick
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Subject,
                        contentDescription = "Lyrics",
                        tint = NeoBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "LYRICS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack
                    )
                }
            }

            NeoCard(
                backgroundColor = NeoPink,
                modifier = Modifier.weight(1f),
                onClick = onEqClick
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "EQ",
                        tint = NeoBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "10-EQ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 120fps Async-Image Neo-Brutalist 3D Cover Card.
 */
@Composable
fun NeoCoverCard(
    track: Track,
    isCenter: Boolean,
    modifier: Modifier = Modifier
) {
    val artworkFile = remember(track.artworkUri) {
        track.artworkUri?.let { File(it) }
    }

    NeoCard(
        backgroundColor = if (isCenter) NeoYellow else NeoWhite,
        borderColor = NeoBlack,
        borderWidth = NeoBorderThick,
        shadowOffset = if (isCenter) 6.dp else 3.dp,
        cornerRadius = 16.dp,
        modifier = modifier
            .width(220.dp)
            .height(290.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Async Artwork Loader with Coil (Background Thread Decoding)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(NeoBlack)
            ) {
                if (artworkFile != null && artworkFile.exists()) {
                    AsyncImage(
                        model = artworkFile,
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = NeoYellow,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
            }

            // Neo-Brutalist Card Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = NeoBorderWidth,
                        color = NeoBlack,
                        shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                    )
                    .background(if (isCenter) NeoYellow else NeoWhite)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = track.title.uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoBlack.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * High-Performance Decoupled Scrubber.
 */
@Composable
fun NeoScrubberContainer(
    playbackProgressFlow: StateFlow<PlaybackProgress>?,
    onSeekTo: (Long) -> Unit
) {
    val progressState = playbackProgressFlow?.collectAsState()?.value ?: PlaybackProgress()
    val pos = progressState.positionMs
    val dur = progressState.durationMs

    val progressFraction = if (dur > 0) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Slider(
            value = progressFraction,
            onValueChange = { frac ->
                onSeekTo((frac * dur).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = NeoYellow,
                activeTrackColor = NeoYellow,
                inactiveTrackColor = NeoWhite.copy(alpha = 0.3f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatNeoTime(pos),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = NeoWhite
            )
            Text(
                text = formatNeoTime(dur),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = NeoWhite
            )
        }
    }
}

private fun formatNeoTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
