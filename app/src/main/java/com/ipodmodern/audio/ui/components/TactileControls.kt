package com.ipodmodern.audio.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.theme.RaycastAccentBlue
import com.ipodmodern.audio.ui.theme.RaycastAccentGreen
import com.ipodmodern.audio.ui.theme.RaycastAccentYellow
import com.ipodmodern.audio.ui.theme.RaycastAsh
import com.ipodmodern.audio.ui.theme.RaycastBody
import com.ipodmodern.audio.ui.theme.RaycastHairline
import com.ipodmodern.audio.ui.theme.RaycastHairlineStrong
import com.ipodmodern.audio.ui.theme.RaycastInk
import com.ipodmodern.audio.ui.theme.RaycastKeycapGradient
import com.ipodmodern.audio.ui.theme.RaycastMute
import com.ipodmodern.audio.ui.theme.RaycastOnPrimary
import com.ipodmodern.audio.ui.theme.RaycastPrimaryPressed
import com.ipodmodern.audio.ui.theme.RaycastPrimaryWhite
import com.ipodmodern.audio.ui.theme.RaycastRadiusMd
import com.ipodmodern.audio.ui.theme.RaycastRadiusXs
import com.ipodmodern.audio.ui.theme.RaycastSurface
import com.ipodmodern.audio.ui.theme.RaycastSurfaceCard
import com.ipodmodern.audio.ui.theme.RaycastSurfaceElevated
import java.util.Locale

/**
 * Raycast Surface Card Container with 1px hairline border (no drop shadows).
 */
@Composable
fun RaycastCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = RaycastSurface,
    borderColor: Color = RaycastHairline,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
    ) {
        content()
    }
}

/**
 * Keycap-style physical badge for audio badges and keyboard hints.
 */
@Composable
fun RaycastKeycapBadge(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = RaycastInk,
    accentColor: Color? = null
) {
    Box(
        modifier = modifier
            .clip(RaycastRadiusXs)
            .background(RaycastKeycapGradient)
            .border(1.dp, RaycastHairline, RaycastRadiusXs)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (accentColor != null) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.4.sp
            )
        }
    }
}

/**
 * Universal Raycast Primary White Action Pill Button.
 */
@Composable
fun RaycastPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = 600f, dampingRatio = 0.75f),
        label = "btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(if (isPressed) RaycastPrimaryPressed else RaycastPrimaryWhite)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RaycastOnPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = RaycastOnPrimary,
                letterSpacing = 0.2.sp
            )
        }
    }
}

/**
 * Raycast Secondary / Tertiary Button with 1px hairline border.
 */
@Composable
fun RaycastSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(stiffness = 600f),
        label = "sec_btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(RaycastSurfaceElevated)
            .border(1.dp, if (isPressed) RaycastHairlineStrong else RaycastHairline, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RaycastInk,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = RaycastInk,
                letterSpacing = 0.2.sp
            )
        }
    }
}

/**
 * Tactile Icon Button in Raycast button-tertiary style.
 */
@Composable
fun TactileIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp,
    tint: Color = RaycastInk,
    isActive: Boolean = false,
    badgeText: String? = null
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.7f),
        label = "icon_press_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isActive) RaycastSurfaceCard else RaycastSurfaceElevated)
            .border(
                1.dp,
                if (isActive) RaycastHairlineStrong else RaycastHairline,
                CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) RaycastPrimaryWhite else tint,
            modifier = Modifier.size(iconSize)
        )

        // Tiny Badge or Dot for Active State
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(RaycastPrimaryWhite)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaycastOnPrimary
                )
            }
        }
    }
}

/**
 * Universal Raycast Primary White Hero Play/Pause Button.
 */
@Composable
fun TactilePlayPauseHeroButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = 600f, dampingRatio = 0.75f),
        label = "hero_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isPressed) RaycastPrimaryPressed else RaycastPrimaryWhite)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                fadeIn(animationSpec = tween(140)) togetherWith fadeOut(animationSpec = tween(140))
            },
            label = "hero_icon"
        ) { playing ->
            Icon(
                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (playing) "Pause" else "Play",
                tint = RaycastOnPrimary,
                modifier = Modifier.size(size * 0.46f)
            )
        }
    }
}

/**
 * Raycast Transport Deck Row.
 */
