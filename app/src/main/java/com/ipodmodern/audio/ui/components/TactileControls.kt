package com.ipodmodern.audio.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.theme.LocalModernColors
import com.ipodmodern.audio.ui.theme.ModernAccentBlue
import com.ipodmodern.audio.ui.theme.ModernAccentCyan
import com.ipodmodern.audio.ui.theme.ModernAccentEmerald
import com.ipodmodern.audio.ui.theme.ModernAccentPurple
import com.ipodmodern.audio.ui.theme.ModernHeroGradient
import com.ipodmodern.audio.ui.theme.ModernTextMuted
import com.ipodmodern.audio.ui.theme.ModernTextPrimary
import com.ipodmodern.audio.ui.theme.ModernTextSecondary

/**
 * Double-bezel glass card container with top highlight and subtle ambient shadow.
 */
@Composable
fun TactileGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0xFF101319),
    borderColor: Color = Color(0x24FFFFFF),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color.Black.copy(alpha = 0.8f)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .drawBehind {
                // Top inner highlight sheen
                drawLine(
                    color = Color.White.copy(alpha = 0.12f),
                    start = Offset(16.dp.toPx(), 1f),
                    end = Offset(size.width - 16.dp.toPx(), 1f),
                    strokeWidth = 1.5f
                )
            }
    ) {
        content()
    }
}

/**
 * Realistic tactile glass button with spring-press physics and inner lighting.
 */
