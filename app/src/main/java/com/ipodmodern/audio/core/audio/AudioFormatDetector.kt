package com.ipodmodern.audio.core.audio

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import com.ipodmodern.audio.core.model.AudioQuality
import com.ipodmodern.audio.core.model.NativeAudioMetadata
import java.io.File

object AudioFormatDetector {

    fun detect(context: Context, uri: Uri, fallbackPath: String? = null): NativeAudioMetadata {
        // 1. Try NativeTagInspector via Native C++ engine first
        if (!fallbackPath.isNullOrEmpty()) {
            val file = File(fallbackPath)
            if (file.exists() && file.canRead()) {
                val meta = NativeAudioBridge.inspectFileMetadata(file.absolutePath)
                if (meta != null && meta.sampleRate > 0) {
                    return meta
                }
            }
        }

        // 2. Android MediaExtractor inspection fallback
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    val sampleRate = if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) format.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 44100
                    val channels = if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 2
                    val bitRate = if (format.containsKey(MediaFormat.KEY_BIT_RATE)) format.getInteger(MediaFormat.KEY_BIT_RATE) / 1000 else 0
                    val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) else 0L

                    val formatName = when {
                        mime.contains("flac") -> "FLAC"
                        mime.contains("wav") || mime.contains("raw") -> "WAV"
                        mime.contains("alac") -> "ALAC"
                        mime.contains("mp4") || mime.contains("aac") -> "AAC"
                        mime.contains("mpeg") -> "MP3"
                        mime.contains("ogg") || mime.contains("opus") -> "OPUS"
                        else -> "PCM"
                    }

                    val isLossy = formatName == "MP3" || formatName == "AAC" || formatName == "OPUS"
                    val isHiRes = sampleRate > 48000 || (!isLossy && sampleRate >= 48000)

                    val quality = when {
                        isLossy -> AudioQuality.LOSSY
                        isHiRes -> AudioQuality.HI_RES_LOSSLESS
                        else -> AudioQuality.LOSSLESS
                    }

                    val badgeText = when (quality) {
                        AudioQuality.LOSSY -> "LOSSY $formatName ${if (bitRate > 0) "${bitRate}k" else ""}".trim()
                        AudioQuality.HI_RES_LOSSLESS -> "HI-RES ${(sampleRate / 1000.0f)}kHz"
                        AudioQuality.LOSSLESS -> "LOSSLESS ${(sampleRate / 1000.0f)}kHz"
                    }

                    return NativeAudioMetadata(
                        formatName = formatName,
                        sampleRate = sampleRate,
                        bitDepth = if (isHiRes) 24 else 16,
                        channels = channels,
                        bitRateKbps = bitRate,
                        durationMs = durationUs / 1000L,
                        badgeText = badgeText,
                        qualityCategoryInt = quality.ordinal
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { extractor.release() } catch (_: Exception) {}
        }

        // Default fallback
        return NativeAudioMetadata(
            formatName = "FLAC",
            sampleRate = 44100,
            bitDepth = 16,
            channels = 2,
            bitRateKbps = 1411,
            durationMs = 0L,
            badgeText = "LOSSLESS 16-BIT / 44.1kHz",
            qualityCategoryInt = AudioQuality.LOSSLESS.ordinal
        )
    }
}
