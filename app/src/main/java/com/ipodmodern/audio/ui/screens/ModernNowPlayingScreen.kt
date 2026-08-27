package com.ipodmodern.audio.ui.screens

import android.graphics.BitmapFactory
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.RaycastKeycapBadge
import com.ipodmodern.audio.ui.components.TactileIconButton
import com.ipodmodern.audio.ui.components.TactileTimelineScrubber
import com.ipodmodern.audio.ui.components.TactileTransportRow
import com.ipodmodern.audio.ui.components.TactileVolumeBar
import com.ipodmodern.audio.ui.theme.AmberCanvas
import com.ipodmodern.audio.ui.theme.AmberChampagne
import com.ipodmodern.audio.ui.theme.AmberCognac
import com.ipodmodern.audio.ui.theme.AmberGold
import com.ipodmodern.audio.ui.theme.AmberGoldGlow
import com.ipodmodern.audio.ui.theme.AmberHairline
import com.ipodmodern.audio.ui.theme.AmberHairlineStrong
import com.ipodmodern.audio.ui.theme.AmberInk
import com.ipodmodern.audio.ui.theme.AmberMute
import com.ipodmodern.audio.ui.theme.AmberPrimaryWhite
import com.ipodmodern.audio.ui.theme.AmberRadiusLg
import com.ipodmodern.audio.ui.theme.AmberRadiusMd
import com.ipodmodern.audio.ui.theme.AmberRadiusXl
import com.ipodmodern.audio.ui.theme.AmberRose
import com.ipodmodern.audio.ui.theme.AmberSurface
import com.ipodmodern.audio.ui.theme.AmberSurfaceCard
import com.ipodmodern.audio.ui.theme.AmberSurfaceElevated
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

