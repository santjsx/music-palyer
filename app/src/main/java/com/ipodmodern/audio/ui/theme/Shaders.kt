package com.ipodmodern.audio.ui.theme

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import org.intellij.lang.annotations.Language

object Shaders {

    @Language("AGSL")
    const val BRUSHED_METAL_SHADER = """
        uniform float2 resolution;
        uniform float time;
        uniform float3 baseColor;
        uniform float3 highlightColor;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / resolution;
            
            // Linear brushed micro-streaks
            float noise = fract(sin(dot(float2(fragCoord.x, 0.0), float2(12.9898, 78.233))) * 43758.5453);
            float grain = (noise - 0.5) * 0.08;
            
            // Radial highlight from top-center
            float distFromLight = distance(uv, float2(0.5, -0.2));
            float spec = clamp(1.0 - distFromLight * 0.9, 0.0, 1.0);
            
            float3 col = mix(baseColor, highlightColor, spec * 0.45) + grain;
            return half4(col, 1.0);
        }
    """

    @Language("AGSL")
    const val RETRO_LCD_DOT_MATRIX = """
        uniform float2 resolution;
        uniform half4 inputColor;

        half4 main(float2 fragCoord) {
            // 2px dot-matrix grid
            float2 grid = mod(fragCoord, 2.0);
            float scan = (grid.x > 1.0 || grid.y > 1.0) ? 0.92 : 1.0;
            return half4(inputColor.rgb * scan, inputColor.a);
        }
    """

    fun createBrushedMetalShader(width: Float, height: Float): RuntimeShader? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shader = RuntimeShader(BRUSHED_METAL_SHADER)
            shader.setFloatUniform("resolution", width, height)
            shader.setFloatUniform("time", 0.0f)
            shader.setFloatUniform("baseColor", 0.15f, 0.16f, 0.18f)
            shader.setFloatUniform("highlightColor", 0.28f, 0.30f, 0.35f)
            return shader
        }
        return null
    }
}
