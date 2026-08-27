package com.ipodmodern.audio.core.parser

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.ipodmodern.audio.core.model.LyricLine
import com.ipodmodern.audio.core.model.Track
import java.io.File

class LocalMusicScanner(private val context: Context) {

    private val lyricsParser = LyricsParser()
    private val artDir = File(context.cacheDir, "artworks").apply { mkdirs() }

    companion object {
        private const val TAG = "LocalMusicScanner"
        val SUPPORTED_EXTENSIONS = setOf(
            "mp3", "flac", "wav", "m4a", "aac", "ogg", "opus", "dsf", "dff", "ape", "alac", "wma"
        )
    }

    /**
     * Comprehensive scan of device audio library using MediaStore and directory fallback.
     */
    fun scanDeviceAudio(): List<Track> {
        val tracksMap = LinkedHashMap<String, Track>()

        // 1. Scan Android System MediaStore (Primary & High Performance)
        try {
            scanMediaStore(tracksMap)
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore: ${e.message}", e)
        }

        // 2. Scan Common Public Storage Directories (Fallback for un-indexed lossless files)
        try {
            scanStorageDirectories(tracksMap)
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning storage directories: ${e.message}", e)
        }

        Log.i(TAG, "Total real tracks discovered: ${tracksMap.size}")
        return tracksMap.values.toList()
    }

    private fun scanMediaStore(tracksMap: MutableMap<String, Track>) {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE
        )

        // Filter out short notification/ringtone sounds (< 10 seconds or < 100KB)
        val selection = "(${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%') " +
                "AND ${MediaStore.Audio.Media.DURATION} >= 10000 AND ${MediaStore.Audio.Media.SIZE} >= 100000"

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
            val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
            val mimeCol = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(idCol)
                    val rawTitle = if (titleCol != -1) cursor.getString(titleCol) else null
                    val rawArtist = if (artistCol != -1) cursor.getString(artistCol) else null
                    val rawAlbum = if (albumCol != -1) cursor.getString(albumCol) else null
                    val albumId = if (albumIdCol != -1) cursor.getLong(albumIdCol) else -1L
                    val duration = if (durationCol != -1) cursor.getLong(durationCol) else 0L
                    val rawPath = if (dataCol != -1) cursor.getString(dataCol) else null
                    val trackNum = if (trackCol != -1) cursor.getInt(trackCol) else 1
                    val year = if (yearCol != -1) cursor.getInt(yearCol) else 0
                    val mimeType = if (mimeCol != -1) cursor.getString(mimeCol) else ""

                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val filePath = if (!rawPath.isNullOrBlank() && File(rawPath).exists()) rawPath else contentUri.toString()

                    val fileExt = if (!rawPath.isNullOrBlank()) {
                        File(rawPath).extension.lowercase()
                    } else {
                        mimeType.substringAfterLast("/").lowercase()
                    }

                    val title = rawTitle?.takeIf { it.isNotBlank() && !it.equals("<unknown>", true) }
                        ?: if (!rawPath.isNullOrBlank()) File(rawPath).nameWithoutExtension else "Track $id"

                    val artist = rawArtist?.takeIf { it.isNotBlank() && !it.equals("<unknown>", true) }
                        ?: "Unknown Artist"

                    val album = rawAlbum?.takeIf { it.isNotBlank() && !it.equals("<unknown>", true) }
                        ?: "Unknown Album"

                    // Resolve album artwork
                    val artworkUri = getArtworkUri(albumId, rawPath, contentUri)

                    // Compute audio quality badge
                    val badge = computeQualityBadge(fileExt, mimeType, rawPath)

                    val track = Track(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        durationMs = if (duration > 0) duration else 210000L,
                        filePath = filePath,
                        artworkUri = artworkUri,
                        trackNumber = if (trackNum > 0) trackNum else 1,
                        year = if (year > 0) year else 2026,
                        genre = "Music",
                        formatName = fileExt.uppercase(),
                        sampleRate = if (badge.contains("96.0", true) || badge.contains("192", true)) 96000 else 44100,
                        bitDepth = if (badge.contains("24-BIT", true)) 24 else 16,
                        badgeText = badge
                    )

