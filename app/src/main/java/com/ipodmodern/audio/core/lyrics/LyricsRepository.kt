package com.ipodmodern.audio.core.lyrics

import android.content.Context
import android.util.Log
import com.ipodmodern.audio.core.model.LyricLine
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.core.parser.LyricsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * High-performance lyrics repository supporting:
 * 1. Local adjacent .lrc files
 * 2. On-disk LRCLIB cached .lrc files
 * 3. High-accuracy LRCLIB public API with auto-cleaning of song titles and artist names
 */
class LyricsRepository(private val context: Context) {

    private val lyricsParser = LyricsParser()
    private val lyricsCacheDir = File(context.cacheDir, "lyrics").apply { mkdirs() }

    companion object {
        private const val TAG = "LyricsRepository"
        private const val LRCLIB_GET_URL = "https://lrclib.net/api/get"
        private const val LRCLIB_SEARCH_URL = "https://lrclib.net/api/search"

        @Volatile
        private var instance: LyricsRepository? = null

        fun getInstance(context: Context): LyricsRepository {
            return instance ?: synchronized(this) {
                instance ?: LyricsRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Fast local-only fetch (returns immediately if present in local directory or app cache).
     */
    fun getCachedLyrics(track: Track): List<LyricLine>? {
        // 1. Check adjacent .lrc file in storage
        val path = track.filePath
        if (path.startsWith("/") || path.startsWith("file://")) {
            val cleanPath = path.removePrefix("file://")
            val audioFile = File(cleanPath)
            val lrcFile = File(audioFile.parentFile, "${audioFile.nameWithoutExtension}.lrc")
            if (lrcFile.exists() && lrcFile.canRead()) {
                try {
                    val content = lrcFile.readText()
                    val parsed = lyricsParser.parseLrc(content)
                    if (parsed.isNotEmpty()) return parsed
                } catch (e: Exception) {
                    Log.w(TAG, "Error reading adjacent .lrc: ${e.message}")
                }
            }
        }

        // 2. Check disk cache
        val cachedFile = File(lyricsCacheDir, "${track.id}.lrc")
        if (cachedFile.exists() && cachedFile.canRead()) {
            try {
                val content = cachedFile.readText()
                val parsed = lyricsParser.parseLrc(content)
                if (parsed.isNotEmpty()) return parsed
            } catch (e: Exception) {
                Log.w(TAG, "Error reading cached .lrc: ${e.message}")
            }
        }

        return null
    }

    /**
     * Fetches real synchronized lyrics online from LRCLIB if not found locally.
     */
    suspend fun fetchLyricsOnline(track: Track): List<LyricLine> = withContext(Dispatchers.IO) {
        val cached = getCachedLyrics(track)
        if (cached != null) return@withContext cached

        val cleanTitle = cleanSongTitle(track.title)
        val cleanArtist = cleanArtistName(track.artist)
        val durationSec = (track.durationMs / 1000).toInt()

        var lrcContent: String? = null

        // 1. Exact GET request
        try {
            val encodedTitle = URLEncoder.encode(cleanTitle, "UTF-8")
            val encodedArtist = URLEncoder.encode(cleanArtist, "UTF-8")
            val urlStr = "$LRCLIB_GET_URL?track_name=$encodedTitle&artist_name=$encodedArtist&duration=$durationSec"

            val connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "iPodModern-Audio-Android (https://github.com/santjsx/music-palyer)")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)
                val synced = json.optString("syncedLyrics", "")
                if (synced.isNotBlank()) {
                    lrcContent = synced
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Direct LRCLIB get error: ${e.message}")
        }

        // 2. Fallback: Search request if exact match was not found
        if (lrcContent == null) {
            try {
                val query = URLEncoder.encode("$cleanTitle $cleanArtist".trim(), "UTF-8")
                val searchUrl = "$LRCLIB_SEARCH_URL?q=$query"

                val connection = (URL(searchUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("User-Agent", "iPodModern-Audio-Android (https://github.com/santjsx/music-palyer)")
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                    val array = JSONArray(jsonStr)
                    if (array.length() > 0) {
                        for (i in 0 until array.length()) {
                            val item = array.getJSONObject(i)
                            val synced = item.optString("syncedLyrics", "")
                            if (synced.isNotBlank()) {
                                lrcContent = synced
                                break
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "LRCLIB search error: ${e.message}")
            }
        }

        // 3. If found, cache and parse
        if (!lrcContent.isNullOrBlank()) {
            try {
                val cachedFile = File(lyricsCacheDir, "${track.id}.lrc")
                cachedFile.writeText(lrcContent)
            } catch (e: Exception) {
                Log.w(TAG, "Could not write to lyrics cache: ${e.message}")
            }
            return@withContext lyricsParser.parseLrc(lrcContent)
        }

        emptyList()
    }

    private fun cleanSongTitle(raw: String): String {
        return raw
            .replace(Regex("(?i)\\[(official|audio|video|lyrics|hd|4k|remastered)[^\\]]*\\]"), "")
            .replace(Regex("(?i)\\((official|audio|video|lyrics|hd|4k|remastered)[^\\)]*\\)"), "")
            .replace(Regex("(?i)\\((from|feat\\.|ft\\.)[^\\)]*\\)"), "")
            .replace(Regex("(?i)\\[(from|feat\\.|ft\\.)[^\\]]*\\]"), "")
            .replace(Regex("(?i)ft\\..*"), "")
            .replace(Regex("(?i)feat\\..*"), "")
            .trim()
    }

    private fun cleanArtistName(raw: String): String {
        return raw
            .split(",", "/", "&", "feat.", "ft.")
            .firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() && !it.equals("Unknown Artist", ignoreCase = true) }
            ?: ""
    }
}
