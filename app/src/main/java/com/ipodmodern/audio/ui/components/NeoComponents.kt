package com.ipodmodern.audio.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.theme.NeoBlack
import com.ipodmodern.audio.ui.theme.NeoBlue
import com.ipodmodern.audio.ui.theme.NeoBorderThick
import com.ipodmodern.audio.ui.theme.NeoBorderWidth
import com.ipodmodern.audio.ui.theme.NeoGreen
import com.ipodmodern.audio.ui.theme.NeoPink
import com.ipodmodern.audio.ui.theme.NeoPurple
import com.ipodmodern.audio.ui.theme.NeoRadiusMd
import com.ipodmodern.audio.ui.theme.NeoRadiusSm
import com.ipodmodern.audio.ui.theme.NeoShadowOffset
import com.ipodmodern.audio.ui.theme.NeoShadowOffsetSmall
import com.ipodmodern.audio.ui.theme.NeoWhite
import com.ipodmodern.audio.ui.theme.NeoYellow

/**
 * Neo-Brutalist Solid Shadow Modifier.
 * Draws an unblurred, hard solid black drop shadow behind the element.
 */
fun Modifier.neoShadow(
    offsetX: Dp = NeoShadowOffset,
    offsetY: Dp = NeoShadowOffset,
    color: Color = NeoBlack,
    cornerRadius: Dp = 12.dp
): Modifier = this.drawBehind {
    val cornerPx = cornerRadius.toPx()
    val offX = offsetX.toPx()
    val offY = offsetY.toPx()

    if (offX != 0f || offY != 0f) {
        drawRoundRect(
            color = color,
            topLeft = Offset(offX, offY),
            size = size,
            cornerRadius = CornerRadius(cornerPx, cornerPx)
        )
    }
}

/**
 * Neo-Brutalist Interactive Card Container with Emil Kowalski Spring Mechanics.
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoWhite,
    borderColor: Color = NeoBlack,
    borderWidth: Dp = NeoBorderWidth,
    shadowOffset: Dp = NeoShadowOffset,
    cornerRadius: Dp = 12.dp,
    shape: Shape = RoundedCornerShape(cornerRadius),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val currentScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 1400f),
        label = "neo_card_scale"
    )
    val currentOffset by animateDpAsState(
        targetValue = if (isPressed && onClick != null) 2.dp else 0.dp,
        animationSpec = spring(stiffness = 2400f),
        label = "neo_card_press"
    )
    val currentShadow by animateDpAsState(
        targetValue = if (isPressed && onClick != null) (shadowOffset - 2.dp).coerceAtLeast(0.dp) else shadowOffset,
        animationSpec = spring(stiffness = 2400f),
        label = "neo_card_shadow"
    )

    Box(
        modifier = modifier
            .neoShadow(
                offsetX = currentShadow,
                offsetY = currentShadow,
                color = borderColor,
                cornerRadius = cornerRadius
            )
            .offset(x = currentOffset, y = currentOffset)
            .graphicsLayer {
                scaleX = currentScale
                scaleY = currentScale
            }
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
            .then(
                if (onClick != null) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = { onClick() }
                        )
                    }
                } else Modifier
            )
    ) {
        content()
    }
}

/**
 * Neo-Brutalist Push-Down Button with Tactile Springs.
 */