@Composable
fun ModernNowPlayingScreen(
    currentTrack: Track?,
    allTracks: List<Track>,
    currentTrackIndex: Int,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    volumeLevel: Float,
    isShuffle: Boolean = false,
    repeatMode: Int = 0,
    isFavorite: Boolean = false,
    currentLyricText: String? = null,
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

    // Smooth carousel animation offset
    val carouselOffset = remember { Animatable(activeIndex.toFloat()) }

    LaunchedEffect(activeIndex) {
        if (abs(carouselOffset.value - activeIndex.toFloat()) > 0.01f) {
            carouselOffset.animateTo(
                targetValue = activeIndex.toFloat(),
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(AmberCanvas)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // MARK: - Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TactileIconButton(
                icon = Icons.Default.KeyboardArrowDown,
                contentDescription = "Collapse",
                onClick = onCollapseClick,
                size = 42.dp,
                iconSize = 22.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "AETHER HI-FI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGold,
                    letterSpacing = 1.8.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${activeIndex + 1} of ${queue.size.coerceAtLeast(1)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AmberInk,
                    fontFamily = FontFamily.Monospace
                )
            }

            TactileIconButton(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                onClick = onToggleFavorite,
                size = 42.dp,
                iconSize = 20.dp,
                tint = if (isFavorite) AmberRose else AmberInk
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MARK: - Dynamic 3D Curved Carousel (Inspired by Reference Design)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            val newTarget = (carouselOffset.value - delta / 220f).coerceIn(0f, (queue.size - 1).toFloat().coerceAtLeast(0f))
                            carouselOffset.snapTo(newTarget)
                        }
                    },
                    onDragStopped = { velocity ->
                        coroutineScope.launch {
                            val current = carouselOffset.value
                            val target = if (abs(velocity) > 300f) {
                                (current - sign(velocity) * 0.6f).roundToInt()
                            } else {
                                current.roundToInt()
                            }.coerceIn(0, queue.size - 1)

                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            carouselOffset.animateTo(
                                target.toFloat(),
                                spring(dampingRatio = 0.72f, stiffness = 450f)
                            )
                            if (target != activeIndex) {
                                onTrackSelect(target)
                            }
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val centerIndex = carouselOffset.value

            // Ambient Gold Radiant Halo behind active card
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(AmberGoldGlow, Color.Transparent)
                        )
                    )
            )

            // Render 3D fanned cards (active center ± 2 neighbors)
            val minVisible = (centerIndex - 2.5f).toInt().coerceAtLeast(0)
            val maxVisible = (centerIndex + 2.5f).toInt().coerceAtMost(queue.size - 1)

            for (i in minVisible..maxVisible) {
                val trackItem = queue[i]
                val offsetFromCenter = i - centerIndex
                val absOffset = abs(offsetFromCenter)

                // 3D parameters
                val translationX = offsetFromCenter * 140f
                val rotationY = (offsetFromCenter * -24f).coerceIn(-48f, 48f)
                val scale = (1.0f - absOffset * 0.12f).coerceIn(0.75f, 1.0f)
                val zIndexVal = 100f - absOffset * 10f
                val darkOverlayAlpha = (absOffset * 0.45f).coerceIn(0f, 0.75f)

                CoverFlowCardItem(
                    track = trackItem,
                    isCenter = absOffset < 0.3f,
                    modifier = Modifier
                        .zIndex(zIndexVal)
                        .graphicsLayer {
                            this.translationX = translationX
                            this.rotationY = rotationY
                            this.scaleX = scale
                            this.scaleY = scale
                            this.cameraDistance = 14f * density
                            this.transformOrigin = TransformOrigin(0.5f, 0.5f)
                        }
                        .clickable {
                            if (i != activeIndex) {
                                coroutineScope.launch {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    carouselOffset.animateTo(i.toFloat(), spring(dampingRatio = 0.72f, stiffness = 450f))
                                    onTrackSelect(i)
                                }
                            }
                        },
                    darkOverlayAlpha = darkOverlayAlpha
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hi-Res Audio Keycap Badge
        val badgeText = currentTrack?.badgeText ?: "24-BIT • 96kHz LOSSLESS"
        RaycastKeycapBadge(
            text = badgeText,
            textColor = AmberGold,
            accentColor = AmberGold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // MARK: - Precision Waveform Scrubber
        TactileTimelineScrubber(
            positionMs = positionMs,
            durationMs = durationMs,
            onSeekTo = onSeekTo
        )

        Spacer(modifier = Modifier.height(12.dp))

        // MARK: - Tactile Transport Row (Hero White CTA with Amber Accents)
        TactileTransportRow(
            isPlaying = isPlaying,
            isShuffle = isShuffle,
            repeatMode = repeatMode,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onPrevClick = onPrevClick,
            onToggleShuffle = onToggleShuffle,
            onToggleRepeat = onToggleRepeat
        )

        Spacer(modifier = Modifier.height(10.dp))

        // MARK: - Precision Volume Slider
        TactileVolumeBar(
            volume = volumeLevel,
            onVolumeChange = onVolumeChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        // MARK: - Quick Bottom Actions (Lyrics & Studio 10-EQ)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Lyrics Pill
            Box(
                modifier = Modifier
                    .weight(1.4f)
                    .clip(AmberRadiusMd)
                    .background(AmberSurfaceElevated)
                    .border(1.dp, AmberHairline, AmberRadiusMd)
                    .clickable { onLyricsClick() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Subject,
                        contentDescription = "Lyrics",
                        tint = AmberGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (!currentLyricText.isNullOrBlank()) currentLyricText else "Synchronized Lyrics",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AmberInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 10-Band EQ Pill
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .clip(AmberRadiusMd)
                    .background(AmberSurfaceElevated)
                    .border(1.dp, AmberHairline, AmberRadiusMd)
                    .clickable { onEqClick() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "EQ",
                        tint = AmberGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "10-EQ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberInk
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 3D Curved Carousel Card Item (Faithful to Reference Design).
 */
@Composable
fun CoverFlowCardItem(
    track: Track,
    isCenter: Boolean,
    darkOverlayAlpha: Float,
    modifier: Modifier = Modifier
) {
    val artworkBitmap = remember(track.artworkUri) {
        track.artworkUri?.let { path ->
            try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
        }
    }

    Box(
        modifier = modifier
            .width(220.dp)
            .height(290.dp)
            .shadow(
                elevation = if (isCenter) 24.dp else 8.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = AmberGold.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(AmberSurface)
            .border(
                1.5.dp,
                if (isCenter) AmberHairlineStrong else AmberHairline,
                RoundedCornerShape(22.dp)
            )
    ) {
        // Upper Artwork
        if (artworkBitmap != null) {
            Image(
                bitmap = artworkBitmap.asImageBitmap(),
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2E2216), Color(0xFF140E08))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AmberGold,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        // Dark gradient shade on non-center cards
        if (darkOverlayAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = darkOverlayAlpha))
            )
        }

        // Frosted Glass Dark Gradient Footer with Song Title and Artist (as in screenshot)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color(0xCC0C0906),
                            Color(0xF00C0906)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = track.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberPrimaryWhite,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = track.artist,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AmberChampagne,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
