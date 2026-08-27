package com.ipodmodern.audio.ui.components

import android.graphics.BitmapFactory
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.theme.RaycastBody
import com.ipodmodern.audio.ui.theme.RaycastHairline
import com.ipodmodern.audio.ui.theme.RaycastHairlineStrong
import com.ipodmodern.audio.ui.theme.RaycastInk
import com.ipodmodern.audio.ui.theme.RaycastMute
import com.ipodmodern.audio.ui.theme.RaycastOnPrimary
import com.ipodmodern.audio.ui.theme.RaycastPrimaryWhite
import com.ipodmodern.audio.ui.theme.RaycastRadiusLg
import com.ipodmodern.audio.ui.theme.RaycastRadiusSm
import com.ipodmodern.audio.ui.theme.RaycastSurface
import com.ipodmodern.audio.ui.theme.RaycastSurfaceElevated

@Composable
fun MiniPlayerBar(
    track: Track?,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onBarClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    val view = LocalView.current
    val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val artworkBitmap = remember(track.artworkUri) {
        track.artworkUri?.let { path ->
            try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RaycastRadiusLg)
            .background(RaycastSurface)
            .border(1.dp, RaycastHairline, RaycastRadiusLg)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onBarClick()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Album Art Thumbnail (38px)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RaycastRadiusSm)
                    .background(RaycastSurfaceElevated)
                    .border(1.dp, RaycastHairline, RaycastRadiusSm),
                contentAlignment = Alignment.Center
            ) {
                if (artworkBitmap != null) {
                    Image(
                        bitmap = artworkBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = RaycastMute,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Title & Artist
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = track.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaycastInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    fontSize = 11.sp,
                    color = RaycastBody,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Mini Play/Pause Button (Raycast Universal White CTA Circle)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(RaycastPrimaryWhite)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onPlayPauseClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isPlaying,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "mini_play"
                ) { playing ->
                    Icon(
                        imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playing) "Pause" else "Play",
                        tint = RaycastOnPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Next Track Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(RaycastSurfaceElevated)
                    .border(1.dp, RaycastHairline, CircleShape)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onNextClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = RaycastInk,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Hairline Progress Track at Bottom Edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter)
                .background(RaycastHairline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(RaycastPrimaryWhite)
            )
        }
    }
}
