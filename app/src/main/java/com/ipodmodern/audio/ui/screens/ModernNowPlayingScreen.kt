package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.AddToPlaylistSheet
import com.ipodmodern.audio.ui.components.AmbientBackground
import com.ipodmodern.audio.ui.components.CircularPlayer
import com.ipodmodern.audio.ui.theme.LocalThemePalette
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusXl
import com.ipodmodern.audio.ui.viewmodel.PlayerViewModel
import java.io.File
import java.util.Locale

/**
 * ModernNowPlayingScreen implements PRD Sections 17-21:
 * - Full-bleed ambient glow influenced by current artwork
 * - Large tactile album artwork container with spring scale feedback
 * - Bold metadata hierarchy with instant favorite heart toggle
 * - PRD Section 18 Concentric Glowing Circular Player with rotational seeking
 * - Micro-timeline 3.dp scrubber with elapsed & remaining timestamps
 * - Tactile playback controls: Previous, Next, Shuffle, Repeat, Lyrics, Equalizer, Queue
 */
@Composable
fun ModernNowPlayingScreen(
    playerViewModel: PlayerViewModel,
    onBackClick: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val uiState by playerViewModel.uiState.collectAsState()
    val progressState by playerViewModel.playbackProgress.collectAsState()
    val view = LocalView.current
    val context = LocalContext.current

    val track = uiState.currentTrack
    val isPlaying = uiState.isPlaying
    val isShuffle = uiState.isShuffle
    val repeatMode = uiState.repeatMode
    val isFavorite = track?.let { uiState.favoriteTrackIds.contains(it.id) } == true

    var isOptionsSheetOpen by remember { mutableStateOf(false) }

    val artworkFile = remember(track?.artworkUri) {
        track?.artworkUri?.let { File(it) }
    }
    val artworkRequest = remember(artworkFile) {
        artworkFile?.let {
            ImageRequest.Builder(context)
                .data(it)
                .size(600)
                .crossfade(true)
                .build()
        }
    }

    // Dynamic scale for album art on play/pause
    val artScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.93f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "art_scale_anim"
    )

    // Position and duration calculation
    val durationMs = if (progressState.durationMs > 0) progressState.durationMs else (track?.durationMs ?: 0L)
    val positionMs = progressState.positionMs.coerceIn(0L, durationMs.coerceAtLeast(1L))
    val progressFraction = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val scrollState = rememberScrollState()

    AmbientBackground(
        modifier = modifier,
        ambientColor = palette.accent,
        ambientAlpha = 0.24f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dismiss / Back button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(palette.surfaceElevated)
                        .border(1.dp, palette.borderSubtle, CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onBackClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dismiss",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Center Title with Neon Status Dot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) palette.accent else palette.textMuted)
                    )
                    Text(
                        text = "NOW PLAYING",
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp
                    )
                }

                // Queue Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(palette.surfaceElevated)
                        .border(1.dp, palette.borderSubtle, CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onOpenQueue()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Queue",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 2. Hero Album Artwork Presentation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.0f)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                // Soft Ambient Glow behind artwork
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.92f)
                        .graphicsLayer {
                            scaleX = artScale
                            scaleY = artScale
                        }
                        .shadow(32.dp, RadiusXl, spotColor = palette.accentGlow)
                )

                // Main Artwork Card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = artScale
                            scaleY = artScale
                        }
                        .clip(RadiusXl)
                        .background(palette.surfaceElevated)
                        .border(1.dp, palette.borderSubtle, RadiusXl),
                    contentAlignment = Alignment.Center
                ) {
                    if (artworkRequest != null) {
                        AsyncImage(
                            model = artworkRequest,
                            contentDescription = track?.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = palette.textMuted,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    // Subtle luxury gradient sheen over artwork
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.08f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.25f)
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Track Metadata & Favorite Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track?.title ?: "Select a song",
                        color = palette.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = track?.artist?.ifBlank { "Unknown Artist" } ?: "TuneHive Player",
                        color = palette.textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite Heart Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            track?.let { playerViewModel.toggleFavorite(it.id) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isFavorite,
                        transitionSpec = {
                            scaleIn(initialScale = 0.7f) + fadeIn() togetherWith scaleOut(targetScale = 0.7f) + fadeOut()
                        },
                        label = "heart_anim"
                    ) { fav ->
                        Icon(
                            imageVector = if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (fav) palette.accent else palette.textSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Sleek 3.dp Micro-Timeline Scrubber (PRD Section 21)
            NowPlayingTimelineScrubber(
                durationMs = durationMs,
                positionMs = positionMs,
                onSeek = { targetMs ->
                    playerViewModel.seekTo(targetMs)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Centerpiece: PRD Section 18 Concentric Glowing Circular Player & Skip Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Previous Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(palette.surfaceElevated)
                        .border(1.dp, palette.borderSubtle, CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            playerViewModel.prevTrack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Flagship 6-Layer Concentric Glowing Circular Player
                CircularPlayer(
                    isPlaying = isPlaying,
                    progressFraction = progressFraction,
                    onPlayPauseClick = {
                        playerViewModel.togglePlayPause()
                    },
                    onSeekFraction = { fraction ->
                        val seekTarget = (fraction * durationMs).toLong()
                        playerViewModel.seekTo(seekTarget)
                    },
                    size = 190.dp,
                    buttonSize = 84.dp
                )

                // Skip Next Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(palette.surfaceElevated)
                        .border(1.dp, palette.borderSubtle, CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            playerViewModel.nextTrack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. Secondary Tactical Controls Dock: Shuffle, Repeat, Lyrics, Equalizer, Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Toggle
                Box(
                    modifier = Modifier
                        .size(44.dp)
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
                        tint = if (isShuffle) palette.accent else palette.textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Repeat Mode Toggle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            playerViewModel.toggleRepeat()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (repeatMode == 2) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (repeatMode != 0) palette.accent else palette.textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Synced Lyrics Shortcut
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onOpenLyrics()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lyrics,
                        contentDescription = "Lyrics",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Equalizer Shortcut
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onOpenEqualizer()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Equalizer",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // More Options Menu
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            isOptionsSheetOpen = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Add to Playlist Sheet
    if (isOptionsSheetOpen && track != null) {
        AddToPlaylistSheet(
            track = track,
            playerViewModel = playerViewModel,
            onDismiss = { isOptionsSheetOpen = false }
        )
    }
}

/**
 * Isolated Sleek 3.dp Micro-Timeline Scrubber (PRD Section 21 & 3.1.3).
 */
@Composable
private fun NowPlayingTimelineScrubber(
    durationMs: Long,
    positionMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val currentFraction = if (durationMs > 0) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val displayFraction = if (isDragging) dragFraction else currentFraction

    val elapsedSeconds = if (isDragging) (dragFraction * durationMs / 1000).toLong() else positionMs / 1000
    val totalSeconds = durationMs / 1000
    val remainingSeconds = (totalSeconds - elapsedSeconds).coerceAtLeast(0)

    val elapsedText = String.format(Locale.getDefault(), "%d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)
    val remainingText = String.format(Locale.getDefault(), "-%d:%02d", remainingSeconds / 60, remainingSeconds % 60)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onSeek((fraction * durationMs).toLong())
                    }
                }
                .pointerInput(durationMs) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeek((dragFraction * durationMs).toLong())
                        },
                        onDragCancel = {
                            isDragging = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                val w = size.width
                val trackH = 3.dp.toPx()
                val cy = size.height / 2f
                val activeW = w * displayFraction

                // Inactive Background Track
                drawRoundRect(
                    color = palette.surfaceElevated,
                    topLeft = Offset(0f, cy - trackH / 2f),
                    size = Size(w, trackH),
                    cornerRadius = CornerRadius(trackH / 2f, trackH / 2f)
                )

                // Active Progress Track (Electric Lime)
                drawRoundRect(
                    color = palette.accent,
                    topLeft = Offset(0f, cy - trackH / 2f),
                    size = Size(activeW, trackH),
                    cornerRadius = CornerRadius(trackH / 2f, trackH / 2f)
                )

                // Thumb Dot
                val thumbR = if (isDragging) 6.dp.toPx() else 4.dp.toPx()
                drawCircle(
                    color = palette.accent,
                    radius = thumbR,
                    center = Offset(activeW, cy)
                )
                drawCircle(
                    color = Color.White,
                    radius = thumbR * 0.5f,
                    center = Offset(activeW, cy)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = elapsedText,
                color = palette.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = remainingText,
                color = palette.textMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
