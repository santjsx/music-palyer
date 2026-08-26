package com.ipodmodern.audio.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Authentic Hardware Audio Palette
private val KeyDark = Color(0xFF22242B)
private val KeyDarker = Color(0xFF14151A)
private val KeyLid = Color(0xFF2C2F38)
private val AccentNeon = Color(0xFF0A84FF)
private val AccentGreen = Color(0xFF30D158)
private val AccentAmber = Color(0xFFFF9F0A)
private val AccentMuted = Color(0xFF6B7280)

/**
 * 3D Physical Mechanical Key Component with dynamic tilt,
 * realistic sink translation, lid reveal, and LED glow.
 */
@Composable
fun MechanicalKey(
    label: String,
    icon: ImageVector? = null,
    isActive: Boolean = false,
    accentColor: Color = AccentNeon,
    width: Dp = 82.dp,
    height: Dp = 68.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isUserPressed by interactionSource.collectIsPressedAsState()

    // Key sink state (user press or toggled state)
    val pressProgress by animateFloatAsState(
        targetValue = if (isUserPressed || isActive) 1f else 0f,
        animationSpec = tween(durationMillis = 110),
        label = "pressProgress"
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height + 12.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        // 1. Back-side lid: rotates into view behind the key when pressed
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(14.dp)
                .graphicsLayer {
                    rotationX = 50f
                    cameraDistance = 16f * density
                    alpha = pressProgress
                }
                .background(
                    KeyLid,
                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                )
                .border(
                    0.5.dp,
                    Color.White.copy(alpha = 0.15f * pressProgress),
                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                )
        )

        // 2. Key face: tilts back in 3D and sinks down when pressed
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(height)
                .graphicsLayer {
                    rotationX = -16f * pressProgress
                    cameraDistance = 10f * density
                    transformOrigin = TransformOrigin(0.5f, 0.35f)
                    translationY = 5.dp.toPx() * pressProgress
                }
                .shadow(
                    elevation = ((1f - pressProgress) * 6 + 1).dp,
                    shape = RoundedCornerShape(6.dp),
                    spotColor = if (isActive) accentColor.copy(alpha = 0.5f) else Color.Black
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isActive) KeyDark.copy(alpha = 0.95f) else KeyDark,
                            if (isActive) KeyDarker else Color(0xFF181A20)
                        )
                    ),
                    RoundedCornerShape(6.dp)
                )
                .border(
                    1.dp,
                    if (isActive) accentColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(6.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // LED Indicator dot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isActive) accentColor else Color(0xFF383C48))
                        .then(
                            if (isActive) Modifier.shadow(4.dp, CircleShape, spotColor = accentColor) else Modifier
                        )
                )
            }

            // Center Content (Icon and/or Label)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isActive) accentColor else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (label.isNotEmpty()) {
                    Text(
                        text = label.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.8.sp,
                        color = if (isActive) accentColor else Color(0xFFCACFD9),
                        style = TextStyle(
                            shadow = Shadow(
                                color = if (isActive) accentColor.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.3f),
                                blurRadius = if (isActive) 12f else 2f
                            )
                        )
                    )
                }
            }

            // Bottom tactile notch bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(3.dp)
                    .background(
                        color = if (isActive) accentColor.copy(alpha = 0.8f) else Color(0xFF2E313D),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

/**
 * Hi-Fi 3-Key Hardware Transport Deck: PREV, PLAY/PAUSE, NEXT
 */
@Composable
fun MechanicalTransportDeck(
    isPlaying: Boolean,
    onPrevClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F1015))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // PREV KEY
        MechanicalKey(
            label = "REV",
            icon = Icons.Default.FastRewind,
            width = 86.dp,
            height = 64.dp,
            onClick = onPrevClick
        )

        // MASTER PLAY / PAUSE KEY
        MechanicalKey(
            label = if (isPlaying) "PAUSE" else "PLAY",
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            isActive = isPlaying,
            accentColor = AccentNeon,
            width = 110.dp,
            height = 70.dp,
            onClick = onPlayPauseClick
        )

        // NEXT KEY
        MechanicalKey(
            label = "FWD",
            icon = Icons.Default.FastForward,
            width = 86.dp,
            height = 64.dp,
            onClick = onNextClick
        )
    }
}

/**
 * Auxiliary Mechanical Function Strip: SHUFFLE, REPEAT, 10-BAND EQ, LYRICS
 */
@Composable
fun MechanicalAuxKeyDeck(
    isShuffle: Boolean,
    isRepeat: Boolean,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onEqClick: () -> Unit,
    onLyricsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1015))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MechanicalKey(
            label = "SHUF",
            icon = Icons.Default.Shuffle,
            isActive = isShuffle,
            accentColor = AccentNeon,
            width = 68.dp,
            height = 48.dp,
            onClick = onShuffleToggle
        )

        MechanicalKey(
            label = "RPT",
            icon = Icons.Default.Repeat,
            isActive = isRepeat,
            accentColor = AccentNeon,
            width = 68.dp,
            height = 48.dp,
            onClick = onRepeatToggle
        )

        MechanicalKey(
            label = "10-EQ",
            icon = Icons.Default.Tune,
            isActive = false,
            accentColor = AccentGreen,
            width = 72.dp,
            height = 48.dp,
            onClick = onEqClick
        )

        MechanicalKey(
            label = "LYRIC",
            icon = Icons.Default.FormatQuote,
            isActive = false,
            accentColor = AccentAmber,
            width = 72.dp,
            height = 48.dp,
            onClick = onLyricsClick
        )
    }
}
