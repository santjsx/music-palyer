package com.ipodmodern.audio.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.haptics.WheelKinematicsCalculator
import com.ipodmodern.audio.ui.theme.LocalIpodColors
import kotlin.math.atan2
import kotlin.math.hypot

@Composable
fun ClickWheel(
    wheelDiameter: Dp = 260.dp,
    onMenuClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onCenterClick: () -> Unit,
    onWheelRotate: (Int) -> Unit, // +1 for CW, -1 for CCW per 15-deg tick
    isHoldActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = LocalIpodColors.current
    val kinematics = remember { WheelKinematicsCalculator(tickStepDegrees = 15.0) }

    var centerOffset by remember { mutableStateOf(Offset.Zero) }
    var isCenterPressed by remember { mutableStateOf(false) }

    val centerButtonScale by animateFloatAsState(
        targetValue = if (isCenterPressed) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 80),
        label = "center_scale"
    )

    Box(
        modifier = modifier
            .size(wheelDiameter)
            .onGloballyPositioned { coordinates ->
                val size = coordinates.size
                centerOffset = Offset(size.width / 2f, size.height / 2f)
            }
            .clip(CircleShape)
            .shadow(12.dp, CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        colors.wheelBackground.copy(alpha = 0.95f),
                        colors.wheelBackground,
                        colors.wheelBackground.copy(alpha = 0.85f)
                    )
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            .pointerInput(isHoldActive) {
                if (isHoldActive) return@pointerInput

                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        val touchPos = change.position
                        val dx = touchPos.x - centerOffset.x
                        val dy = touchPos.y - centerOffset.y
                        val dist = hypot(dx, dy)
                        val innerRadiusPx = (wheelDiameter.toPx() * 0.35f) / 2f

                        // Only track orbital rotation outside inner center button
                        if (dist >= innerRadiusPx) {
                            if (change.pressed) {
                                val ticks = kinematics.onTouchMove(
                                    centerX = centerOffset.x,
                                    centerY = centerOffset.y,
                                    touchX = touchPos.x,
                                    touchY = touchPos.y,
                                    timestampNs = change.uptimeMillis * 1_000_000L
                                )
                                if (ticks != 0) {
                                    onWheelRotate(ticks)
                                }
                                change.consume()
                            } else {
                                kinematics.reset(
                                    centerX = centerOffset.x,
                                    centerY = centerOffset.y,
                                    touchX = touchPos.x,
                                    touchY = touchPos.y
                                )
                            }
                        }
                    }
                }
            }
            .pointerInput(isHoldActive) {
                if (isHoldActive) return@pointerInput

                detectTapGestures(
                    onPress = { offset ->
                        val dx = offset.x - centerOffset.x
                        val dy = offset.y - centerOffset.y
                        val dist = hypot(dx, dy)
                        val innerRadiusPx = (wheelDiameter.toPx() * 0.35f) / 2f

                        if (dist < innerRadiusPx) {
                            isCenterPressed = true
                            tryAwaitRelease()
                            isCenterPressed = false
                        }
                    },
                    onTap = { offset ->
                        val dx = offset.x - centerOffset.x
                        val dy = offset.y - centerOffset.y
                        val dist = hypot(dx, dy)
                        val innerRadiusPx = (wheelDiameter.toPx() * 0.35f) / 2f

                        if (dist < innerRadiusPx) {
                            onCenterClick()
                        } else {
                            // Quadrant Button Detection
                            // Angle in degrees [0..360] where 0 is Right, 90 is Down, 180 is Left, 270 is Up
                            var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                            if (angleDeg < 0) angleDeg += 360.0

                            when {
                                angleDeg in 225.0..315.0 -> onMenuClick() // Top (MENU)
                                angleDeg in 45.0..135.0 -> onPlayPauseClick() // Bottom (Play/Pause)
                                angleDeg in 315.0..360.0 || angleDeg in 0.0..45.0 -> onNextClick() // Right (Next)
                                angleDeg in 135.0..225.0 -> onPrevClick() // Left (Prev)
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Quadrant Labels
        // 1. Top: MENU
        Text(
            text = "MENU",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colors.wheelText,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 18.dp)
        )

        // 2. Bottom: Play / Pause
        Text(
            text = "▶ ❚❚",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.wheelText,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-18).dp)
        )

        // 3. Right: Fast Forward / Next
        Text(
            text = "▶▶❚",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.wheelText,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-18).dp)
        )

        // 4. Left: Rewind / Previous
        Text(
            text = "❚◀◀",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.wheelText,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 18.dp)
        )

        // Center Action Button
        Box(
            modifier = Modifier
                .size(wheelDiameter * 0.35f)
                .scale(centerButtonScale)
                .clip(CircleShape)
                .shadow(6.dp, CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            colors.centerButton,
                            colors.centerButton.copy(alpha = 0.9f)
                        )
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.18f), CircleShape)
        )
    }
}
