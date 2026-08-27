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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
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
import androidx.compose.ui.draw.blur
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
 * Pixel-Perfect Luxury Audiophile Player Screen matching the reference design:
 * - Dynamic Blurred Album Art Backdrop with Deep Atmospheric Lighting
 * - Balanced Top Navigation Bar with Back Button, Auto-Fit Pill Badge, and Menu
 * - Upper Stage: Centered 280dp Circular Chronograph Wheel with ambient glow, precision ticks, flat progress arc, speed chips, and inside jump controls
 * - Lower Stage: Full-Span Vibrant Glassmorphic Audio Deck anchored to the bottom (No empty void at bottom of screen!)
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
    val hasRealLyrics = lyrics.isNotEmpty()

    val prevLyric = remember(lyrics, activeIdx) {
        if (hasRealLyrics && activeIdx > 0 && activeIdx <= lyrics.size) {
            lyrics.getOrNull(activeIdx - 1)?.text
        } else null
    }
    val currentLyric = remember(lyrics, activeIdx, progressState.currentLyricText) {
        if (hasRealLyrics) {
            progressState.currentLyricText ?: lyrics.getOrNull(activeIdx.coerceAtLeast(0))?.text
        } else null
    }
    val nextLyric = remember(lyrics, activeIdx) {
        if (hasRealLyrics && activeIdx >= 0 && activeIdx + 1 < lyrics.size) {
            lyrics.getOrNull(activeIdx + 1)?.text
        } else null
    }

    // Top pill title
    val pillTitle = remember(isCurrentFav, currentTrack) {
        if (isCurrentFav) {
            "Playlist: «My Favourite»"
        } else {
            val alb = currentTrack?.album
            if (!alb.isNullOrBlank()) "Album: «$alb»" else "Playing from Library"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
    ) {
        // LAYER 0: Ambient Blurred Album Art Backdrop
        if (artworkFile != null && artworkFile.exists()) {
            AsyncImage(
                model = artworkFile,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(45.dp)
            )
        }

        // LAYER 1: Deep Atmospheric Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F1B38).copy(alpha = 0.82f),
                            Color(0xFF070D1E).copy(alpha = 0.88f),
                            Color(0xFF03050A).copy(alpha = 0.94f)
                        )
                    )
                )
        )

        // LAYER 2: Foreground Structure (Header + Upper Wheel Stage + Anchored Bottom Deck)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. TOP NAVIGATION HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleek Back Arrow
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
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

                // Center Floating Pill (Auto-fit weighted)
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 10.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pillTitle,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                // Right Options Menu (Equalizer / Queue)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
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

            // 2. UPPER STAGE: Centered Circular Chronograph Vinyl Wheel (Weight = 1.15f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.15f)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Ambient Radial Glow Backdrop
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF2870FF).copy(alpha = 0.40f),
                                        Color(0xFF1440C0).copy(alpha = 0.18f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Canvas: Perimeter Ticks, Machined Vinyl Body, Concentric Grooves & White Progress Arc
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
                        val arcStrokeWidth = 7.dp.toPx()
                        val outerRadius = size.minDimension / 2f - (arcStrokeWidth / 2f)
                        val vinylRadius = outerRadius - 3.dp.toPx()

                        // Draw 72 Perimeter Ticks
                        val tickCount = 72
                        for (i in 0 until tickCount) {
                            val tickAngleDeg = (i.toFloat() / tickCount) * 360f - 90f
                            val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())
                            val isMajor = i % 6 == 0

                            val tickInnerR = outerRadius - (if (isMajor) 11.dp.toPx() else 6.dp.toPx())
                            val tickOuterR = outerRadius + 2.dp.toPx()

                            val startX = canvasCenter.x + (tickInnerR * cos(tickAngleRad)).toFloat()
                            val startY = canvasCenter.y + (tickInnerR * sin(tickAngleRad)).toFloat()
                            val endX = canvasCenter.x + (tickOuterR * cos(tickAngleRad)).toFloat()
                            val endY = canvasCenter.y + (tickOuterR * sin(tickAngleRad)).toFloat()

                            drawLine(
                                color = Color.White.copy(alpha = if (isMajor) 0.40f else 0.15f),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (isMajor) 1.8.dp.toPx() else 1.dp.toPx()
                            )
                        }

                        // Draw Solid Dark Machined Vinyl Disc Body
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1E2128),
                                    Color(0xFF121419),
                                    Color(0xFF0A0B0E)
                                ),
                                center = canvasCenter,
                                radius = vinylRadius
                            ),
                            radius = vinylRadius,
                            center = canvasCenter
                        )

                        // Concentric Grooves
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = vinylRadius * 0.76f,
                            center = canvasCenter,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = vinylRadius * 0.56f,
                            center = canvasCenter,
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Sweeping Solid White Progress Arc with flat precision ends
                        if (progress > 0.005f) {
                            val arcSweepAngle = 360f * progress
                            drawArc(
                                color = Color.White,
                                startAngle = -90f,
                                sweepAngle = arcSweepAngle,
                                useCenter = false,
                                topLeft = Offset(canvasCenter.x - outerRadius, canvasCenter.y - outerRadius),
                                size = Size(outerRadius * 2, outerRadius * 2),
                                style = Stroke(width = arcStrokeWidth, cap = StrokeCap.Butt)
                            )
                        }
                    }

                    // Speed Selector Chips [x0.5, x1, x2]
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentSpeed = uiState.playbackSpeed
                        listOf(0.5f to "x0.5", 1.0f to "x1", 2.0f to "x2").forEach { (spd, label) ->
                            val isSelected = currentSpeed == spd
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White.copy(alpha = 0.20f) else Color.Transparent)
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        playerViewModel.setPlaybackSpeed(spd)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.50f),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Symmetrical Interior Jump Buttons & Center Circular Artwork Core
                    Row(
                        modifier = Modifier
                            .width(174.dp)
                            .align(Alignment.Center),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // -10s Rewind Button
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.seekBackward10s()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⟲10",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Center Circular Album Artwork (~72dp)
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .border(1.5.dp, Color.White.copy(alpha = 0.30f), CircleShape),
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
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                playerViewModel.seekForward10s()
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "10⟳",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Monospaced Timestamp & Triple Dots
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = elapsedText,
                            color = Color.White.copy(alpha = 0.90f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "•••",
                            color = Color.White.copy(alpha = 0.35f),
                            fontSize = 8.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            // 3. LOWER STAGE: Full-Span Vibrant Glassmorphic Audio Deck (Anchored to Bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF256BFE),
                                Color(0xFF1650D8),
                                Color(0xFF0C36A8)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.22f),
                        RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 3.1 3-Line Synchronized Lyrics / Lossless Audio Showcase
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenLyrics() }
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (hasRealLyrics) {
                            Text(
                                text = prevLyric ?: "•••",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            AnimatedContent(
                                targetState = currentLyric ?: "Playing Audio",
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "lyric_text_anim"
                            ) { activeText ->
                                Text(
                                    text = activeText,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = nextLyric ?: "•••",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = "LOSSLESS HI-RES AUDIO",
                                color = Color.White.copy(alpha = 0.50f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Immerse in the Sound",
                                color = Color.White,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Aether Audiophile DSP Deck",
                                color = Color.White.copy(alpha = 0.50f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 3.2 Overlapping Circular Artwork Discs + Track Title & Artist
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Double Overlapping Circular Artworks
                        Box(modifier = Modifier.size(54.dp, 38.dp)) {
                            // Back disc
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
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
                            // Front disc (Offset by 16dp)
                            Box(
                                modifier = Modifier
                                    .offset(x = 16.dp)
                                    .size(36.dp)
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

                    // 3.3 Transport Controls: Repeat • Prev • Solid White Play/Pause • Next • Favorite
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
                            tint = if (uiState.repeatMode > 0) Color.White else Color.White.copy(alpha = 0.65f),
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
                                .size(36.dp)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.prevTrack()
                                }
                        )

                        // Center Solid Pure White Play/Pause Circle (58dp)
                        Box(
                            modifier = Modifier
                                .size(58.dp)
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
                                tint = Color(0xFF0C36A8),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Next Track
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.nextTrack()
                                }
                        )

                        // Favorite Heart
                        Icon(
                            imageVector = if (isCurrentFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isCurrentFav) Color(0xFFFF4B72) else Color.White.copy(alpha = 0.75f),
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