@Composable
fun TactileTransportRow(
    isPlaying: Boolean,
    isShuffle: Boolean,
    repeatMode: Int,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle Button
        TactileIconButton(
            icon = Icons.Default.Shuffle,
            contentDescription = "Shuffle",
            onClick = onToggleShuffle,
            size = 46.dp,
            iconSize = 18.dp,
            tint = if (isShuffle) RaycastAccentBlue else RaycastMute,
            isActive = isShuffle
        )

        // Previous Track
        TactileIconButton(
            icon = Icons.Default.SkipPrevious,
            contentDescription = "Previous",
            onClick = onPrevClick,
            size = 52.dp,
            iconSize = 22.dp
        )

        // Hero Play / Pause
        TactilePlayPauseHeroButton(
            isPlaying = isPlaying,
            onClick = onPlayPauseClick,
            size = 68.dp
        )

        // Next Track
        TactileIconButton(
            icon = Icons.Default.SkipNext,
            contentDescription = "Next",
            onClick = onNextClick,
            size = 52.dp,
            iconSize = 22.dp
        )

        // Repeat Mode
        TactileIconButton(
            icon = if (repeatMode == 2) Icons.Default.RepeatOne else Icons.Default.Repeat,
            contentDescription = "Repeat",
            onClick = onToggleRepeat,
            size = 46.dp,
            iconSize = 18.dp,
            tint = if (repeatMode > 0) RaycastAccentGreen else RaycastMute,
            isActive = repeatMode > 0,
            badgeText = if (repeatMode == 2) "1" else null
        )
    }
}

/**
 * Raycast Precision Waveform/Timeline Scrubber.
 */
@Composable
fun TactileTimelineScrubber(
    positionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMs = durationMs.coerceAtLeast(1L)
    val progress = (positionMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val currentProgress = if (isDragging) dragProgress else progress

    val elapsedText = remember(isDragging, positionMs, dragProgress, totalMs) {
        val ms = if (isDragging) (dragProgress * totalMs).toLong() else positionMs
        val totalSec = ms / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        String.format(Locale.US, "%02d:%02d", m, s)
    }

    val remainingText = remember(isDragging, positionMs, dragProgress, totalMs) {
        val currentMs = if (isDragging) (dragProgress * totalMs).toLong() else positionMs
        val remainMs = (totalMs - currentMs).coerceAtLeast(0L)
        val totalSec = remainMs / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        String.format(Locale.US, "-%02d:%02d", m, s)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeekTo((newProgress * totalMs).toLong())
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeekTo((dragProgress * totalMs).toLong())
                        },
                        onDragCancel = { isDragging = false },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val trackHeight = if (isDragging) 4.dp else 3.dp

            // Background Hairline Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(CircleShape)
                    .background(RaycastHairline)
            )

            // Active White Progress Track
            Box(
                modifier = Modifier
                    .fillMaxWidth(currentProgress)
                    .height(trackHeight)
                    .clip(CircleShape)
                    .background(RaycastPrimaryWhite)
            )

            // Draggable Thumb
            val thumbOffsetDp = ((maxWidth - 12.dp) * currentProgress).coerceAtLeast(0.dp)
            Box(
                modifier = Modifier
                    .offset(x = thumbOffsetDp)
                    .size(if (isDragging) 14.dp else 10.dp)
                    .clip(CircleShape)
                    .background(RaycastPrimaryWhite)
                    .border(1.dp, RaycastHairline, CircleShape)
            )
        }

        // Time Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = elapsedText,
                color = RaycastMute,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = remainingText,
                color = RaycastMute,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Raycast Precision Volume Slider.
 */
@Composable
fun TactileVolumeBar(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (volume <= 0.01f) Icons.Default.VolumeMute else Icons.Default.VolumeDown,
            contentDescription = "Volume",
            tint = if (volume > 0.01f) RaycastBody else RaycastAsh,
            modifier = Modifier
                .size(16.dp)
                .clickable {
                    if (volume > 0.01f) onVolumeChange(0.0f) else onVolumeChange(0.5f)
                }
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newVol = (offset.x / size.width).coerceIn(0f, 1f)
                        onVolumeChange(newVol)
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            onVolumeChange((offset.x / size.width).coerceIn(0f, 1f))
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            onVolumeChange((change.position.x / size.width).coerceIn(0f, 1f))
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val trackHeight = if (isDragging) 4.dp else 3.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(CircleShape)
                    .background(RaycastHairline)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(volume.coerceIn(0f, 1f))
                    .height(trackHeight)
                    .clip(CircleShape)
                    .background(RaycastPrimaryWhite)
            )

            val thumbOffsetDp = ((maxWidth - 10.dp) * volume.coerceIn(0f, 1f)).coerceAtLeast(0.dp)
            Box(
                modifier = Modifier
                    .offset(x = thumbOffsetDp)
                    .size(if (isDragging) 12.dp else 9.dp)
                    .clip(CircleShape)
                    .background(RaycastPrimaryWhite)
            )
        }

        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Volume High",
            tint = RaycastBody,
            modifier = Modifier.size(16.dp)
        )
    }
}
