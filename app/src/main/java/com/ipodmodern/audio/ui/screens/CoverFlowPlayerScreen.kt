package com.ipodmodern.audio.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.Album
import com.ipodmodern.audio.core.model.Track
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CoverFlowPlayerScreen(
    currentTrack: Track?,
    allTracks: List<Track>,
    currentTrackIndex: Int, // 0-based
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    volumeLevel: Float,
    currentLyricText: String?,
    onTrackSelect: (Int) -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onLyricsClick: () -> Unit,
    onEqClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedOffset = remember { Animatable(currentTrackIndex.toFloat()) }
    val coroutineScope = rememberCoroutineScope()
    var isUserDragging by remember { mutableStateOf(false) }
    var isShuffleActive by remember { mutableStateOf(false) }
    var isRepeatActive by remember { mutableStateOf(false) }

    // Synchronize animated offset when currentTrackIndex changes externally
    LaunchedEffect(currentTrackIndex) {
        if (!isUserDragging) {
            animatedOffset.animateTo(
                targetValue = currentTrackIndex.toFloat(),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    val displayTracks = if (allTracks.isNotEmpty()) allTracks else listOfNotNull(currentTrack)
    val activeTrack = currentTrack ?: displayTracks.getOrNull(currentTrackIndex)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF040507),
                        Color(0xFF0B0D12),
                        Color(0xFF06070A)
                    )
                )
            )
    ) {
        // Ambient dynamic background glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.TopCenter)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF0A84FF).copy(alpha = 0.22f),
                            Color(0xFF5E5CE6).copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ==========================================
            // 1. TOP SPATIAL 3D COVER FLOW STAGE
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            isUserDragging = true
                            coroutineScope.launch {
                                val currentVal = animatedOffset.value - (delta / 160f)
                                val clamped = currentVal.coerceIn(0f, (displayTracks.size - 1).coerceAtLeast(0).toFloat())
                                animatedOffset.snapTo(clamped)
                            }
                        },
                        onDragStopped = {
                            isUserDragging = false
                            val target = animatedOffset.value.roundToInt().coerceIn(0, (displayTracks.size - 1).coerceAtLeast(0))
                            coroutineScope.launch {
                                animatedOffset.animateTo(
                                    targetValue = target.toFloat(),
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                                onTrackSelect(target)
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (displayTracks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Music Available", color = Color.Gray, fontSize = 15.sp)
                    }
                } else {
                    val currentIndex = animatedOffset.value.roundToInt().coerceIn(0, displayTracks.size - 1)
                    val windowRange = (currentIndex - 3)..(currentIndex + 3)

                    for (i in windowRange) {
                        if (i < 0 || i >= displayTracks.size) continue

                        val track = displayTracks[i]
                        val offset = i - animatedOffset.value
                        val absOffset = abs(offset)

                        // 3D Matrix Math
                        val rotationY = when {
                            offset < -0.1f -> 52f
                            offset > 0.1f -> -52f
                            else -> -offset * 520f
                        }.coerceIn(-58f, 58f)

                        val scale = (1.18f - absOffset * 0.18f).coerceIn(0.68f, 1.18f)
                        val translationX = offset * 115f
                        val zIndexVal = 100f - absOffset * 10f

                        Box(
                            modifier = Modifier
                                .zIndex(zIndexVal)
                                .graphicsLayer {
                                    this.cameraDistance = 20f
                                    this.rotationY = rotationY
                                    this.scaleX = scale
                                    this.scaleY = scale
                                    this.translationX = translationX * density
                                    this.transformOrigin = TransformOrigin(
                                        pivotFractionX = if (offset < 0) 0.85f else if (offset > 0) 0.15f else 0.5f,
                                        pivotFractionY = 0.5f
                                    )
                                }
                                .size(175.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (absOffset < 0.35f) {
                                        onPlayPauseClick()
                                    } else {
                                        onTrackSelect(i)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Main Album Card
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .shadow(32.dp, RoundedCornerShape(12.dp))
                                        .background(Color(0xFF16181D))
                                        .border(1.5.dp, Color.White.copy(alpha = if (absOffset < 0.35f) 0.35f else 0.15f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!track.artworkUri.isNullOrEmpty()) {
                                        val model = if (track.artworkUri.startsWith("/")) File(track.artworkUri) else track.artworkUri
                                        AsyncImage(
                                            model = model,
                                            contentDescription = track.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(56.dp)
                                        )
                                    }

                                    // Specular glass sheen highlight
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color.White.copy(alpha = 0.28f),
                                                        Color.White.copy(alpha = 0.05f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                    )
                                }

                                // Floor Mirror Reflection
                                Box(
                                    modifier = Modifier
                                        .size(width = 150.dp, height = 55.dp)
                                        .graphicsLayer { scaleY = -1f }
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    if (!track.artworkUri.isNullOrEmpty()) {
                                        val model = if (track.artworkUri.startsWith("/")) File(track.artworkUri) else track.artworkUri
                                        AsyncImage(
                                            model = model,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    // Mirror fade into floor
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        Color(0xFF040507).copy(alpha = 0.30f),
                                                        Color(0xFF040507).copy(alpha = 0.85f),
                                                        Color(0xFF040507)
                                                    )
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. TRACK METADATA & AUDIOPHILE BADGE
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Song Title
                Text(
                    text = activeTrack?.title ?: "Select a song",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Artist & Album
                Text(
                    text = if (activeTrack != null) "${activeTrack.artist} • ${activeTrack.album}" else "iPod Modern",
                    fontSize = 14.sp,
                    color = Color(0xFFA1A4B0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Audiophile Quality Pill with mini Equalizer bars
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF15181F))
                        .border(1.dp, Color(0xFF00C7BE).copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    MiniEqualizerVisualizer(isPlaying = isPlaying)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activeTrack?.badgeText ?: "LOSSLESS 24-BIT / 96.0kHz",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF30D158),
                        letterSpacing = 0.8.sp
                    )
                }

                // Live Lyrics Snippet preview
                if (!currentLyricText.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E212A).copy(alpha = 0.6f))
                            .clickable { onLyricsClick() }
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = "Lyrics",
                            tint = Color(0xFFFFD60A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentLyricText,
                            fontSize = 12.sp,
                            color = Color(0xFFFFD60A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ==========================================
            // 3. PRECISION PROGRESS SCRUBBER BAR
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                var sliderPosition by remember { mutableFloatStateOf(0f) }
                var isSeeking by remember { mutableStateOf(false) }

                val maxDuration = if (durationMs > 0) durationMs.toFloat() else 1f
                val currentProgress = if (isSeeking) sliderPosition else positionMs.toFloat().coerceIn(0f, maxDuration)

                Slider(
                    value = currentProgress,
                    onValueChange = {
                        isSeeking = true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        onSeekTo(sliderPosition.toLong())
                    },
                    valueRange = 0f..maxDuration,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFF0A84FF),
                        inactiveTrackColor = Color(0xFF2C2F38)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                )

                // Time indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentSec = (currentProgress / 1000).toLong()
                    val remainingSec = ((maxDuration - currentProgress) / 1000).toLong()

                    Text(
                        text = String.format(Locale.US, "%02d:%02d", currentSec / 60, currentSec % 60),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                    Text(
                        text = String.format(Locale.US, "-%02d:%02d", remainingSec / 60, remainingSec % 60),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                }
            }

            // ==========================================
            // 4. LUXURY TRANSPORT CONTROLS
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(
                    onClick = { isShuffleActive = !isShuffleActive },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleActive) Color(0xFF0A84FF) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Previous Track
                IconButton(
                    onClick = onPrevClick,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Master Glowing Play/Pause Center Button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF0A84FF),
                                    Color(0xFF0060DF)
                                )
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        .shadow(20.dp, CircleShape, spotColor = Color(0xFF0A84FF))
                        .clickable { onPlayPauseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Next Track
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Repeat Button
                IconButton(
                    onClick = { isRepeatActive = !isRepeatActive },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (isRepeatActive) Color(0xFF0A84FF) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ==========================================
            // 5. QUICK UTILITIES & VOLUME CONTROLLER
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                // EQ & Lyrics Shortcuts Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // EQ Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B1E26))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .clickable { onEqClick() }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Equalizer",
                            tint = Color(0xFF0A84FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("10-Band EQ", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    // Full Lyrics Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B1E26))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                            .clickable { onLyricsClick() }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = "Lyrics",
                            tint = Color(0xFFFF9F0A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lyrics", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Volume Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                        contentDescription = "Volume Down",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Slider(
                        value = volumeLevel,
                        onValueChange = onVolumeChange,
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White.copy(alpha = 0.7f),
                            inactiveTrackColor = Color(0xFF2C2F38)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Volume Up",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Animated 4-bar mini equalizer visualizer pulsating to audio beats
 */
@Composable
fun MiniEqualizerVisualizer(isPlaying: Boolean) {
    val transition = rememberInfiniteTransition(label = "eqAnim")

    val h1 by transition.animateFloat(
        initialValue = 4f,
        targetValue = if (isPlaying) 14f else 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 8f,
        targetValue = if (isPlaying) 18f else 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 12f,
        targetValue = if (isPlaying) 6f else 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )
    val h4 by transition.animateFloat(
        initialValue = 5f,
        targetValue = if (isPlaying) 16f else 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h4"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(18.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h1.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(Color(0xFF30D158))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h2.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(Color(0xFF30D158))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(Color(0xFF30D158))
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h4.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(Color(0xFF30D158))
        )
    }
}
