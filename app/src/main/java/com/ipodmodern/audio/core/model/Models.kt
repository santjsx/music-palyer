package com.ipodmodern.audio.core.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class NativeAudioMetadata(
    val formatName: String,
    val sampleRate: Int,
    val bitDepth: Int,
    val channels: Int,
    val bitRateKbps: Int,
    val durationMs: Long,
    val badgeText: String,
    val qualityCategoryInt: Int
) {
    val qualityCategory: AudioQuality
        get() = when (qualityCategoryInt) {
            0 -> AudioQuality.LOSSY
            1 -> AudioQuality.LOSSLESS
            2 -> AudioQuality.HI_RES_LOSSLESS
            else -> AudioQuality.LOSSLESS
        }
}

enum class AudioQuality {
    LOSSY,
    LOSSLESS,
    HI_RES_LOSSLESS
}

@Serializable
data class Track(
    val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val filePath: String,
    val trackNumber: Int = 1,
    val year: Int = 0,
    val genre: String = "Audiophile",
    val artworkUri: String? = null,
    val formatName: String = "FLAC",
    val sampleRate: Int = 44100,
    val bitDepth: Int = 16,
    val badgeText: String = "LOSSLESS 16-BIT / 44.1kHz",
    val isCueSplit: Boolean = false,
    val cueStartMs: Long = 0L,
    val cueEndMs: Long = 0L
) {
    val isHiRes: Boolean
        get() = badgeText.contains("HI-RES", ignoreCase = true) || 
                badgeText.contains("24-BIT", ignoreCase = true) || 
                badgeText.contains("96.0", ignoreCase = true) || 
                sampleRate > 48000 || bitDepth > 16

    val isLossless: Boolean
        get() = isHiRes || 
                badgeText.contains("LOSSLESS", ignoreCase = true) ||
                badgeText.contains("FLAC", ignoreCase = true) ||
                badgeText.contains("ALAC", ignoreCase = true) ||
                badgeText.contains("WAV", ignoreCase = true) ||
                badgeText.contains("DSD", ignoreCase = true) ||
                formatName.equals("FLAC", true) ||
                formatName.equals("WAV", true) ||
                formatName.equals("ALAC", true)

    val displayBadge: String
        get() = when {
            isHiRes -> "HI-RES LOSSLESS"
            isLossless -> "LOSSLESS"
            badgeText.contains("320", ignoreCase = true) -> "320 KBPS"
            formatName.isNotBlank() -> formatName.uppercase()
            else -> "HIGH QUALITY"
        }

    val audioSpecText: String
        get() = when {
            isHiRes -> "${bitDepth}-Bit / ${(sampleRate / 1000.0f).toString().removeSuffix(".0")} kHz • ${formatName.uppercase()}"
            isLossless -> "${bitDepth}-Bit / 44.1 kHz • ${formatName.uppercase()}"
            else -> "${formatName.uppercase()} • High Quality"
        }
}

@Serializable
data class Album(
    val id: Long = 0,
    val title: String,
    val artist: String,
    val trackCount: Int,
    val year: Int = 0,
    val artworkUri: String? = null,
    val isHiRes: Boolean = false
)

@Serializable
data class Artist(
    val id: Long = 0,
    val name: String,
    val albumCount: Int,
    val trackCount: Int
)

@Serializable
data class Playlist(
    val id: Long = 0,
    val name: String,
    val trackIds: List<Long> = emptyList()
)

data class CueTrack(
    val trackNumber: Int,
    val title: String,
    val performer: String,
    val startMs: Long,
    val endMs: Long? = null
)

data class CueSheet(
    val title: String,
    val performer: String,
    val audioFileName: String,
    val tracks: List<CueTrack>
)

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class EqualizerPreset(
    val name: String,
    val bandGains: FloatArray // 10 bands: 31.25Hz to 16kHz
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EqualizerPreset
        return name == other.name && bandGains.contentEquals(other.bandGains)
    }

    override fun hashCode(): Int {
        return 31 * name.hashCode() + bandGains.contentHashCode()
    }
}
