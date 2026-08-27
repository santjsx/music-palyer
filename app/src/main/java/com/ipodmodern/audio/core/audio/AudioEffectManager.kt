package com.ipodmodern.audio.core.audio

import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Hardware-accelerated AudioEffectManager for native Android Equalizer
 * and LoudnessEnhancer effects attached directly to the playback audioSessionId.
 */
class AudioEffectManager {

    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    // UI state streams for smooth slider mapping
    private val _eqEnabled = MutableStateFlow(true)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled

    /**
     * Call this as soon as the playback engine initializes or provides an Audio Session ID.
     */
    fun initAudioEffects(audioSessionId: Int) {
        if (audioSessionId == 0) return
        try {
            release()
            // Priority 0 = Lowest priority, standard app usage
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _eqEnabled.value
            }

            // Loudness Enhancer prevents volume drops when EQing down frequencies
            try {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    enabled = _eqEnabled.value
                }
            } catch (e: Exception) {
                Log.w("AudioEffectManager", "LoudnessEnhancer not supported on this device/ROM", e)
            }
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Failed to initialize native audio hardware effects", e)
        }
    }

    fun toggleEqualizer(isEnabled: Boolean) {
        _eqEnabled.value = isEnabled
        try {
            equalizer?.enabled = isEnabled
            loudnessEnhancer?.enabled = isEnabled
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Error toggling equalizer", e)
        }
    }

    fun getBandCount(): Short {
        return try {
            equalizer?.numberOfBands ?: 5
        } catch (e: Exception) {
            5
        }
    }

    fun getBandCenterFrequency(band: Short): Int {
        return try {
            equalizer?.getCenterFreq(band) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun getBandLevelRange(): ShortArray {
        return try {
            equalizer?.bandLevelRange ?: shortArrayOf(-1200, 1200)
        } catch (e: Exception) {
            shortArrayOf(-1200, 1200)
        }
    }

    /**
     * Sets the gain for a specific frequency band.
     * @param levelDb Gain in Decibels (dB). Internally converted to milliBels (* 100) for Android's API.
     */
    fun setBandLevel(band: Short, levelDb: Float) {
        val milliBels = (levelDb * 100).toInt().coerceIn(-1200, 1200).toShort()
        try {
            val eq = equalizer ?: return
            val numBands = eq.numberOfBands
            if (band in 0 until numBands) {
                val range = eq.bandLevelRange
                val clampedMb = milliBels.coerceIn(range[0], range[1])
                eq.setBandLevel(band, clampedMb)
            }
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Error setting band level: $band -> ${levelDb}dB", e)
        }
    }

    fun setTargetGain(gainDb: Float) {
        try {
            val le = loudnessEnhancer ?: return
            val mB = (gainDb * 100).toInt().coerceIn(0, 1000)
            le.setTargetGain(mB)
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Error setting LoudnessEnhancer target gain", e)
        }
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Error releasing Equalizer", e)
        } finally {
            equalizer = null
        }

        try {
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Error releasing LoudnessEnhancer", e)
        } finally {
            loudnessEnhancer = null
        }
    }
}
