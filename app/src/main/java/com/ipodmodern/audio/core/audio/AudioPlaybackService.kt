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
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ipodmodern.audio.R
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.MainActivity
import java.io.File

class AudioPlaybackService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaSession: MediaSessionCompat? = null

    companion object {
        const val CHANNEL_ID = "aether_playback_channel_v3"
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
        const val EXTRA_POSITION_MS = "extra_position_ms"
        const val EXTRA_DURATION_MS = "extra_duration_ms"

        var playbackActionListener: ((String) -> Unit)? = null
        var seekActionListener: ((Long) -> Unit)? = null

        fun updateService(
            context: Context,
            track: Track?,
            isPlaying: Boolean,
            positionMs: Long = 0L,
            durationMs: Long = 0L
        ) {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_UPDATE_NOTIFICATION
                putExtra(EXTRA_TITLE, track?.title ?: "Aether Audio")
                putExtra(EXTRA_ARTIST, track?.artist ?: "Lossless Hi-Fi")
                putExtra(EXTRA_ALBUM, track?.album ?: "")
                putExtra(EXTRA_BADGE, track?.badgeText ?: "24-BIT • FLAC")
                putExtra(EXTRA_ARTWORK_URI, track?.artworkUri)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
                putExtra(EXTRA_POSITION_MS, positionMs)
                putExtra(EXTRA_DURATION_MS, if (durationMs > 0) durationMs else (track?.durationMs ?: 0L))
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

        // Initialize Android System MediaSessionCompat
        mediaSession = MediaSessionCompat(this, "AetherAudioSession").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    playbackActionListener?.invoke(ACTION_PLAY)
                }

                override fun onPause() {
                    playbackActionListener?.invoke(ACTION_PAUSE)
                }

                override fun onSkipToNext() {
                    playbackActionListener?.invoke(ACTION_NEXT)
                }

                override fun onSkipToPrevious() {
                    playbackActionListener?.invoke(ACTION_PREV)
                }

                override fun onSeekTo(pos: Long) {
                    seekActionListener?.invoke(pos)
                }

                override fun onStop() {
                    playbackActionListener?.invoke(ACTION_STOP)
                }
            })
            isActive = true
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            // Delete old low-importance channel if present
            try {
                manager.deleteNotificationChannel("aether_playback_channel")
            } catch (e: Exception) {
                // Ignore
            }

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aether Hi-Fi Audio Playback",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Audiophile lossless background audio playback & controls"
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun decodeArtworkBitmap(artworkUriStr: String?): Bitmap? {
        if (artworkUriStr.isNullOrEmpty()) return null
        return try {
            val file = File(artworkUriStr)
            if (file.exists()) {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(file.absolutePath, options)
                var inSampleSize = 1
                while (options.outWidth / inSampleSize > 512 || options.outHeight / inSampleSize > 512) {
                    inSampleSize *= 2
                }
                options.inJustDecodeBounds = false
                options.inSampleSize = inSampleSize
                BitmapFactory.decodeFile(file.absolutePath, options)
            } else {
                val uri = Uri.parse(artworkUriStr)
                contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun updateMediaSession(
        title: String,
        artist: String,
        album: String,
        badge: String,
        durationMs: Long,
        positionMs: Long,
        isPlaying: Boolean,
        artworkBitmap: Bitmap?
    ) {
        val session = mediaSession ?: return

        // 1. Update PlaybackStateCompat (Enables System Lockscreen / Quick Settings Scrubber & Buttons)
        val actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_STOP

        val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val playbackSpeed = if (isPlaying) 1.0f else 0.0f

        val playbackState = PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(state, positionMs, playbackSpeed)
            .build()
        session.setPlaybackState(playbackState)

        // 2. Update MediaMetadataCompat (Provides track title, artist, album, full artwork to Android OS)
        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "$artist • $badge")

        if (artworkBitmap != null) {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artworkBitmap)
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, artworkBitmap)
        }

        session.setMetadata(metadataBuilder.build())
    }

    private fun buildMediaNotification(
        title: String,
        artist: String,
        album: String,
        badge: String,
        durationMs: Long,
        positionMs: Long,
        artworkUriStr: String?,
        isPlaying: Boolean
    ): Notification {
        val artworkBitmap = decodeArtworkBitmap(artworkUriStr)

        // Sync with Android System MediaSession
        updateMediaSession(
            title = title,
            artist = artist,
            album = album,
            badge = badge,
            durationMs = durationMs,
            positionMs = positionMs,
            isPlaying = isPlaying,
            artworkBitmap = artworkBitmap
        )

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

        val stopIntent = Intent(this, AudioPlaybackService::class.java).apply { action = ACTION_STOP }
        val pendingStop = PendingIntent.getService(this, 4, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }

        // Android System MediaStyle
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
        mediaSession?.sessionToken?.let { token ->
            mediaStyle.setMediaSession(token)
        }
        mediaStyle.setShowActionsInCompactView(0, 1, 2)
        mediaStyle.setShowCancelButton(true)
        mediaStyle.setCancelButtonIntent(pendingStop)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(badge)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingOpenIntent)
            .setDeleteIntent(pendingStop)
            .setOngoing(isPlaying)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setStyle(mediaStyle)
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
                val album = intent.getStringExtra(EXTRA_ALBUM) ?: ""
                val badge = intent.getStringExtra(EXTRA_BADGE) ?: "24-BIT • FLAC"
                val artworkUri = intent.getStringExtra(EXTRA_ARTWORK_URI)
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, true)
                val positionMs = intent.getLongExtra(EXTRA_POSITION_MS, 0L)
                val durationMs = intent.getLongExtra(EXTRA_DURATION_MS, 0L)

                if (isPlaying) {
                    wakeLock?.acquire(30 * 60 * 1000L) // 30 minutes wake lock per song refresh
                } else {
                    wakeLock?.release()
                }

                val notification = buildMediaNotification(
                    title = title,
                    artist = artist,
                    album = album,
                    badge = badge,
                    durationMs = durationMs,
                    positionMs = positionMs,
                    artworkUriStr = artworkUri,
                    isPlaying = isPlaying
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
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
                mediaSession?.isActive = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
        mediaSession?.release()
        mediaSession = null
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
