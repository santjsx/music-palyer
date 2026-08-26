package com.ipodmodern.audio.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ipodmodern.audio.core.model.Track

@Entity(
    tableName = "tracks",
    indices = [
        Index("artist"),
        Index("album"),
        Index("filePath", unique = true)
    ]
)
data class TrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val filePath: String,
    val trackNumber: Int,
    val year: Int,
    val genre: String,
    val artworkUri: String?,
    val formatName: String,
    val sampleRate: Int,
    val bitDepth: Int,
    val badgeText: String,
    val isCueSplit: Boolean = false,
    val cueStartMs: Long = 0L,
    val cueEndMs: Long = 0L
) {
    fun toDomain(): Track = Track(
        id = id,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        filePath = filePath,
        trackNumber = trackNumber,
        year = year,
        genre = genre,
        artworkUri = artworkUri,
        formatName = formatName,
        sampleRate = sampleRate,
        bitDepth = bitDepth,
        badgeText = badgeText,
        isCueSplit = isCueSplit,
        cueStartMs = cueStartMs,
        cueEndMs = cueEndMs
    )

    companion object {
        fun fromDomain(track: Track): TrackEntity = TrackEntity(
            id = track.id,
            title = track.title,
            artist = track.artist,
            album = track.album,
            durationMs = track.durationMs,
            filePath = track.filePath,
            trackNumber = track.trackNumber,
            year = track.year,
            genre = track.genre,
            artworkUri = track.artworkUri,
            formatName = track.formatName,
            sampleRate = track.sampleRate,
            bitDepth = track.bitDepth,
            badgeText = track.badgeText,
            isCueSplit = track.isCueSplit,
            cueStartMs = track.cueStartMs,
            cueEndMs = track.cueEndMs
        )
    }
}

@Entity(
    tableName = "albums",
    indices = [
        Index("title"),
        Index("artist")
    ]
)
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String,
    val trackCount: Int,
    val year: Int,
    val artworkUri: String?,
    val isHiRes: Boolean
)

@Entity(
    tableName = "artists",
    indices = [
        Index("name", unique = true)
    ]
)
data class ArtistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val albumCount: Int,
    val trackCount: Int
)
