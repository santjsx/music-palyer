package com.ipodmodern.audio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.model.AudioQuality
import com.ipodmodern.audio.ui.theme.AudiophileGold
import com.ipodmodern.audio.ui.theme.HiResGoldBg
import com.ipodmodern.audio.ui.theme.LosslessGreen
import com.ipodmodern.audio.ui.theme.LosslessGreenBg
import com.ipodmodern.audio.ui.theme.LossyGray
import com.ipodmodern.audio.ui.theme.LossyGrayBg

@Composable
fun AudioQualityBadge(
    quality: AudioQuality,
    badgeText: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (quality) {
        AudioQuality.HI_RES_LOSSLESS -> Triple(HiResGoldBg, AudiophileGold, AudiophileGold)
        AudioQuality.LOSSLESS -> Triple(LosslessGreenBg, LosslessGreen, LosslessGreen.copy(alpha = 0.5f))
        AudioQuality.LOSSY -> Triple(LossyGrayBg, LossyGray, LossyGray.copy(alpha = 0.3f))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = badgeText.uppercase(),
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun BatteryIndicator(
    levelPercent: Int = 85,
    isCharging: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(10.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(2.dp))
                .padding(1.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeightWidth(levelPercent / 100f)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (levelPercent > 20) LosslessGreen else Color(0xFFFF453A))
            )
        }
        Spacer(modifier = Modifier.width(1.dp))
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .height(4.dp)
                .background(Color.Gray, RoundedCornerShape(topEnd = 1.dp, bottomEnd = 1.dp))
        )
    }
}

private fun Modifier.fillMaxHeightWidth(fraction: Float) = this.then(
    Modifier.size(width = (17 * fraction).dp, height = 7.dp)
)
