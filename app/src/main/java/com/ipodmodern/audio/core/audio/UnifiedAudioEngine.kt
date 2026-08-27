package com.ipodmodern.audio.core.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.util.Log
import java.io.File

class UnifiedAudioEngine(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    val effectManager = AudioEffectManager()

    private var currentFilePath: String? = null
    private var currentVolume: Float = 1.0f
    private var isDucked: Boolean = false

    var onPlaybackCompleted: (() -> Unit)? = null
    var onPlaybackError: ((String) -> Unit)? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                isDucked = true
                applyVolumeInternal(currentVolume * 0.25f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (isDucked) {
                    isDucked = false
                    applyVolumeInternal(currentVolume)
                }
            }
        }
    }

    private var isNoisyReceiverRegistered = false
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pause()
            }
        }
    }

    init {
        initMediaPlayer()
        registerNoisyReceiver()
    }

    private fun registerNoisyReceiver() {
        try {
            if (!isNoisyReceiverRegistered) {
                val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                context.registerReceiver(noisyReceiver, filter)
                isNoisyReceiverRegistered = true
            }
        } catch (e: Exception) {
            Log.e("UnifiedAudioEngine", "Failed to register becoming noisy receiver", e)
        }
    }

    private fun requestAudioFocus(): Boolean {
        val am = audioManager ?: return true
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
                val focusReq = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
                audioFocusRequest = focusReq
                am.requestAudioFocus(focusReq) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } catch (e: Exception) {
            true
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(audioFocusChangeListener)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initMediaPlayer() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setWakeMode(context.applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                applyVolumeInternal(currentVolume)
                setOnCompletionListener {
                    onPlaybackCompleted?.invoke()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("UnifiedAudioEngine", "MediaPlayer error: what=$what, extra=$extra")
                    onPlaybackError?.invoke("Error $what ($extra)")
                    true // error handled
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
                        onPlaybackError?.invoke("File not found: $filePath")
                        return
                    }
                }
            }

            player.prepare()
            effectManager.initAudioEffects(player.audioSessionId)
            applyVolumeInternal(currentVolume)

            if (autoPlay) {
                play()
            }
        } catch (e: Exception) {
            Log.e("UnifiedAudioEngine", "Error loading file: $filePath", e)
            onPlaybackError?.invoke(e.message ?: "Failed to load audio")
        }
    }

    fun play() {
        try {
            if (requestAudioFocus()) {
                mediaPlayer?.let {
                    if (!it.isPlaying) {
                        it.start()
                    }
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
            abandonAudioFocus()
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

    private fun applyVolumeInternal(vol: Float) {
        val clamped = vol.coerceIn(0.0f, 1.0f)
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0.0f, 1.0f)
        if (!isDucked) {
            applyVolumeInternal(currentVolume)
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
        effectManager.setBandLevel(bandIndex.toShort(), gainDb)
    }

    fun getAudioSessionId(): Int {
        return try {
            mediaPlayer?.audioSessionId ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun release() {
        try {
            if (isNoisyReceiverRegistered) {
                context.unregisterReceiver(noisyReceiver)
                isNoisyReceiverRegistered = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        abandonAudioFocus()
        effectManager.release()

        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