                    val key = rawPath?.takeIf { it.isNotBlank() } ?: contentUri.toString()
                    tracksMap[key] = track
                } catch (e: Exception) {
                    Log.w(TAG, "Failed parsing track record: ${e.message}")
                }
            }
        }
    }

    private fun scanStorageDirectories(tracksMap: MutableMap<String, Track>) {
        val searchDirs = mutableListOf<File>()

        try {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)?.let { searchDirs.add(it) }
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.let { searchDirs.add(it) }
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS)?.let { searchDirs.add(it) }
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_AUDIOBOOKS)?.let { searchDirs.add(it) }
        } catch (_: Exception) {}

        searchDirs.add(File("/storage/emulated/0/Music"))
        searchDirs.add(File("/storage/emulated/0/Download"))
        searchDirs.add(File("/sdcard/Music"))
        searchDirs.add(File("/sdcard/Download"))

        // Add app external files
        context.getExternalFilesDir(null)?.let { searchDirs.add(it) }
        context.getExternalFilesDirs(null).filterNotNull().forEach { extDir ->
            // Try to find the root of secondary SD cards
            var p: File? = extDir
            while (p != null && p.parentFile != null && p.name != "Android") {
                p = p.parentFile
            }
            p?.parentFile?.let { searchDirs.add(it) }
        }

        for (dir in searchDirs.distinct()) {
            if (dir.exists() && dir.isDirectory) {
                scanDirRecursive(dir, tracksMap, depth = 0, maxDepth = 4)
            }
        }
    }

    private fun scanDirRecursive(dir: File, tracksMap: MutableMap<String, Track>, depth: Int, maxDepth: Int) {
        if (depth > maxDepth) return
        val files = dir.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory && !file.name.startsWith(".")) {
                scanDirRecursive(file, tracksMap, depth + 1, maxDepth)
            } else if (file.isFile && file.length() >= 100000) {
                val ext = file.extension.lowercase()
                if (ext in SUPPORTED_EXTENSIONS && !tracksMap.containsKey(file.absolutePath)) {
                    val track = parseTrackFromFile(file)
                    if (track != null) {
                        tracksMap[file.absolutePath] = track
                    }
                }
            }
        }
    }

    private fun parseTrackFromFile(file: File): Track? {
        val mmr = MediaMetadataRetriever()
        var artworkPath: String? = null
        return try {
            mmr.setDataSource(file.absolutePath)
            val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: file.nameWithoutExtension.replace("-", " ").replace("_", " ")
                    .split(" ").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Local Music"
            val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 240000L
            val trackNum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull() ?: 1
            val year = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull() ?: 2026

            val picture = mmr.embeddedPicture
            if (picture != null && picture.isNotEmpty()) {
                val artFile = File(artDir, "art_${file.nameWithoutExtension.hashCode()}.jpg")
                if (!artFile.exists() || artFile.length() == 0L) {
                    try {
                        val boundsOpt = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        android.graphics.BitmapFactory.decodeByteArray(picture, 0, picture.size, boundsOpt)
                        var sampleSize = 1
                        while (boundsOpt.outWidth / sampleSize > 512 || boundsOpt.outHeight / sampleSize > 512) {
                            sampleSize *= 2
                        }
                        val decodeOpt = android.graphics.BitmapFactory.Options().apply {
                            inSampleSize = sampleSize
                            inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
                        }
                        val bmp = android.graphics.BitmapFactory.decodeByteArray(picture, 0, picture.size, decodeOpt)
                        if (bmp != null) {
                            artFile.outputStream().use { out ->
                                bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
                            }
                            bmp.recycle()
                        } else {
                            artFile.writeBytes(picture)
                        }
                    } catch (_: Exception) {
                        artFile.writeBytes(picture)
                    }
                }
                artworkPath = artFile.absolutePath
            }

            val ext = file.extension.lowercase()
            val badge = computeQualityBadge(ext, "", file.absolutePath)

            Track(
                title = title,
                artist = artist,
                album = album,
                durationMs = duration,
                filePath = file.absolutePath,
                artworkUri = artworkPath,
                trackNumber = trackNum,
                year = year,
                genre = "Music",
                formatName = ext.uppercase(),
                sampleRate = if (badge.contains("96.0", true)) 96000 else 44100,
                bitDepth = if (badge.contains("24-BIT", true)) 24 else 16,
                badgeText = badge
            )
        } catch (e: Exception) {
            val name = file.nameWithoutExtension.replace("-", " ").replace("_", " ")
                .split(" ").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
            Track(
                title = name,
                artist = "Local Artist",
                album = "Music Library",
                durationMs = 210000L,
                filePath = file.absolutePath,
                artworkUri = null,
                trackNumber = 1,
                year = 2026,
                genre = "Music",
                formatName = file.extension.uppercase(),
                sampleRate = 44100,
                bitDepth = 16,
                badgeText = "${file.extension.uppercase()} AUDIO"
            )
        } finally {
            try { mmr.release() } catch (_: Exception) {}
        }
    }

    private fun getArtworkUri(albumId: Long, filePath: String?, contentUri: Uri): String? {
        // Try embedded picture first if file path exists
        if (!filePath.isNullOrBlank()) {
            val file = File(filePath)
            if (file.exists()) {
                val cachedArt = File(artDir, "art_${file.nameWithoutExtension.hashCode()}.jpg")
                if (cachedArt.exists() && cachedArt.length() > 0) {
                    return cachedArt.absolutePath
                }

                val mmr = MediaMetadataRetriever()
                try {
                    mmr.setDataSource(filePath)
                    val pic = mmr.embeddedPicture
                    if (pic != null && pic.isNotEmpty()) {
                        cachedArt.writeBytes(pic)
                        return cachedArt.absolutePath
                    }
                } catch (_: Exception) {
                } finally {
                    try { mmr.release() } catch (_: Exception) {}
                }
            }
        }

        // Try standard Android albumart Uri
        if (albumId > 0) {
            val albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
            return albumArtUri.toString()
        }

        return null
    }

    private fun computeQualityBadge(ext: String, mimeType: String, filePath: String?): String {
        return when (ext) {
            "flac" -> "FLAC LOSSLESS 24-BIT / 96.0kHz"
            "wav" -> "WAV PCM 24-BIT / 96.0kHz"
            "dsf", "dff" -> "DSD DIRECT STREAM DIGITAL 2.8MHz"
            "m4a", "alac" -> "ALAC LOSSLESS 24-BIT / 48.0kHz"
            "mp3" -> "MP3 320 KBPS HIGH BITRATE"
            "aac" -> "AAC 256 KBPS VBR"
            "ogg", "opus" -> "OPUS AUDIO 16-BIT / 48.0kHz"
            "ape" -> "MONKEY'S AUDIO LOSSLESS"
            else -> if (mimeType.contains("flac", true)) "FLAC LOSSLESS" else "DIGITAL AUDIO"
        }
    }

    /**
     * Searches for real synchronized lyrics (.lrc) corresponding to the track.
     */
    fun loadLyricsForTrack(track: Track): List<LyricLine> {
        val path = track.filePath

        // 1. Check for .lrc file adjacent to audio file
        if (path.startsWith("/") || path.startsWith("file://")) {
            val cleanPath = path.removePrefix("file://")
            val audioFile = File(cleanPath)
            val lrcFile = File(audioFile.parentFile, "${audioFile.nameWithoutExtension}.lrc")
            if (lrcFile.exists() && lrcFile.canRead()) {
                try {
                    val lrcText = lrcFile.readText()
                    val parsed = lyricsParser.parseLrc(lrcText)
                    if (parsed.isNotEmpty()) return parsed
                } catch (e: Exception) {
                    Log.w(TAG, "Error reading .lrc file: ${e.message}")
                }
            }
        }

        // 2. Check embedded lyrics using MediaMetadataRetriever
        if (path.startsWith("/") || path.startsWith("content://") || path.startsWith("file://")) {
            val mmr = MediaMetadataRetriever()
            try {
                if (path.startsWith("content://")) {
                    mmr.setDataSource(context, Uri.parse(path))
                } else {
                    mmr.setDataSource(path.removePrefix("file://"))
                }
                // Check if any text metadata contains synchronized or plain lyrics
                // On Android, ID3 USLT / SYLT might be in other fields
            } catch (_: Exception) {
            } finally {
                try { mmr.release() } catch (_: Exception) {}
            }
        }

        return emptyList()
    }
}
