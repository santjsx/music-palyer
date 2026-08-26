package com.ipodmodern.audio.core.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.MainActivity

class AudioPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val CHANNEL_ID = "ipod_playback_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_PLAY = "com.ipodmodern.audio.ACTION_PLAY"
        const val ACTION_PAUSE = "com.ipodmodern.audio.ACTION_PAUSE"
        const val ACTION_NEXT = "com.ipodmodern.audio.ACTION_NEXT"
        const val ACTION_PREV = "com.ipodmodern.audio.ACTION_PREV"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "iPodModern::AudioWakeLock").apply {
            setReferenceCounted(false)
        }

        NativeAudioBridge.initEngine(48000, true)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audiophile Playback Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Zero-latency audiophile playback notification"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(track: Track?, isPlaying: Boolean): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(track?.title ?: "iPod Modern")
            .setContentText("${track?.artist ?: "Audiophile Player"} • ${track?.badgeText ?: "LOSSLESS"}")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingOpenIntent)
            .setOngoing(isPlaying)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                wakeLock?.acquire(10 * 60 * 1000L)
                NativeAudioBridge.play()
            }
            ACTION_PAUSE -> {
                NativeAudioBridge.pause()
                wakeLock?.release()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
        NativeAudioBridge.stop()
        NativeAudioBridge.destroyEngine()
        mediaSession?.release()
        mediaSession = null
    }
}