@Composable
fun NeoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoYellow,
    textColor: Color = NeoBlack,
    borderColor: Color = NeoBlack,
    borderWidth: Dp = NeoBorderWidth,
    cornerRadius: Dp = 12.dp,
    icon: ImageVector? = null
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val currentScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 1600f),
        label = "neo_btn_scale"
    )
    val currentOffset by animateDpAsState(
        targetValue = if (isPressed) NeoShadowOffset else 0.dp,
        animationSpec = spring(stiffness = 2600f),
        label = "neo_btn_press"
    )
    val currentShadow by animateDpAsState(
        targetValue = if (isPressed) 0.dp else NeoShadowOffset,
        animationSpec = spring(stiffness = 2600f),
        label = "neo_btn_shadow"
    )

    Box(
        modifier = modifier
            .neoShadow(
                offsetX = currentShadow,
                offsetY = currentShadow,
                color = borderColor,
                cornerRadius = cornerRadius
            )
            .offset(x = currentOffset, y = currentOffset)
            .graphicsLayer {
                scaleX = currentScale
                scaleY = currentScale
            }
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Neo-Brutalist Square/Circle Icon Button with Spring Press.
 */
@Composable
fun NeoIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoWhite,
    tint: Color = NeoBlack,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    isCircle: Boolean = false,
    cornerRadius: Dp = if (isCircle) 9999.dp else 12.dp
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val currentScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 1800f),
        label = "neo_icon_btn_scale"
    )
    val currentOffset by animateDpAsState(
        targetValue = if (isPressed) NeoShadowOffsetSmall else 0.dp,
        animationSpec = spring(stiffness = 2600f),
        label = "neo_icon_btn_press"
    )
    val currentShadow by animateDpAsState(
        targetValue = if (isPressed) 0.dp else NeoShadowOffsetSmall,
        animationSpec = spring(stiffness = 2600f),
        label = "neo_icon_btn_shadow"
    )

    val shape = if (isCircle) CircleShape else RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(size)
            .neoShadow(
                offsetX = currentShadow,
                offsetY = currentShadow,
                color = NeoBlack,
                cornerRadius = if (isCircle) size / 2 else cornerRadius
            )
            .offset(x = currentOffset, y = currentOffset)
            .graphicsLayer {
                scaleX = currentScale
                scaleY = currentScale
            }
            .clip(shape)
            .background(backgroundColor)
            .border(NeoBorderWidth, NeoBlack, shape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Neo-Brutalist High-Contrast Pill Badge.
 */
@Composable
fun NeoBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NeoYellow,
    textColor: Color = NeoBlack,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .neoShadow(
                offsetX = 2.dp,
                offsetY = 2.dp,
                color = NeoBlack,
                cornerRadius = cornerRadius
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(2.dp, NeoBlack, RoundedCornerShape(cornerRadius))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Rotating Vinyl Disc / CD Groove Micro-Interaction.
 * Spins smoothly when music is playing, seamlessly settles when paused.
 */
@Composable
fun NeoVinylSpinBadge(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    val currentRotation = if (isPlaying) angle else 0f

    Box(
        modifier = modifier
            .size(size)
            .neoShadow(offsetX = 2.dp, offsetY = 2.dp, color = NeoBlack, cornerRadius = size / 2)
            .rotate(currentRotation)
            .clip(CircleShape)
            .background(NeoBlack)
            .border(2.dp, NeoBlack, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val maxR = size.toPx() / 2f

            // Vinyl grooves
            drawCircle(color = Color(0xFF222222), radius = maxR * 0.85f, center = center, style = Stroke(width = 1.2f))
            drawCircle(color = Color(0xFF1A1A1A), radius = maxR * 0.70f, center = center, style = Stroke(width = 1.2f))
            drawCircle(color = Color(0xFF282828), radius = maxR * 0.55f, center = center, style = Stroke(width = 1.2f))

            // Center Label (Electric Yellow)
            drawCircle(color = NeoYellow, radius = maxR * 0.38f, center = center)
            drawCircle(color = NeoBlack, radius = maxR * 0.38f, center = center, style = Stroke(width = 2f))

            // Center Hole
            drawCircle(color = NeoBlack, radius = maxR * 0.12f, center = center)
        }
    }
}

/**
 * Neo-Brutalist Real-Time Stereo VU-Meter Bars.
 */
@Composable
fun NeoVuMeter(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "vu_meter")

    val levelL by transition.animateFloat(
        initialValue = 0.2f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(260, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "vu_l"
    )
    val levelR by transition.animateFloat(
        initialValue = 0.35f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(320, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "vu_r"
    )

    Row(
        modifier = modifier
            .neoShadow(offsetX = 2.dp, offsetY = 2.dp, color = NeoBlack, cornerRadius = 6.dp)
            .background(NeoWhite, RoundedCornerShape(6.dp))
            .border(2.dp, NeoBlack, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel L
        Text("L", fontSize = 9.sp, fontWeight = FontWeight.Black, color = NeoBlack, fontFamily = FontFamily.Monospace)
        VuSingleBar(level = if (isPlaying) levelL else 0.05f)

        Spacer(modifier = Modifier.width(2.dp))

        // Channel R
        Text("R", fontSize = 9.sp, fontWeight = FontWeight.Black, color = NeoBlack, fontFamily = FontFamily.Monospace)
        VuSingleBar(level = if (isPlaying) levelR else 0.05f)
    }
}

@Composable
private fun VuSingleBar(level: Float) {
    Row(
        modifier = Modifier
            .width(36.dp)
            .height(8.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFFEEEEEE))
            .border(1.dp, NeoBlack, RoundedCornerShape(2.dp)),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        val totalSegments = 6
        val activeSegments = (level * totalSegments).toInt().coerceIn(0, totalSegments)

        for (i in 0 until totalSegments) {
            val color = when {
                i < 3 -> NeoGreen
                i < 5 -> NeoYellow
                else -> NeoPink
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (i < activeSegments) color else Color.Transparent)
            )
        }
    }
}
