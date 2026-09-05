package com.ipodmodern.audio.ui.components

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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.viewmodel.PlaybackProgress
import kotlinx.coroutines.flow.StateFlow
import com.ipodmodern.audio.ui.theme.MintAccent
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
import java.io.File

@Composable
fun MiniPlayerBar(
    track: Track?,
    isPlaying: Boolean,
    positionMs: Long = 0L,
    durationMs: Long = 0L,
    progressFlow: StateFlow<PlaybackProgress>? = null,
    isFavorite: Boolean = false,
    onBarClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onQueueClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (track == null) return

    val view = LocalView.current
    val context = LocalContext.current

    val artworkFile = remember(track.artworkUri) {
        track.artworkUri?.let { File(it) }
    }
    val artworkRequest = remember(artworkFile) {
        artworkFile?.let {
            ImageRequest.Builder(context)
                .data(it)
                .size(132)
                .crossfade(true)
                .build()
        }
    }

    val miniArtScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.88f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "mini_art_scale"
    )

    val palette = com.ipodmodern.audio.ui.theme.LocalThemePalette.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(16.dp, RadiusLg, spotColor = Color(0x77000000))
            .clip(RadiusLg)
            .background(palette.surfaceElevated)
            .border(1.dp, palette.borderSubtle, RadiusLg)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onBarClick()
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Subtle top progress bar isolated to avoid full-bar recomposition
            MiniPlayerProgressBar(
                progressFlow = progressFlow,
                fallbackPos = positionMs,
                fallbackDur = if (durationMs > 0) durationMs else track.durationMs
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Album Art Thumbnail (Elastic Playback Spring Feedback)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .graphicsLayer {
                            scaleX = miniArtScale
                            scaleY = miniArtScale
                        }
                        .clip(RadiusMd)
                        .background(palette.surface)
                        .border(1.dp, palette.borderSubtle, RadiusMd),
                    contentAlignment = Alignment.Center
                ) {
                    if (artworkRequest != null) {
                        AsyncImage(
                            model = artworkRequest,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = palette.textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Title & Artist Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedContent(
                        targetState = track.title,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "mini_title"
                    ) { targetTitle ->
                        Text(
                            text = targetTitle,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist.ifBlank { "Unknown Artist" },
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Play / Pause Icon Button (Punchy Electric Lime Circle with Dark Icon)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(palette.accent)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onPlayPauseClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = {
                            (scaleIn(initialScale = 0.75f, animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)))
                                .togetherWith(scaleOut(targetScale = 0.75f, animationSpec = tween(200)) + fadeOut(animationSpec = tween(200)))
                        },
                        label = "mini_play_pause"
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = palette.bg,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Next Track Icon Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onNextClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniPlayerProgressBar(
    progressFlow: StateFlow<PlaybackProgress>?,
    fallbackPos: Long,
    fallbackDur: Long
) {
    val fraction = if (progressFlow != null) {
        val state by progressFlow.collectAsState()
        val dur = if (state.durationMs > 0) state.durationMs else fallbackDur
        if (dur > 0) (state.positionMs.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f
    } else {
        if (fallbackDur > 0) (fallbackPos.toFloat() / fallbackDur.toFloat()).coerceIn(0f, 1f) else 0f
    }

    val accent = com.ipodmodern.audio.ui.theme.LocalThemePalette.current.accent
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Color(0x22FFFFFF))
            .drawBehind {
                drawRect(
                    color = accent,
                    size = androidx.compose.ui.geometry.Size(
                        width = fraction * size.width,
                        height = size.height
                    )
                )
            }
    )
}
