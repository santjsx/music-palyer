package com.ipodmodern.audio.core.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.ipodmodern.audio.core.audio.NativeAudioBridge

class HapticEngine(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var hapticsEnabled: Boolean = true
    private var audioClickEnabled: Boolean = true

    fun setHapticsEnabled(enabled: Boolean) {
        hapticsEnabled = enabled
    }

    fun setAudioClickEnabled(enabled: Boolean) {
        audioClickEnabled = enabled
        NativeAudioBridge.setClickEnabled(enabled)
    }

    /**
     * Micro-tick on 15° rotational displacement
     */
    fun performTick() {
        if (audioClickEnabled) {
            NativeAudioBridge.triggerNativeClick(0.75f)
        }

        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK)) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.65f)
                        .compose()
                    vibrator.vibrate(effect)
                    return
                }
            } catch (e: Exception) {
                // Fallthrough to standard vibration
            }
        }

        // Fallback for API 29-30
        try {
            val effect = VibrationEffect.createOneShot(8, 40)
            vibrator.vibrate(effect)
        } catch (e: Exception) {
            // Ignore if device doesn't support amplitude control
        }
    }

    /**
     * Solid center button click actuation
     */
    fun performClick() {
        if (audioClickEnabled) {
            NativeAudioBridge.triggerNativeClick(1.0f)
        }

        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f)
                        .compose()
                    vibrator.vibrate(effect)
                    return
                }
            } catch (e: Exception) {
                // Fallthrough
            }
        }

        try {
            val effect = VibrationEffect.createOneShot(20, 180)
            vibrator.vibrate(effect)
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Boundary limit bounce / thud
     */
    fun performThud() {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                if (vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD)) {
                    val effect = VibrationEffect.startComposition()
                        .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.9f)
                        .compose()
                    vibrator.vibrate(effect)
                    return
                }
            } catch (e: Exception) {
                // Fallthrough
            }
        }

        try {
            val effect = VibrationEffect.createOneShot(28, 220)
            vibrator.vibrate(effect)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
