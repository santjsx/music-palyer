package com.ipodmodern.audio.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.ipodmodern.audio.ui.theme.NeoBgDark
import com.ipodmodern.audio.ui.theme.NeoBlack
import com.ipodmodern.audio.ui.theme.NeoBorderThick
import com.ipodmodern.audio.ui.theme.NeoBorderWidth
import com.ipodmodern.audio.ui.theme.NeoGreen
import com.ipodmodern.audio.ui.theme.NeoPink
import com.ipodmodern.audio.ui.theme.NeoPurple
import com.ipodmodern.audio.ui.theme.NeoRadiusLg
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
 * Neo-Brutalist Interactive Card Container.
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

    val currentOffset by animateDpAsState(
        targetValue = if (isPressed && onClick != null) 2.dp else 0.dp,
        animationSpec = spring(stiffness = 2000f),
        label = "neo_card_press"
    )
    val currentShadow by animateDpAsState(
        targetValue = if (isPressed && onClick != null) (shadowOffset - 2.dp).coerceAtLeast(0.dp) else shadowOffset,
        animationSpec = spring(stiffness = 2000f),
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
 * Neo-Brutalist Push-Down Button.
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

    val currentOffset by animateDpAsState(
        targetValue = if (isPressed) NeoShadowOffset else 0.dp,
        animationSpec = spring(stiffness = 2400f),
        label = "neo_btn_press"
    )
    val currentShadow by animateDpAsState(
        targetValue = if (isPressed) 0.dp else NeoShadowOffset,
        animationSpec = spring(stiffness = 2400f),
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
 * Neo-Brutalist Square/Circle Icon Button.
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

    val currentOffset by animateDpAsState(
        targetValue = if (isPressed) NeoShadowOffsetSmall else 0.dp,
        animationSpec = spring(stiffness = 2400f),
        label = "neo_icon_btn_press"
    )
    val currentShadow by animateDpAsState(
        targetValue = if (isPressed) 0.dp else NeoShadowOffsetSmall,
        animationSpec = spring(stiffness = 2400f),
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
