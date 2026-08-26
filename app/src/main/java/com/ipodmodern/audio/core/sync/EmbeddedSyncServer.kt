package com.ipodmodern.audio.core.sync

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Environment
import android.os.StatFs
import com.ipodmodern.audio.core.audio.AudioFormatDetector
import com.ipodmodern.audio.core.database.MusicDatabase
import com.ipodmodern.audio.core.database.entity.AlbumEntity
import com.ipodmodern.audio.core.database.entity.ArtistEntity
import com.ipodmodern.audio.core.database.entity.TrackEntity
import com.ipodmodern.audio.core.model.AudioQuality
import com.ipodmodern.audio.core.parser.CueSheetParser
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class SyncServerState(
    val isRunning: Boolean = false,
    val hostAddress: String = "",
    val port: Int = 8080,
    val totalUploadedFiles: Int = 0
)

class EmbeddedSyncServer(private val context: Context) {

    private val db = MusicDatabase.getInstance(context)
    private val cueParser = CueSheetParser()
    private val scope = CoroutineScope(Dispatchers.IO)

    private var serverEngine: ApplicationEngine? = null
    private val _serverState = MutableStateFlow(SyncServerState())
    val serverState: StateFlow<SyncServerState> = _serverState.asStateFlow()

    private val musicStorageDir: File by lazy {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "iPodMusic")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    fun startServer(port: Int = 8080) {
        if (_serverState.value.isRunning) return

        val ip = getLocalWifiIpAddress() ?: "127.0.0.1"

        try {
            serverEngine = embeddedServer(CIO, port = port, host = "0.0.0.0") {
                install(CORS) {
                    anyHost()
                    allowHeader(HttpHeaders.ContentType)
                    allowMethod(HttpMethod.Get)
                    allowMethod(HttpMethod.Post)
                    allowMethod(HttpMethod.Delete)
                }
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }

                routing {
                    get("/") {
                        call.respondText(WebPortalAssets.INDEX_HTML, ContentType.Text.Html)
                    }

                    get("/api/status") {
                        val statFs = StatFs(musicStorageDir.absolutePath)
                        val freeBytes = statFs.availableBlocksLong * statFs.blockSizeLong
                        val totalBytes = statFs.blockCountLong * statFs.blockSizeLong
                        val trackCount = db.trackDao().getTrackCount().first()
                        val albumCount = db.albumDao().getAlbumCount().first()
                        val artistCount = db.artistDao().getArtistCount().first()

                        val json = buildJsonObject {
                            put("deviceName", "iPod Modern (${android.os.Build.MODEL})")
                            put("ipAddress", ip)
                            put("port", port)
                            put("storageFreeBytes", freeBytes)
                            put("storageTotalBytes", totalBytes)
                            put("totalTracks", trackCount)
                            put("totalAlbums", albumCount)
                            put("totalArtists", artistCount)
                            put("isScanning", false)
                        }
                        call.respond(json.toString())
                    }

                    post("/api/upload") {
                        val multipart = call.receiveMultipart()
                        var uploadedCount = 0

                        multipart.forEachPart { part ->
                            if (part is PartData.FileItem) {
                                val fileName = part.originalFileName ?: "audio_${System.currentTimeMillis()}.flac"
                                val destFile = File(musicStorageDir, fileName)

                                part.streamProvider().use { input ->
                                    destFile.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }

                                scope.launch {
                                    processUploadedFile(destFile)
                                }
                                uploadedCount++
                            }
                            part.dispose()
                        }

                        _serverState.value = _serverState.value.copy(
                            totalUploadedFiles = _serverState.value.totalUploadedFiles + uploadedCount
                        )

                        call.respond(HttpStatusCode.OK, """{"status":"success","uploaded":$uploadedCount}""")
                    }
                }
            }.start(wait = false)

            _serverState.value = SyncServerState(
                isRunning = true,
                hostAddress = ip,
                port = port
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopServer() {
        serverEngine?.stop(1000, 2000)
        serverEngine = null
        _serverState.value = SyncServerState(isRunning = false)
    }

    private suspend fun processUploadedFile(file: File) {
        val ext = file.extension.lowercase()

        if (ext == "cue") {
            val cueSheet = cueParser.parse(file) ?: return
            val audioFile = File(file.parentFile, cueSheet.audioFileName)

            // Insert virtual tracks from CUE sheet
            for (cueTrack in cueSheet.tracks) {
                val durationMs = if (cueTrack.endMs != null) cueTrack.endMs - cueTrack.startMs else 0L
                val track = TrackEntity(
                    title = cueTrack.title,
                    artist = cueTrack.performer,
                    album = cueSheet.title,
                    durationMs = durationMs,
                    filePath = "${audioFile.absolutePath}#cue_${cueTrack.trackNumber}",
                    trackNumber = cueTrack.trackNumber,
                    year = 2026,
                    genre = "Audiophile",
                    artworkUri = null,
                    formatName = "CUE/FLAC",
                    sampleRate = 96000,
                    bitDepth = 24,
                    badgeText = "HI-RES 24-BIT / 96.0kHz",
                    isCueSplit = true,
                    cueStartMs = cueTrack.startMs,
                    cueEndMs = cueTrack.endMs ?: 0L
                )
                db.trackDao().insertTrack(track)
            }
            db.albumDao().insertAlbum(
                AlbumEntity(title = cueSheet.title, artist = cueSheet.performer, trackCount = cueSheet.tracks.size, year = 2026, artworkUri = null, isHiRes = true)
            )
            db.artistDao().insertArtist(
                ArtistEntity(name = cueSheet.performer, albumCount = 1, trackCount = cueSheet.tracks.size)
            )
            return
        }

        // Standard Lossless / Audio File Ingestion
        val meta = AudioFormatDetector.detect(context, android.net.Uri.fromFile(file), file.absolutePath)
        val title = file.nameWithoutExtension.replace('_', ' ')
        val artist = "Audiophile Master"
        val album = "High-Resolution Library"

        val track = TrackEntity(
            title = title,
            artist = artist,
            album = album,
            durationMs = meta.durationMs.coerceAtLeast(180_000L),
            filePath = file.absolutePath,
            trackNumber = 1,
            year = 2026,
            genre = "Audiophile",
            artworkUri = null,
            formatName = meta.formatName,
            sampleRate = meta.sampleRate,
            bitDepth = meta.bitDepth,
            badgeText = meta.badgeText,
            isCueSplit = false
        )

        db.trackDao().insertTrack(track)
        db.albumDao().insertAlbum(
            AlbumEntity(title = album, artist = artist, trackCount = 1, year = 2026, artworkUri = null, isHiRes = meta.qualityCategory == AudioQuality.HI_RES_LOSSLESS)
        )
        db.artistDao().insertArtist(
            ArtistEntity(name = artist, albumCount = 1, trackCount = 1)
        )
    }

    private fun getLocalWifiIpAddress(): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val ipInt = wifiManager?.connectionInfo?.ipAddress ?: 0
        if (ipInt == 0) return null

        return InetAddress.getByAddress(
            ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()
        ).hostAddress
    }
}
