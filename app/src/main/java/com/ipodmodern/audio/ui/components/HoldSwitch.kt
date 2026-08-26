package com.ipodmodern.audio.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.theme.HoldSwitchOff
import com.ipodmodern.audio.ui.theme.HoldSwitchOrange

@Composable
fun HoldSwitch(
    isHoldActive: Boolean,
    onToggleHold: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val sliderOffset by animateDpAsState(
        targetValue = if (isHoldActive) 16.dp else 0.dp,
        animationSpec = tween(durationMillis = 180),
        label = "hold_slider"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = "HOLD",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHoldActive) HoldSwitchOrange else Color.Gray,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(end = 6.dp)
        )

        // Physical slider track
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(
                    if (isHoldActive) HoldSwitchOrange.copy(alpha = 0.4f) else HoldSwitchOff
                )
                .border(1.dp, Color(0xFF1E2024), RoundedCornerShape(7.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onToggleHold(!isHoldActive)
                }
                .padding(1.5.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Orange active sliver visible when locked
            if (isHoldActive) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(11.dp)
                        .background(HoldSwitchOrange, RoundedCornerShape(5.dp))
                )
            }

            // Mechanical Slider Knob
            Box(
                modifier = Modifier
                    .offset(x = sliderOffset)
                    .size(width = 17.dp, height = 11.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFE5E5EA), Color(0xFF8E8E93))
                        )
                    )
                    .border(0.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(5.dp))
            )
        }
    }
}
