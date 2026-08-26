package com.ipodmodern.audio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.theme.LocalIpodColors

@Composable
fun ChassisContainer(
    isHoldActive: Boolean,
    onToggleHold: (Boolean) -> Unit,
    screenContent: @Composable () -> Unit,
    wheelContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalIpodColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Unibody iPod Chassis
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .shadow(24.dp, RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.chassisHighlight,
                            colors.chassisBackground,
                            colors.chassisBackground,
                            colors.chassisBackground.copy(alpha = 0.95f)
                        )
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(32.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Hardware Header: Brand Label + Hold Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "iPod",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.wheelText,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    HoldSwitch(
                        isHoldActive = isHoldActive,
                        onToggleHold = onToggleHold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Display Bezel Window (Top Half)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.05f)
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .background(Color(0xFF101214))
                        .border(2.dp, Color(0xFF22252A), RoundedCornerShape(12.dp))
                        .padding(6.dp)
                ) {
                    // Inner Screen Surface
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.screenBackground)
                    ) {
                        screenContent()
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Click Wheel Controller Module (Bottom Half)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.95f),
                    contentAlignment = Alignment.Center
                ) {
                    wheelContent()
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