@Composable
fun TactileIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 22.dp,
    isActive: Boolean = false,
    activeColor: Color = ModernAccentBlue,
    inactiveColor: Color = ModernTextSecondary,
    badgeText: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 600f),
        label = "tactile_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        animationSpec = tween(180),
        label = "icon_color"
    )

    val bgBrush = if (isActive) {
        Brush.radialGradient(
            listOf(activeColor.copy(alpha = 0.25f), Color(0xFF161A22))
        )
    } else {
        Brush.linearGradient(
            listOf(Color(0xFF1A1E27), Color(0xFF12151B))
        )
    }

    val borderStroke = if (isActive) {
        activeColor.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.10f)
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = if (isActive) 10.dp else 4.dp,
                shape = CircleShape,
                spotColor = if (isActive) activeColor.copy(alpha = 0.4f) else Color.Black
            )
            .clip(CircleShape)
            .background(bgBrush)
            .border(1.dp, borderStroke, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .drawBehind {
                // Top crescent sheen
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(this.size.width / 2, 4.dp.toPx()),
                        radius = this.size.width / 2
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .background(activeColor, CircleShape)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Primary Hero Play/Pause button with radial neon glow and animated morph.
 */
@Composable
fun TactilePlayPauseHeroButton(
    isPlaying: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 550f),
        label = "hero_play_scale"
    )

    val glowColor = ModernAccentBlue

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = glowColor.copy(alpha = 0.5f),
                spotColor = glowColor.copy(alpha = 0.7f)
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(ModernAccentBlue, ModernAccentPurple)
                )
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle
            )
            .drawBehind {
                // Top bevel reflection
                drawCircle(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.4f), Color.Transparent),
                        startY = 0f,
                        endY = this.size.height * 0.6f
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                (fadeIn(animationSpec = tween(150, easing = FastOutSlowInEasing)))
                    .togetherWith(fadeOut(animationSpec = tween(150)))
            },
            label = "play_pause_morph"
        ) { playing ->
            Icon(
                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (playing) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/**
 * Complete tactile transport controls row (Shuffle, Prev, Hero Play/Pause, Next, Repeat).
 */
@Composable
fun TactileTransportRow(
    isPlaying: Boolean,
    isShuffle: Boolean,
    repeatMode: Int, // 0 = OFF, 1 = ALL, 2 = ONE
    onTogglePlayPause: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle Button
        TactileIconButton(
            icon = Icons.Default.Shuffle,
            contentDescription = "Shuffle",
            isActive = isShuffle,
            activeColor = ModernAccentCyan,
            onClick = onToggleShuffle,
            size = 46.dp,
            iconSize = 20.dp
        )

        // Previous Track
        TactileIconButton(
            icon = Icons.Default.SkipPrevious,
            contentDescription = "Previous Track",
            onClick = onPrevClick,
            size = 54.dp,
            iconSize = 26.dp
        )

        // Hero Play / Pause Button
        TactilePlayPauseHeroButton(
            isPlaying = isPlaying,
            onToggle = onTogglePlayPause,
            size = 72.dp
        )

        // Next Track
        TactileIconButton(
            icon = Icons.Default.SkipNext,
            contentDescription = "Next Track",
            onClick = onNextClick,
            size = 54.dp,
            iconSize = 26.dp
        )

        // Repeat Mode
        TactileIconButton(
            icon = if (repeatMode == 2) Icons.Default.RepeatOne else Icons.Default.Repeat,
            contentDescription = "Repeat",
            isActive = repeatMode > 0,
            activeColor = ModernAccentPurple,
            badgeText = if (repeatMode == 2) "1" else null,
            onClick = onToggleRepeat,
            size = 46.dp,
            iconSize = 20.dp
        )
    }
}

/**
 * Fluid interactive timeline scrubber with glowing active track and smooth drag.
 */
@Composable
fun TactileTimelineScrubber(
    positionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val totalDuration = durationMs.coerceAtLeast(1L)
    val currentProgress = if (isDragging) dragProgress else (positionMs.toFloat() / totalDuration).coerceIn(0f, 1f)

    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.35f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "scrubber_thumb_scale"
    )

    fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val minutes = totalSec / 60
        val seconds = totalSec % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    val elapsedText = formatTime(if (isDragging) (dragProgress * totalDuration).toLong() else positionMs)
    val remainingMs = (totalDuration - (if (isDragging) (dragProgress * totalDuration).toLong() else positionMs)).coerceAtLeast(0L)
    val remainingText = "-${formatTime(remainingMs)}"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .pointerInput(totalDuration) {
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeekTo((newProgress * totalDuration).toLong())
                    }
                }
                .pointerInput(totalDuration) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeekTo((dragProgress * totalDuration).toLong())
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val trackHeight = if (isDragging) 6.dp else 4.dp

            // Background Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(CircleShape)
                    .background(Color(0xFF222834))
            )

            // Active Glowing Progress Track
            Box(
                modifier = Modifier
                    .fillMaxWidth(currentProgress)
                    .height(trackHeight)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(ModernAccentCyan, ModernAccentBlue)
                        )
                    )
            )

            // Draggable Thumb
            val thumbOffsetDp = ((maxWidth - 14.dp) * currentProgress).coerceAtLeast(0.dp)
            Box(
                modifier = Modifier
                    .offset(x = thumbOffsetDp)
                    .size(14.dp)
                    .scale(thumbScale)
                    .shadow(8.dp, CircleShape, spotColor = ModernAccentBlue)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, ModernAccentBlue, CircleShape)
            )
        }

        // Time Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = elapsedText,
                color = ModernTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = remainingText,
                color = ModernTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Modern tactile volume bar with quick mute.
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
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (volume <= 0.01f) Icons.Default.VolumeMute else Icons.Default.VolumeDown,
            contentDescription = "Volume",
            tint = if (volume > 0.01f) ModernTextSecondary else ModernTextMuted,
            modifier = Modifier
                .size(18.dp)
                .clickable {
                    if (volume > 0.01f) onVolumeChange(0.0f) else onVolumeChange(0.5f)
                }
        )

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
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
            val trackHeight = if (isDragging) 6.dp else 4.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(CircleShape)
                    .background(Color(0xFF1E232E))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(volume.coerceIn(0f, 1f))
                    .height(trackHeight)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF475569), ModernAccentBlue)
                        )
                    )
            )

            val thumbOffsetDp = ((maxWidth - 12.dp) * volume.coerceIn(0f, 1f)).coerceAtLeast(0.dp)
            Box(
                modifier = Modifier
                    .offset(x = thumbOffsetDp)
                    .size(12.dp)
                    .scale(if (isDragging) 1.3f else 1.0f)
                    .shadow(4.dp, CircleShape, spotColor = ModernAccentBlue)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }

        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Volume High",
            tint = ModernTextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
