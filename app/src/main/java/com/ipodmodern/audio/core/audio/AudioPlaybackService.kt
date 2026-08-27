package com.ipodmodern.audio.core.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ipodmodern.audio.R
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.MainActivity

class AudioPlaybackService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val CHANNEL_ID = "aether_playback_channel"
        const val NOTIFICATION_ID = 2002

        const val ACTION_PLAY = "com.ipodmodern.audio.ACTION_PLAY"
        const val ACTION_PAUSE = "com.ipodmodern.audio.ACTION_PAUSE"
        const val ACTION_TOGGLE_PLAY = "com.ipodmodern.audio.ACTION_TOGGLE_PLAY"
        const val ACTION_NEXT = "com.ipodmodern.audio.ACTION_NEXT"
        const val ACTION_PREV = "com.ipodmodern.audio.ACTION_PREV"
        const val ACTION_STOP = "com.ipodmodern.audio.ACTION_STOP"
        const val ACTION_UPDATE_NOTIFICATION = "com.ipodmodern.audio.ACTION_UPDATE_NOTIFICATION"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_ALBUM = "extra_album"
        const val EXTRA_BADGE = "extra_badge"
        const val EXTRA_ARTWORK_URI = "extra_artwork_uri"
        const val EXTRA_IS_PLAYING = "extra_is_playing"

        var playbackActionListener: ((String) -> Unit)? = null

        fun updateService(
            context: Context,
            track: Track?,
            isPlaying: Boolean
        ) {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_UPDATE_NOTIFICATION
                putExtra(EXTRA_TITLE, track?.title ?: "Aether Audio")
                putExtra(EXTRA_ARTIST, track?.artist ?: "Lossless Hi-Fi")
                putExtra(EXTRA_ALBUM, track?.album ?: "")
                putExtra(EXTRA_BADGE, track?.badgeText ?: "24-BIT • FLAC")
                putExtra(EXTRA_ARTWORK_URI, track?.artworkUri?.toString())
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AetherAudio::BackgroundWakeLock").apply {
            setReferenceCounted(false)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aether Hi-Fi Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Uninterrupted background lossless playback"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildMediaNotification(
        title: String,
        artist: String,
        badge: String,
        artworkUriStr: String?,
        isPlaying: Boolean
    ): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevIntent = Intent(this, AudioPlaybackService::class.java).apply { action = ACTION_PREV }
        val pendingPrev = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val toggleIntent = Intent(this, AudioPlaybackService::class.java).apply { action = ACTION_TOGGLE_PLAY }
        val pendingToggle = PendingIntent.getService(this, 2, toggleIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(this, AudioPlaybackService::class.java).apply { action = ACTION_NEXT }
        val pendingNext = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        var artworkBitmap: Bitmap? = null
        if (!artworkUriStr.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(artworkUriStr)
                contentResolver.openInputStream(uri)?.use { stream ->
                    artworkBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                // Ignore fallback to null
            }
        }

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("$artist • $badge")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingOpenIntent)
            .setOngoing(isPlaying)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_previous, "Previous", pendingPrev)
            .addAction(playPauseIcon, if (isPlaying) "Pause" else "Play", pendingToggle)
            .addAction(android.R.drawable.ic_media_next, "Next", pendingNext)

        if (artworkBitmap != null) {
            builder.setLargeIcon(artworkBitmap)
        }

        return builder.build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE_NOTIFICATION -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Aether Audio"
                val artist = intent.getStringExtra(EXTRA_ARTIST) ?: "Lossless Hi-Fi"
                val badge = intent.getStringExtra(EXTRA_BADGE) ?: "24-BIT • FLAC"
                val artworkUri = intent.getStringExtra(EXTRA_ARTWORK_URI)
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, true)

                if (isPlaying) {
                    wakeLock?.acquire(30 * 60 * 1000L) // 30 minutes wake lock per song refresh
                } else {
                    wakeLock?.release()
                }

                val notification = buildMediaNotification(title, artist, badge, artworkUri, isPlaying)
                startForeground(NOTIFICATION_ID, notification)
            }
            ACTION_PLAY -> {
                playbackActionListener?.invoke(ACTION_PLAY)
            }
            ACTION_PAUSE -> {
                playbackActionListener?.invoke(ACTION_PAUSE)
            }
            ACTION_TOGGLE_PLAY -> {
                playbackActionListener?.invoke(ACTION_TOGGLE_PLAY)
            }
            ACTION_NEXT -> {
                playbackActionListener?.invoke(ACTION_NEXT)
            }
            ACTION_PREV -> {
                playbackActionListener?.invoke(ACTION_PREV)
            }
            ACTION_STOP -> {
                wakeLock?.release()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
