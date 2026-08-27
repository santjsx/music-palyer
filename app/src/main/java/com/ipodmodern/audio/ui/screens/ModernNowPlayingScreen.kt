package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ipodmodern.audio.ui.theme.ModernThemeTokens
import com.ipodmodern.audio.ui.viewmodel.PlayerViewModel
import java.io.File
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern High-End Audiophile Player Screen inspired by luxury digital-analog players:
 * - Top Bar with Glass Back button, Floating Playlist Pill, and Menu Icon
 * - Circular Chronograph Vinyl Audio Wheel (Progress Arc, Speed Chips, +-10s Jumps, Spinning Artwork Core, Timestamp)
 * - Lower Vibrant Glassmorphic Audio Deck with 3-Line Synchronized Lyrics & High-Contrast Transport Controls
 */
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
    val palette = ModernThemeTokens.palette
    val view = LocalView.current

    val currentTrack = uiState.currentTrack
    val queue = uiState.allTracks.ifEmpty { listOfNotNull(currentTrack) }

    val durationMs = if (progressState.durationMs > 0) progressState.durationMs else (currentTrack?.durationMs ?: 0L)
    val positionMs = progressState.positionMs.coerceIn(0L, durationMs.coerceAtLeast(0L))

    val progress = if (durationMs > 0) {
        (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val posMin = (positionMs / 1000) / 60
    val posSec = (positionMs / 1000) % 60
    val elapsedText = String.format("%02d:%02d", posMin, posSec)

    val isCurrentFav = currentTrack?.let { uiState.favoriteTrackIds.contains(it.id) } == true

    // Artwork file resolution
    val artworkFile = remember(currentTrack?.artworkUri) {
        currentTrack?.artworkUri?.let { File(it) }
    }

    // Dynamic Lyrics Extraction (Previous, Active, Next)
    val lyrics = uiState.lyrics
    val activeIdx = progressState.activeLyricIndex
    val prevLyric = remember(lyrics, activeIdx) {
        if (lyrics.isNotEmpty() && activeIdx > 0 && activeIdx <= lyrics.size) {
            lyrics.getOrNull(activeIdx - 1)?.text
        } else null
    }
    val currentLyric = remember(lyrics, activeIdx, progressState.currentLyricText) {
        progressState.currentLyricText ?: lyrics.getOrNull(activeIdx.coerceAtLeast(0))?.text
    }
    val nextLyric = remember(lyrics, activeIdx) {
        if (lyrics.isNotEmpty() && activeIdx >= 0 && activeIdx + 1 < lyrics.size) {
            lyrics.getOrNull(activeIdx + 1)?.text
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF162036),
                        Color(0xFF0C101A),
                        Color(0xFF07090E)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP NAVIGATION HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleek Back Arrow
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onBackClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = 2.dp)
                    )
                }

                // Center Floating Pill: "Playlist: «My Favourite»" or Album Name
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isCurrentFav) "Playlist: «My Favourite»" else "Album: «${currentTrack?.album ?: "Library"}»",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right Options Menu (Opens Queue / Equalizer)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onOpenEqualizer()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. CIRCULAR CHRONOGRAPH VINYL WHEEL
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // Background Canvas with Tick Marks & White Progress Arc
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(durationMs) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val angleRad = atan2(change.position.y - centerY, change.position.x - centerX)
                                var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                                if (angleDeg < 0) angleDeg += 360f
                                val pct = (angleDeg / 360f).coerceIn(0f, 1f)
                                val targetMs = (pct * durationMs).toLong()
                                playerViewModel.seekTo(targetMs)
                            }
                        }
                        .pointerInput(durationMs) {
                            detectTapGestures { offset ->
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val angleRad = atan2(offset.y - centerY, offset.x - centerX)
                                var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                                if (angleDeg < 0) angleDeg += 360f
                                val pct = (angleDeg / 360f).coerceIn(0f, 1f)
                                val targetMs = (pct * durationMs).toLong()
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                playerViewModel.seekTo(targetMs)
                            }
                        }
                ) {
                    val canvasCenter = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.minDimension / 2f - 10.dp.toPx()
                    val vinylRadius = outerRadius - 14.dp.toPx()

                    // Draw 72 Perimeter Ticks
                    val tickCount = 72
                    for (i in 0 until tickCount) {
                        val tickAngleDeg = (i.toFloat() / tickCount) * 360f - 90f
                        val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())
                        val isMajor = i % 6 == 0

                        val tickInnerR = outerRadius - (if (isMajor) 9.dp.toPx() else 5.dp.toPx())
                        val tickOuterR = outerRadius

                        val startX = canvasCenter.x + (tickInnerR * cos(tickAngleRad)).toFloat()
                        val startY = canvasCenter.y + (tickInnerR * sin(tickAngleRad)).toFloat()
                        val endX = canvasCenter.x + (tickOuterR * cos(tickAngleRad)).toFloat()
                        val endY = canvasCenter.y + (tickOuterR * sin(tickAngleRad)).toFloat()

                        drawLine(
                            color = Color.White.copy(alpha = if (isMajor) 0.35f else 0.12f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = if (isMajor) 1.8.dp.toPx() else 1.dp.toPx()
                        )
                    }

                    // Draw Dark Machined Vinyl Disc Body
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF22252C),
                                Color(0xFF14161C),
                                Color(0xFF0B0C10)
                            ),
                            center = canvasCenter,
                            radius = vinylRadius
                        ),
                        radius = vinylRadius,
                        center = canvasCenter
                    )

                    // Concentric Grooves on Vinyl
                    drawCircle(
                        color = Color.White.copy(alpha = 0.04f),
                        radius = vinylRadius * 0.78f,
                        center = canvasCenter,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.04f),
                        radius = vinylRadius * 0.58f,
                        center = canvasCenter,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Draw Sweeping Solid White Progress Arc along the perimeter
                    val arcSweepAngle = 360f * progress
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = arcSweepAngle,
                        useCenter = false,
                        topLeft = Offset(canvasCenter.x - outerRadius, canvasCenter.y - outerRadius),
                        size = Size(outerRadius * 2, outerRadius * 2),
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner Speed Chips (x0.5, x1, x2) at top
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentSpeed = uiState.playbackSpeed
                    listOf(0.5f to "x0.5", 1.0f to "x1", 2.0f to "x2").forEach { (spd, label) ->
                        Text(
                            text = label,
                            color = if (currentSpeed == spd) Color.White else Color.White.copy(alpha = 0.45f),
                            fontSize = 10.sp,
                            fontWeight = if (currentSpeed == spd) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.clickable {
                                playerViewModel.setPlaybackSpeed(spd)
                            }
                        )
                    }
                }

                // Center Row: [-10s Jump] • [Center Album Art Core] • [+10s Jump]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // -10s Rewind Button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { playerViewModel.seekBackward10s() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⟲10",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Center Circular Album Artwork Core (~76dp)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape),
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
                                tint = palette.accent,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // +10s Fast Forward Button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { playerViewModel.seekForward10s() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "10⟳",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Timestamp readout (01:12) & triple dots at bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = elapsedText,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "•••",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 8.sp,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. THE VIBRANT GLASSMORPHIС AUDIO DECK (Bottom Floating Card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF256BFE),
                                Color(0xFF1852DC),
                                Color(0xFF0F3BB0)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 3.1 Synchronized Lyrics Showcase (3 Lines)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenLyrics() }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Previous Lyric Line (Translucent)
                        Text(
                            text = prevLyric ?: "•••",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Current Active Lyric Line (Large Bold Pure White)
                        AnimatedContent(
                            targetState = currentLyric ?: currentTrack?.title ?: "Playing Audio",
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "lyric_text_anim"
                        ) { activeText ->
                            Text(
                                text = activeText,
                                color = Color.White,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Next Lyric Line (Translucent)
                        Text(
                            text = nextLyric ?: currentTrack?.artist ?: "Lossless Engine",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3.2 Track Info Row: Overlapping Circular Artwork Discs + Title/Artist
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Overlapping Circular Artworks
                        Box(modifier = Modifier.size(48.dp, 36.dp)) {
                            // Back art circle
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            ) {
                                if (artworkFile != null && artworkFile.exists()) {
                                    AsyncImage(
                                        model = artworkFile,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            // Front art circle (Offset by 14dp)
                            Box(
                                modifier = Modifier
                                    .offset(x = 14.dp)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .border(1.5.dp, Color.White, CircleShape)
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
                                        tint = palette.accent,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Title & Artist Text
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentTrack?.title ?: "Select Track",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentTrack?.artist ?: "Unknown Artist",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3.3 Full Transport Controls Row: Repeat • Prev • Play/Pause • Next • Favorite
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Repeat / Repeat 1 Switcher
                        Icon(
                            imageVector = when (uiState.repeatMode) {
                                2 -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (uiState.repeatMode > 0) Color.White else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.toggleRepeat()
                                }
                        )

                        // Previous Track
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier
                                .size(34.dp)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.prevTrack()
                                }
                        )

                        // Center Solid White Play / Pause Circle (56dp)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.togglePlayPause()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                tint = Color(0xFF0F3BB0),
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        // Next Track
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier
                                .size(34.dp)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.nextTrack()
                                }
                        )

                        // Favorite Heart
                        Icon(
                            imageVector = if (isCurrentFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isCurrentFav) Color(0xFFFF4B72) else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.toggleFavorite()
                                }
                        )
                    }
                }
            }
        }
    }
}
