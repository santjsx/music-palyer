package com.ipodmodern.audio.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.net.Uri
import java.io.File

class UnifiedAudioEngine(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var currentFilePath: String? = null
    private var currentVolume: Float = 1.0f
    var onPlaybackCompleted: (() -> Unit)? = null

    init {
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setVolume(currentVolume, currentVolume)
                setOnCompletionListener {
                    onPlaybackCompleted?.invoke()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupEqualizer(audioSessionId: Int) {
        try {
            equalizer?.release()
            if (audioSessionId != 0) {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAndPlay(filePath: String, autoPlay: Boolean = true) {
        currentFilePath = filePath
        try {
            initMediaPlayer()
            val player = mediaPlayer ?: return

            if (filePath.startsWith("content://") || filePath.startsWith("file://")) {
                player.setDataSource(context, Uri.parse(filePath))
            } else {
                val file = File(filePath)
                if (file.exists()) {
                    player.setDataSource(file.absolutePath)
                } else {
                    try {
                        player.setDataSource(context, Uri.parse(filePath))
                    } catch (e: Exception) {
                        return
                    }
                }
            }

            player.prepare()
            setupEqualizer(player.audioSessionId)
            player.setVolume(currentVolume, currentVolume)

            if (autoPlay) {
                player.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0.0f, 1.0f)
        try {
            mediaPlayer?.setVolume(currentVolume, currentVolume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getVolume(): Float = currentVolume

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentPosition(): Long {
        return try {
            mediaPlayer?.currentPosition?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun getDuration(): Long {
        return try {
            val dur = mediaPlayer?.duration?.toLong() ?: 0L
            if (dur > 0) dur else 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun setEqBandGain(bandIndex: Int, gainDb: Float) {
        try {
            val eq = equalizer ?: return
            val numBands = eq.numberOfBands.toInt()
            if (bandIndex in 0 until numBands) {
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
        try {
            equalizer?.release()
            equalizer = null
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
