package com.ipodmodern.audio.core.model

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Keep
@Immutable
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

enum class AudioQualityType {
    LOSSY,
    LOSSLESS,
    HI_RES
}

@Keep
@Immutable
@Serializable
data class AudioTrackSpecs(
    val qualityType: AudioQualityType = AudioQualityType.LOSSY,
    val sampleRateKhz: Float = 0f,
    val bitDepth: Int = 0,
    val codec: String = ""
) {
    val isHiRes: Boolean get() = qualityType == AudioQualityType.HI_RES
    val isLossless: Boolean get() = qualityType == AudioQualityType.LOSSLESS || qualityType == AudioQualityType.HI_RES
    val badgeText: String get() = when (qualityType) {
        AudioQualityType.HI_RES -> "HI-RES LOSSLESS"
        AudioQualityType.LOSSLESS -> "LOSSLESS"
        AudioQualityType.LOSSY -> codec.uppercase()
    }
    val specDetailsText: String get() {
        val rateStr = if (sampleRateKhz > 0f) {
            if (sampleRateKhz == sampleRateKhz.toInt().toFloat()) "${sampleRateKhz.toInt()} kHz" else String.format(java.util.Locale.US, "%.1f kHz", sampleRateKhz)
        } else "44.1 kHz"
        return when {
            codec.equals("DSD", true) -> "DSD • 2.8 MHz"
            qualityType == AudioQualityType.HI_RES -> "${bitDepth}-Bit / $rateStr • ${codec.uppercase()}"
            qualityType == AudioQualityType.LOSSLESS -> "${bitDepth}-Bit / $rateStr • ${codec.uppercase()}"
            else -> "${codec.uppercase()} • Compressed Audio"
        }
    }
}

@Immutable
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
        get() {
            if (isLossyFormat(formatName)) return false
            return sampleRate > 48000 || (bitDepth > 16 && sampleRate >= 48000) || formatName.equals("DSD", true)
        }

    val isLossless: Boolean
        get() {
            if (isLossyFormat(formatName)) return false
            return isLosslessFormat(formatName)
        }

    val displayBadge: String
        get() = when {
            isHiRes -> "HI-RES LOSSLESS"
            isLossless -> "LOSSLESS"
            else -> formatName.uppercase()
        }

    val audioSpecText: String
        get() {
            val rateText = if (sampleRate > 0) {
                val khz = sampleRate / 1000.0f
                if (khz == khz.toInt().toFloat()) "${khz.toInt()} kHz" else String.format(java.util.Locale.US, "%.1f kHz", khz)
            } else "44.1 kHz"

            return when {
                isHiRes && formatName.equals("DSD", true) -> "DSD • 2.8 MHz"
                isHiRes -> "${bitDepth}-Bit / $rateText • ${formatName.uppercase()}"
                isLossless -> "${bitDepth}-Bit / $rateText • ${formatName.uppercase()}"
                else -> "${formatName.uppercase()} • Compressed Audio"
            }
        }
}

fun isLossyFormat(format: String): Boolean {
    val f = format.lowercase().trim()
    return f == "mp3" || f == "aac" || f == "m4a" || f == "ogg" || f == "opus" || f == "wma"
}

fun isLosslessFormat(format: String): Boolean {
    val f = format.lowercase().trim()
    return f == "flac" || f == "wav" || f == "alac" || f == "aiff" || f == "ape" || f == "dsd" || f == "dsf" || f == "dff"
}

@Immutable
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

@Immutable
@Serializable
data class Artist(
    val id: Long = 0,
    val name: String,
    val albumCount: Int,
    val trackCount: Int
)

@Immutable
@Serializable
data class Playlist(
    val id: Long = 0,
    val name: String,
    val trackIds: List<Long> = emptyList()
)

@Immutable
data class CueTrack(
    val trackNumber: Int,
    val title: String,
    val performer: String,
    val startMs: Long,
    val endMs: Long? = null
)

@Immutable
data class CueSheet(
    val title: String,
    val performer: String,
    val audioFileName: String,
    val tracks: List<CueTrack>
)

@Immutable
data class LyricLine(
    val timeMs: Long,
    val text: String
)

@Immutable
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
