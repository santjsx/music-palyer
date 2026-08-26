package com.ipodmodern.audio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ChassisMaterial {
    CLASSIC_SILVER,
    SPACE_TITANIUM,
    STEALTH_OBSIDIAN,
    RETRO_LCD
}

data class iPodColors(
    val chassisBackground: Color,
    val chassisHighlight: Color,
    val wheelBackground: Color,
    val wheelText: Color,
    val centerButton: Color,
    val screenBackground: Color,
    val screenText: Color,
    val isDarkScreen: Boolean
)

val LocalIpodColors = staticCompositionLocalOf {
    iPodColors(
        chassisBackground = TitaniumBody,
        chassisHighlight = TitaniumBodyHighlight,
        wheelBackground = TitaniumWheel,
        wheelText = TitaniumWheelText,
        centerButton = TitaniumCenterButton,
        screenBackground = TitaniumScreenBg,
        screenText = TitaniumMenuText,
        isDarkScreen = true
    )
}

@Composable
fun IPodModernTheme(
    chassis: ChassisMaterial = ChassisMaterial.SPACE_TITANIUM,
    content: @Composable () -> Unit
) {
    val colors = when (chassis) {
        ChassisMaterial.CLASSIC_SILVER -> iPodColors(
            chassisBackground = ClassicBodySilver,
            chassisHighlight = ClassicBodyHighlight,
            wheelBackground = ClassicWheelGray,
            wheelText = ClassicWheelText,
            centerButton = ClassicCenterButton,
            screenBackground = ClassicScreenBg,
            screenText = ClassicMenuText,
            isDarkScreen = false
        )
        ChassisMaterial.SPACE_TITANIUM -> iPodColors(
            chassisBackground = TitaniumBody,
            chassisHighlight = TitaniumBodyHighlight,
            wheelBackground = TitaniumWheel,
            wheelText = TitaniumWheelText,
            centerButton = TitaniumCenterButton,
            screenBackground = TitaniumScreenBg,
            screenText = TitaniumMenuText,
            isDarkScreen = true
        )
        ChassisMaterial.STEALTH_OBSIDIAN -> iPodColors(
            chassisBackground = ObsidianBody,
            chassisHighlight = Color(0xFF22252B),
            wheelBackground = ObsidianWheel,
            wheelText = Color(0xFF7A7E85),
            centerButton = ObsidianCenter,
            screenBackground = ObsidianScreenBg,
            screenText = Color(0xFFFFFFFF),
            isDarkScreen = true
        )
        ChassisMaterial.RETRO_LCD -> iPodColors(
            chassisBackground = ClassicBodySilver,
            chassisHighlight = ClassicBodyHighlight,
            wheelBackground = ClassicWheelGray,
            wheelText = ClassicWheelText,
            centerButton = ClassicCenterButton,
            screenBackground = RetroLcdBg,
            screenText = RetroLcdText,
            isDarkScreen = false
        )
    }

    CompositionLocalProvider(LocalIpodColors provides colors) {
        MaterialTheme(
            typography = iPodTypography,
            content = content
        )
    }
}
