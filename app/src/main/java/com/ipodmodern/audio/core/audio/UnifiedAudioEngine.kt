package com.ipodmodern.audio.core.audio

import android.content.Context
import android.media.audiofx.Equalizer
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

class UnifiedAudioEngine(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null
    private var equalizer: Equalizer? = null

    init {
        initPlayer()
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            volume = 1.0f
        }
        setupEqualizer()
    }

    private fun setupEqualizer() {
        try {
            val sessionId = exoPlayer?.audioSessionId ?: 0
            if (sessionId != 0) {
                equalizer = Equalizer(0, sessionId).apply {
                    enabled = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAndPlay(filePath: String, autoPlay: Boolean = true) {
        try {
            val player = exoPlayer ?: return
            val uri = if (filePath.startsWith("sample://")) {
                // For sample mock paths, fallback or asset
                Uri.parse("file://$filePath")
            } else if (filePath.startsWith("content://") || filePath.startsWith("file://")) {
                Uri.parse(filePath)
            } else {
                Uri.fromFile(File(filePath))
            }

            val mediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = autoPlay
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun stop() {
        exoPlayer?.stop()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun setVolume(volume: Float) {
        exoPlayer?.volume = volume.coerceIn(0.0f, 1.0f)
    }

    fun getVolume(): Float {
        return exoPlayer?.volume ?: 1.0f
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying == true
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }

    fun getDuration(): Long {
        val dur = exoPlayer?.duration ?: 0L
        return if (dur > 0) dur else 0L
    }

    fun setEqBandGain(bandIndex: Int, gainDb: Float) {
        try {
            val eq = equalizer ?: return
            val numBands = eq.numberOfBands.toInt()
            if (bandIndex in 0 until numBands) {
                // Android equalizer gain is in millibels (1 dB = 100 mB)
                val minLevel = eq.bandLevelRange[0].toInt()
                val maxLevel = eq.bandLevelRange[1].toInt()
                val mB = (gainDb * 100).toInt().coerceIn(minLevel, maxLevel).toShort()
                eq.setBandLevel(bandIndex.toShort(), mB)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        equalizer?.release()
        equalizer = null
        exoPlayer?.release()
        exoPlayer = null
    }
}
