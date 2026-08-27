package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.model.EqualizerPreset
import com.ipodmodern.audio.ui.components.NeoBadge
import com.ipodmodern.audio.ui.components.NeoCard
import com.ipodmodern.audio.ui.components.NeoIconButton
import com.ipodmodern.audio.ui.theme.NeoBgDark
import com.ipodmodern.audio.ui.theme.NeoBlack
import com.ipodmodern.audio.ui.theme.NeoBlue
import com.ipodmodern.audio.ui.theme.NeoBorderWidth
import com.ipodmodern.audio.ui.theme.NeoGreen
import com.ipodmodern.audio.ui.theme.NeoMuted
import com.ipodmodern.audio.ui.theme.NeoPink
import com.ipodmodern.audio.ui.theme.NeoPurple
import com.ipodmodern.audio.ui.theme.NeoRadiusLg
import com.ipodmodern.audio.ui.theme.NeoRadiusMd
import com.ipodmodern.audio.ui.theme.NeoRadiusSm
import com.ipodmodern.audio.ui.theme.NeoWhite
import com.ipodmodern.audio.ui.theme.NeoYellow
import java.util.Locale

val NEO_BAND_LABELS = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

val NEO_STUDIO_PRESETS = listOf(
    EqualizerPreset("Flat", FloatArray(10) { 0.0f }),
    EqualizerPreset("Master", floatArrayOf(2.5f, 2.0f, 1.0f, 0.0f, 0.0f, 0.5f, 1.5f, 2.0f, 2.5f, 3.0f)),
    EqualizerPreset("Bass Surge", floatArrayOf(6.0f, 5.0f, 3.5f, 1.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.5f)),
    EqualizerPreset("Vocal Focus", floatArrayOf(-2.0f, -1.0f, 0.0f, 1.0f, 3.0f, 4.0f, 3.0f, 1.0f, 0.0f, -1.0f)),
    EqualizerPreset("Electronic", floatArrayOf(5.0f, 4.0f, 1.5f, 0.0f, -1.5f, 1.5f, 2.0f, 3.0f, 4.5f, 5.0f)),
    EqualizerPreset("Acoustic", floatArrayOf(2.5f, 2.0f, 1.0f, 1.0f, 1.5f, 2.0f, 3.0f, 3.0f, 2.5f, 2.0f)),
    EqualizerPreset("Rock", floatArrayOf(4.5f, 3.5f, 2.0f, 0.5f, -1.0f, -0.5f, 2.0f, 3.5f, 4.0f, 4.5f))
)

@Composable
fun EqualizerScreen(
    bandGains: FloatArray,
    selectedBandIndex: Int = 0,
    onBandGainChange: (Int, Float) -> Unit,
    onPresetSelect: (EqualizerPreset) -> Unit,
    dynamicPrecutDb: Float = 0.0f,
    presetName: String = "Neo Flat",
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(NeoBgDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .padding(bottom = 140.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NEO DSP STUDIO",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoYellow,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "10-BAND PARAMETRIC EQUALIZER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = NeoWhite
                )
            }

            NeoIconButton(
                icon = Icons.Default.RestartAlt,
                contentDescription = "Reset EQ",
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onPresetSelect(EqualizerPreset("Flat", FloatArray(10) { 0.0f }))
                },
                backgroundColor = NeoYellow,
                size = 40.dp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Neo Response Curve Box
        NeoCard(
            backgroundColor = NeoWhite,
            cornerRadius = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                val width = size.width
                val height = size.height
                val midY = height / 2f

                // Draw Grid
                drawLine(
                    color = NeoBlack.copy(alpha = 0.3f),
                    start = Offset(0f, midY),
                    end = Offset(width, midY),
                    strokeWidth = 2f
                )

                // Response Curve
                val path = Path()
                val stepX = width / 9f

                for (i in 0 until 10) {
                    val gain = bandGains.getOrElse(i) { 0.0f }
                    val y = midY - (gain / 12.0f) * (height / 2.2f)
                    val x = i * stepX

                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        val prevGain = bandGains.getOrElse(i - 1) { 0.0f }
                        val prevY = midY - (prevGain / 12.0f) * (height / 2.2f)
                        val prevX = (i - 1) * stepX
                        val cx = (prevX + x) / 2f
                        path.cubicTo(cx, prevY, cx, y, x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = NeoBlack,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Peak Points
                for (i in 0 until 10) {
                    val gain = bandGains.getOrElse(i) { 0.0f }
                    val y = midY - (gain / 12.0f) * (height / 2.2f)
                    val x = i * stepX
                    drawCircle(
                        color = NeoYellow,
                        radius = 5.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = NeoBlack,
                        radius = 5.dp.toPx(),
                        center = Offset(x, y),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NEO_STUDIO_PRESETS.forEach { preset ->
                val isSelected = preset.name.equals(presetName, ignoreCase = true)
                NeoCard(
                    backgroundColor = if (isSelected) NeoYellow else NeoWhite,
                    shadowOffset = if (isSelected) 3.dp else 2.dp,
                    cornerRadius = 10.dp,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onPresetSelect(preset)
                    }
                ) {
                    Text(
                        text = preset.name.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 10 Precision Vertical Faders
        NeoCard(
            backgroundColor = NeoWhite,
            cornerRadius = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 10) {
                    val gain = bandGains.getOrElse(i) { 0.0f }
                    NeoFaderColumn(
                        label = NEO_BAND_LABELS[i],
                        gainDb = gain,
                        onGainChange = { newGain -> onBandGainChange(i, newGain) }
                    )
                }
            }
        }
    }
}

@Composable
fun NeoFaderColumn(
    label: String,
    gainDb: Float,
    onGainChange: (Float) -> Unit
) {
    val view = LocalView.current
    val normalizedGain = ((gainDb + 12f) / 24f).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "${if (gainDb > 0) "+" else ""}${gainDb.toInt()}",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = NeoBlack
        )

        // Fader Track
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(NeoBgDark)
                .border(2.dp, NeoBlack, RoundedCornerShape(9.dp))
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaDb = -dragAmount / 5f
                        val newGain = (gainDb + deltaDb).coerceIn(-12f, 12f)
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onGainChange(newGain)
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(normalizedGain)
                    .background(NeoYellow)
            )

            // Center indicator thumb
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(NeoWhite)
                    .border(2.dp, NeoBlack, CircleShape)
            )
        }

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = NeoBlack
        )
    }
}
