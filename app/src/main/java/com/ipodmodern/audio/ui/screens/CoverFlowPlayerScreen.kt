package com.ipodmodern.audio.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.exponentialDecay
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
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.MechanicalAuxKeyDeck
import com.ipodmodern.audio.ui.components.MechanicalTransportDeck
import com.ipodmodern.audio.ui.components.VolumeDeck
import com.ipodmodern.audio.ui.components.WaveformScrubber
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.tanh

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

    // Smoothly synchronize position when currentTrackIndex changes externally
    LaunchedEffect(currentTrackIndex) {
        if (!isUserDragging && animatedOffset.targetValue != currentTrackIndex.toFloat()) {
            animatedOffset.animateTo(
                targetValue = currentTrackIndex.toFloat(),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
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
                        Color(0xFF030406),
                        Color(0xFF090B10),
                        Color(0xFF050608)
                    )
                )
            )
    ) {
        // Dynamic ambient colored aura behind the stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.TopCenter)
                .blur(90.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color(0xFF007AFF).copy(alpha = 0.26f),
                            Color(0xFF5856D6).copy(alpha = 0.12f),
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
            // 1. BUTTERY-SMOOTH SPATIAL 3D COVER FLOW
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(245.dp)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            isUserDragging = true
                            coroutineScope.launch {
                                val currentVal = animatedOffset.value - (delta / 170f)
                                val maxIndex = (displayTracks.size - 1).coerceAtLeast(0).toFloat()
                                val clamped = currentVal.coerceIn(0f, maxIndex)
                                animatedOffset.snapTo(clamped)
                            }
                        },
                        onDragStopped = { velocity ->
                            isUserDragging = false
                            val maxIndex = (displayTracks.size - 1).coerceAtLeast(0)
                            // Calculate velocity flick target
                            val flickOffset = -(velocity / 900f).coerceIn(-2.5f, 2.5f)
                            val rawTarget = (animatedOffset.value + flickOffset).roundToInt()
                            val target = rawTarget.coerceIn(0, maxIndex)

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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Music Found On Device", color = Color.Gray, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Grant storage permission or sync music via Wi-Fi", color = Color.Gray.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }
                } else {
                    val currentIndex = animatedOffset.value.roundToInt().coerceIn(0, displayTracks.size - 1)
                    val windowRange = (currentIndex - 3)..(currentIndex + 3)

                    for (i in windowRange) {
                        if (i < 0 || i >= displayTracks.size) continue

                        val track = displayTracks[i]
                        val offset = i - animatedOffset.value
                        val absOffset = abs(offset)

                        // Continuous, smooth trigonometric 3D rotation & projection math
                        val rotationY = (-tanh(offset * 2.2f) * 56f).coerceIn(-60f, 60f)
                        val scale = (1.0f / (1.0f + absOffset * 0.22f) * 1.15f).coerceIn(0.68f, 1.15f)
                        val translationX = (sign(offset) * 88f + offset * 32f) * (1f - (1f / (1f + absOffset * 0.8f))) + (offset * 75f)
                        val zIndexVal = 100f - absOffset * 15f

                        Box(
                            modifier = Modifier
                                .zIndex(zIndexVal)
                                .graphicsLayer {
                                    this.cameraDistance = 22f
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
                                // 3D Album Artwork Card
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .shadow(
                                            elevation = if (absOffset < 0.35f) 36.dp else 16.dp,
                                            shape = RoundedCornerShape(12.dp),
                                            spotColor = if (absOffset < 0.35f) Color(0xFF007AFF) else Color.Black
                                        )
                                        .background(Color(0xFF14161C))
                                        .border(
                                            width = if (absOffset < 0.35f) 1.5.dp else 1.dp,
                                            color = if (absOffset < 0.35f) Color.White.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
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
                                            modifier = Modifier.size(54.dp)
                                        )
                                    }

                                    // Dynamic Specular Glass Glare
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color.White.copy(alpha = if (absOffset < 0.35f) 0.30f else 0.12f),
                                                        Color.White.copy(alpha = 0.04f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                    )
                                }

                                // High-Definition Floor Mirror Reflection
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
                                    // Smooth Floor Gradient Fade
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        Color(0xFF030406).copy(alpha = 0.25f),
                                                        Color(0xFF030406).copy(alpha = 0.85f),
                                                        Color(0xFF030406)
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
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Song Title
                Text(
                    text = activeTrack?.title ?: "No Track Playing",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Artist & Album
                Text(
                    text = if (activeTrack != null) "${activeTrack.artist} • ${activeTrack.album}" else "iPod Modern",
                    fontSize = 14.sp,
                    color = Color(0xFFA5A9B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                if (activeTrack != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Audiophile Quality Pill with pulsating Equalizer bars
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF141720))
                            .border(1.dp, Color(0xFF00C7BE).copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        MiniEqualizerVisualizer(isPlaying = isPlaying)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeTrack.badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF30D158),
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                // Live Lyrics Snippet chip
                if (!currentLyricText.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B1E28).copy(alpha = 0.7f))
                            .clickable { onLyricsClick() }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = "Lyrics",
                            tint = Color(0xFFFFD60A),
                            modifier = Modifier.size(13.dp)
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
            // 3. STUDIO WAVEFORM PROGRESS SCRUBBER
            // ==========================================
            WaveformScrubber(
                positionMs = positionMs,
                durationMs = durationMs,
                onSeekTo = onSeekTo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            // ==========================================
            // 4. 3D MECHANICAL HARDWARE TRANSPORT DECK
            // ==========================================
            MechanicalTransportDeck(
                isPlaying = isPlaying,
                onPrevClick = onPrevClick,
                onPlayPauseClick = onPlayPauseClick,
                onNextClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            // ==========================================
            // 5. AUXILIARY MECHANICAL FUNCTION KEYS
            // ==========================================
            MechanicalAuxKeyDeck(
                isShuffle = isShuffleActive,
                isRepeat = isRepeatActive,
                onShuffleToggle = { isShuffleActive = !isShuffleActive },
                onRepeatToggle = { isRepeatActive = !isRepeatActive },
                onEqClick = onEqClick,
                onLyricsClick = onLyricsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            // ==========================================
            // 6. STUDIO HI-FI TACTILE VOLUME DECK
            // ==========================================
            VolumeDeck(
                volumeLevel = volumeLevel,
                onVolumeChange = onVolumeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
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

