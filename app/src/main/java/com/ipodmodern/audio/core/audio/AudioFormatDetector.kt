package com.ipodmodern.audio.core.audio

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.ipodmodern.audio.core.model.AudioQuality
import com.ipodmodern.audio.core.model.NativeAudioMetadata
import java.io.File

object AudioFormatDetector {

    fun detect(context: Context, uri: Uri?, fallbackPath: String? = null): NativeAudioMetadata {
        // 1. Try Native C++ TagInspector for FLAC, WAV, and DSD files
        if (!fallbackPath.isNullOrEmpty()) {
            val file = File(fallbackPath)
            if (file.exists() && file.canRead()) {
                val ext = file.extension.lowercase()
                if (ext == "flac" || ext == "wav" || ext == "dsd" || ext == "dsf" || ext == "dff") {
                    val meta = NativeAudioBridge.inspectFileMetadata(file.absolutePath)
                    if (meta != null && meta.sampleRate > 0 && meta.formatName != "UNKNOWN") {
                        return meta
                    }
                }
            }
        }

        // 2. Android MediaMetadataRetriever (fast & native extraction of stream headers)
        val mmr = MediaMetadataRetriever()
        try {
            if (!fallbackPath.isNullOrBlank() && File(fallbackPath).exists()) {
                mmr.setDataSource(fallbackPath)
            } else if (uri != null) {
                mmr.setDataSource(context, uri)
            }

            val mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: ""
            val sampleRate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull() ?: 44100
            val bitRateBps = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0
            val bitRateKbps = if (bitRateBps > 0) bitRateBps / 1000 else 0
            val durationMs = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L

            val ext = fallbackPath?.substringAfterLast('.', "")?.lowercase() ?: ""
            val formatName = when {
                ext == "flac" || mime.contains("flac", true) -> "FLAC"
                ext == "wav" || mime.contains("wav", true) || mime.contains("raw", true) -> "WAV"
                ext == "alac" || mime.contains("alac", true) -> "ALAC"
                ext == "dsf" || ext == "dff" || ext == "dsd" -> "DSD"
                ext == "m4a" -> if (bitRateKbps > 600 || mime.contains("alac", true)) "ALAC" else "AAC"
                ext == "aac" || mime.contains("aac", true) -> "AAC"
                ext == "mp3" || mime.contains("mpeg", true) -> "MP3"
                ext == "ogg" || mime.contains("ogg", true) -> "OGG"
                ext == "opus" || mime.contains("opus", true) -> "OPUS"
                else -> ext.uppercase().ifBlank { "AUDIO" }
            }

            val isLossy = formatName == "MP3" || formatName == "AAC" || formatName == "OGG" || formatName == "OPUS" || formatName == "WMA"
            val isLossless = !isLossy && (formatName == "FLAC" || formatName == "WAV" || formatName == "ALAC" || formatName == "DSD" || formatName == "APE")

            // Hi-Res requires a lossless format AND sample rate > 48000 Hz, or bit depth > 16 at >= 48000 Hz, or DSD
            val isHiRes = isLossless && (sampleRate > 48000 || formatName == "DSD")

            val bitDepth = when {
                isHiRes -> if (formatName == "DSD") 1 else 24
                isLossless -> 16
                else -> 16
            }

            val quality = when {
                isLossy -> AudioQuality.LOSSY
                isHiRes -> AudioQuality.HI_RES_LOSSLESS
                else -> AudioQuality.LOSSLESS
            }

            val rateFormatted = if (sampleRate % 1000 == 0) "${sampleRate / 1000}kHz" else String.format(java.util.Locale.US, "%.1fkHz", sampleRate / 1000.0f)
            val badgeText = when {
                isHiRes && formatName == "DSD" -> "DSD HI-RES"
                isHiRes -> "HI-RES $bitDepth-BIT / $rateFormatted"
                isLossless -> "LOSSLESS $bitDepth-BIT / $rateFormatted"
                bitRateKbps > 0 -> "$formatName ${bitRateKbps}k"
                else -> formatName
            }

            return NativeAudioMetadata(
                formatName = formatName,
                sampleRate = sampleRate,
                bitDepth = bitDepth,
                channels = 2,
                bitRateKbps = bitRateKbps,
                durationMs = durationMs,
                badgeText = badgeText,
                qualityCategoryInt = quality.ordinal
            )
        } catch (_: Exception) {
        } finally {
            try { mmr.release() } catch (_: Exception) {}
        }

        // 3. Fallback to MediaExtractor if MMR failed
        val extractor = MediaExtractor()
        try {
            if (uri != null) {
                extractor.setDataSource(context, uri, null)
            } else if (!fallbackPath.isNullOrBlank()) {
                extractor.setDataSource(fallbackPath)
            }
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    val sampleRate = if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) format.getInteger(MediaFormat.KEY_SAMPLE_RATE) else 44100
                    val channels = if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 2
                    val bitRate = if (format.containsKey(MediaFormat.KEY_BIT_RATE)) format.getInteger(MediaFormat.KEY_BIT_RATE) / 1000 else 0
                    val durationUs = if (format.containsKey(MediaFormat.KEY_DURATION)) format.getLong(MediaFormat.KEY_DURATION) else 0L

                    val ext = fallbackPath?.substringAfterLast('.', "")?.lowercase() ?: ""
                    val formatName = when {
                        ext == "flac" || mime.contains("flac", true) -> "FLAC"
                        ext == "wav" || mime.contains("wav", true) || mime.contains("raw", true) -> "WAV"
                        ext == "alac" || mime.contains("alac", true) -> "ALAC"
                        ext == "mp4" || ext == "aac" || mime.contains("aac", true) -> "AAC"
                        ext == "mp3" || mime.contains("mpeg", true) -> "MP3"
                        ext == "ogg" || mime.contains("ogg", true) -> "OGG"
                        ext == "opus" || mime.contains("opus", true) -> "OPUS"
                        else -> ext.uppercase().ifBlank { "AUDIO" }
                    }

                    val isLossy = formatName == "MP3" || formatName == "AAC" || formatName == "OGG" || formatName == "OPUS"
                    val isLossless = !isLossy && (formatName == "FLAC" || formatName == "WAV" || formatName == "ALAC")
                    val isHiRes = isLossless && sampleRate > 48000

                    val quality = when {
                        isLossy -> AudioQuality.LOSSY
                        isHiRes -> AudioQuality.HI_RES_LOSSLESS
                        else -> AudioQuality.LOSSLESS
                    }

                    val rateFormatted = if (sampleRate % 1000 == 0) "${sampleRate / 1000}kHz" else String.format(java.util.Locale.US, "%.1fkHz", sampleRate / 1000.0f)
                    val badgeText = when {
                        isHiRes -> "HI-RES 24-BIT / $rateFormatted"
                        isLossless -> "LOSSLESS 16-BIT / $rateFormatted"
                        bitRate > 0 -> "$formatName ${bitRate}k"
                        else -> formatName
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
        } catch (_: Exception) {
        } finally {
            try { extractor.release() } catch (_: Exception) {}
        }

        val ext = fallbackPath?.substringAfterLast('.', "")?.uppercase() ?: "AUDIO"
        val isExtLossless = ext == "FLAC" || ext == "WAV" || ext == "ALAC" || ext == "DSD"
        return NativeAudioMetadata(
            formatName = ext,
            sampleRate = 44100,
            bitDepth = 16,
            channels = 2,
            bitRateKbps = if (isExtLossless) 1411 else 320,
            durationMs = 0L,
            badgeText = if (isExtLossless) "LOSSLESS 16-BIT / 44.1kHz" else ext,
            qualityCategoryInt = if (isExtLossless) AudioQuality.LOSSLESS.ordinal else AudioQuality.LOSSY.ordinal
        )
    }
}
