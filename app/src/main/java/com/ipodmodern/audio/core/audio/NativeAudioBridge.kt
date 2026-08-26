package com.ipodmodern.audio.core.audio

import com.ipodmodern.audio.core.model.NativeAudioMetadata

object NativeAudioBridge {

    init {
        try {
            System.loadLibrary("ipod_audio_engine")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    external fun initEngine(sampleRate: Int, exclusiveMode: Boolean): Boolean
    external fun destroyEngine()
    external fun loadTrack(filePath: String): Boolean
    external fun play()
    external fun pause()
    external fun stop()
    external fun seekTo(positionMs: Long)
    external fun setVolume(volume: Float)
    external fun triggerNativeClick(volume: Float)
    external fun setClickEnabled(enabled: Boolean)

    // Equalizer controls
    external fun setEqBandGain(bandIndex: Int, gainDb: Float)
    external fun setEqAllBands(gainsArray: FloatArray)
    external fun setEqEnabled(enabled: Boolean)
    external fun getDynamicPrecutGainDb(): Float

    // Engine state
    external fun getCurrentPositionMs(): Long
    external fun getDurationMs(): Long
    external fun isPlaying(): Boolean
    external fun inspectFileMetadata(filePath: String): NativeAudioMetadata?
}
