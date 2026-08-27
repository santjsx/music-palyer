package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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
 * Flagship Luxury Audiophile Player Screen:
 * - Perfectly integrated with the app's dynamic theme palette & dynamic blurred album art
 * - Grand 320dp Circular Chronograph Vinyl Wheel with laser-etched perimeter ticks, flat progress arc, speed chips, and inside jump controls
 * - Slowly spinning central vinyl disc with embedded album artwork
 * - Anchored Full-Bleed Glassmorphic Audio Deck with 3-line synchronized lyrics & high-end transport controls
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

    // Vinyl Rotation Animation (when playing)
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
    val vinylRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

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

    // Harmonized Theme Colors for the Screen & Deck
    val deckGradient = remember(palette.accent) {
        listOf(
            Color(0xFF256BFE),
            Color(0xFF144DC8),
            Color(0xFF0A2E8A)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg)
    ) {
        // LAYER 0: Ambient Blurred Album Art Backdrop
        if (artworkFile != null && artworkFile.exists()) {
            AsyncImage(
                model = artworkFile,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(50.dp)
            )
        }

        // LAYER 1: Atmospheric Gradient & Dark Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            palette.bg.copy(alpha = 0.78f),
                            palette.bg.copy(alpha = 0.88f),
                            Color(0xFF030509).copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // LAYER 2: Main Layout (Top Navigation Header + Grand Chronograph Stage + Anchored Audio Deck)
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
                // Sleek Glass Back Arrow
                Box(
                    modifier = Modifier
                        .size(42.dp)
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

                // Center Floating Pill
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 10.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 7.dp),
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
                        .size(42.dp)
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

            // 2. GRAND CHRONOGRAPH VINYL WHEEL STAGE (Weight = 1.2f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.2f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 330.dp)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // 2.1 Ambient Radial Glow Backdrop
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF2870FF).copy(alpha = 0.42f),
                                        Color(0xFF1440C0).copy(alpha = 0.18f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // 2.2 Background Canvas (Outer Precision Ticks, Dark Vinyl Body, Grooves & Progress Arc)
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
                        val arcStrokeWidth = 7.5.dp.toPx()
                        val outerRadius = size.minDimension / 2f - (arcStrokeWidth / 2f)
                        val vinylRadius = outerRadius - 2.5.dp.toPx()

                        // Draw 72 Perimeter Laser Ticks
                        val tickCount = 72
                        for (i in 0 until tickCount) {
                            val tickAngleDeg = (i.toFloat() / tickCount) * 360f - 90f
                            val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())
                            val isMajor = i % 6 == 0

                            val tickInnerR = outerRadius - (if (isMajor) 12.dp.toPx() else 6.5.dp.toPx())
                            val tickOuterR = outerRadius + 2.dp.toPx()

                            val startX = canvasCenter.x + (tickInnerR * cos(tickAngleRad)).toFloat()
                            val startY = canvasCenter.y + (tickInnerR * sin(tickAngleRad)).toFloat()
                            val endX = canvasCenter.x + (tickOuterR * cos(tickAngleRad)).toFloat()
                            val endY = canvasCenter.y + (tickOuterR * sin(tickAngleRad)).toFloat()

                            drawLine(
                                color = Color.White.copy(alpha = if (isMajor) 0.45f else 0.16f),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                            )
                        }

                        // Draw Dark Machined Vinyl Body
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1E2128),
                                    Color(0xFF13151A),
                                    Color(0xFF090A0E)
                                ),
                                center = canvasCenter,
                                radius = vinylRadius
                            ),
                            radius = vinylRadius,
                            center = canvasCenter
                        )

                        // 3 Concentric Physical Grooves
                        drawCircle(
                            color = Color.White.copy(alpha = 0.06f),
                            radius = vinylRadius * 0.78f,
                            center = canvasCenter,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = vinylRadius * 0.60f,
                            center = canvasCenter,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.04f),
                            radius = vinylRadius * 0.44f,
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

                    // 2.3 Inside Speed Chips [x0.5, x1, x2]
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.14f), CircleShape)
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
                                    .background(if (isSelected) Color.White.copy(alpha = 0.22f) else Color.Transparent)
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        playerViewModel.setPlaybackSpeed(spd)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.50f),
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    // 2.4 Inside Jump Buttons & Spinning Vinyl Core
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.68f)
                            .align(Alignment.Center),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // -10s Rewind Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.09f))
                                .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.seekBackward10s()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⟲10",
                                color = Color.White.copy(alpha = 0.88f),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Center Spinning Vinyl Core (~84dp)
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .rotate(if (uiState.isPlaying) vinylRotation else 0f)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
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
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            // Tiny Center Spindle Pin
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.dp, Color.Black.copy(alpha = 0.5f), CircleShape)
                            )
                        }

                        // +10s Fast Forward Button
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.09f))
                                .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.seekForward10s()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "10⟳",
                                color = Color.White.copy(alpha = 0.88f),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 2.5 Monospaced Timestamp & Subtle Dots
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = elapsedText,
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
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

            // 3. ANCHORED VIBRANT GLASSMORPHIС AUDIO DECK (Weight = 1.0f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                    .background(Brush.verticalGradient(colors = deckGradient))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.24f),
                        RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 3.1 Synchronized Lyrics / Hi-Res Lossless Showcase
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
                                    fontSize = 23.sp,
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
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Immerse in the Sound",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Aether Audiophile DSP Deck",
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // 3.2 Track Info Row: Overlapping Circular Artwork Discs + Title/Artist
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Double Overlapping Circular Artworks
                        Box(modifier = Modifier.size(56.dp, 40.dp)) {
                            // Back art disc
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
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
                            // Front art disc (Offset by 16dp)
                            Box(
                                modifier = Modifier
                                    .offset(x = 16.dp)
                                    .size(38.dp)
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
                                fontSize = 16.5.sp,
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

                    // 3.3 Premium Transport Controls: Repeat • Prev • Pure White Play/Pause • Next • Favorite
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Repeat Switcher
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

                        // Previous Track (High-end geometric skip icon)
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier
                                .size(38.dp)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.prevTrack()
                                }
                        )

                        // Large Solid Pure White Play/Pause Button (64dp) with Floating Depth
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(8.dp, CircleShape, spotColor = Color.Black)
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
                                tint = Color(0xFF0A2E8A),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Next Track (High-end geometric skip icon)
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier
                                .size(38.dp)
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
