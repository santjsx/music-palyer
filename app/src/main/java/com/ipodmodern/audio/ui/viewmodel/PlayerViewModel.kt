package com.ipodmodern.audio.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ipodmodern.audio.core.audio.NativeAudioBridge
import com.ipodmodern.audio.core.database.MusicDatabase
import com.ipodmodern.audio.core.haptics.HapticEngine
import com.ipodmodern.audio.core.model.EqualizerPreset
import com.ipodmodern.audio.core.model.LyricLine
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.core.parser.LyricsParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 1.0f,
    val showVolumeOverlay: Boolean = false,
    val currentTrackIndex: Int = 1,
    val totalTracksInQueue: Int = 1,
    val lyrics: List<LyricLine> = emptyList(),
    val activeLyricIndex: Int = -1,
    val currentLyricText: String? = null,
    val eqGains: FloatArray = FloatArray(10) { 0.0f },
    val selectedEqBandIndex: Int = 0,
    val dynamicPrecutDb: Float = 0.0f,
    val currentPresetName: String = "Audiophile Flat"
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MusicDatabase.getInstance(application)
    val hapticEngine = HapticEngine(application)
    private val lyricsParser = LyricsParser()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var playbackQueue: List<Track> = emptyList()
    private var queueIndex: Int = 0
    private var tickerJob: Job? = null
    private var volumeDismissJob: Job? = null

    init {
        NativeAudioBridge.initEngine(48000, true)
        loadSampleTracksIfEmpty()
        startPositionTicker()
    }

    private fun loadSampleTracksIfEmpty() {
        viewModelScope.launch {
            db.trackDao().getAllTracks().collect { tracks ->
                if (tracks.isEmpty()) {
                    // Seed initial high-resolution tracks
                    val sampleTracks = listOf(
                        Track(
                            title = "Time",
                            artist = "Pink Floyd",
                            album = "The Dark Side of the Moon",
                            durationMs = 425_000L,
                            filePath = "sample://pink_floyd_time.flac",
                            trackNumber = 4,
                            year = 1973,
                            formatName = "FLAC",
                            sampleRate = 96000,
                            bitDepth = 24,
                            badgeText = "HI-RES 24-BIT / 96.0kHz"
                        ),
                        Track(
                            title = "Hotel California (Live Master)",
                            artist = "Eagles",
                            album = "Hell Freezes Over",
                            durationMs = 432_000L,
                            filePath = "sample://eagles_hotel_california.flac",
                            trackNumber = 6,
                            year = 1994,
                            formatName = "FLAC",
                            sampleRate = 192000,
                            bitDepth = 24,
                            badgeText = "HI-RES 24-BIT / 192.0kHz"
                        ),
                        Track(
                            title = "So What",
                            artist = "Miles Davis",
                            album = "Kind of Blue",
                            durationMs = 562_000L,
                            filePath = "sample://miles_davis_so_what.dsf",
                            trackNumber = 1,
                            year = 1959,
                            formatName = "DSD",
                            sampleRate = 2822400,
                            bitDepth = 1,
                            badgeText = "DSD256 HI-RES"
                        ),
                        Track(
                            title = "Bohemian Rhapsody",
                            artist = "Queen",
                            album = "A Night at the Opera",
                            durationMs = 354_000L,
                            filePath = "sample://queen_bohemian_rhapsody.flac",
                            trackNumber = 11,
                            year = 1975,
                            formatName = "FLAC",
                            sampleRate = 44100,
                            bitDepth = 16,
                            badgeText = "LOSSLESS 16-BIT / 44.1kHz"
                        )
                    )
                    db.trackDao().insertTracks(sampleTracks.map {
                        com.ipodmodern.audio.core.database.entity.TrackEntity.fromDomain(it)
                    })
                    db.albumDao().insertAlbums(listOf(
                        com.ipodmodern.audio.core.database.entity.AlbumEntity(title = "The Dark Side of the Moon", artist = "Pink Floyd", trackCount = 1, year = 1973, artworkUri = null, isHiRes = true),
                        com.ipodmodern.audio.core.database.entity.AlbumEntity(title = "Hell Freezes Over", artist = "Eagles", trackCount = 1, year = 1994, artworkUri = null, isHiRes = true),
                        com.ipodmodern.audio.core.database.entity.AlbumEntity(title = "Kind of Blue", artist = "Miles Davis", trackCount = 1, year = 1959, artworkUri = null, isHiRes = true),
                        com.ipodmodern.audio.core.database.entity.AlbumEntity(title = "A Night at the Opera", artist = "Queen", trackCount = 1, year = 1975, artworkUri = null, isHiRes = false)
                    ))
                    db.artistDao().insertArtists(listOf(
                        com.ipodmodern.audio.core.database.entity.ArtistEntity(name = "Pink Floyd", albumCount = 1, trackCount = 1),
                        com.ipodmodern.audio.core.database.entity.ArtistEntity(name = "Eagles", albumCount = 1, trackCount = 1),
                        com.ipodmodern.audio.core.database.entity.ArtistEntity(name = "Miles Davis", albumCount = 1, trackCount = 1),
                        com.ipodmodern.audio.core.database.entity.ArtistEntity(name = "Queen", albumCount = 1, trackCount = 1)
                    ))
                } else {
                    if (_uiState.value.currentTrack == null) {
                        setQueue(tracks.map { it.toDomain() }, 0, autoPlay = false)
                    }
                }
            }
        }
    }

    private fun startPositionTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                if (_uiState.value.isPlaying) {
                    val pos = NativeAudioBridge.getCurrentPositionMs()
                    val dur = NativeAudioBridge.getDurationMs().coerceAtLeast(_uiState.value.currentTrack?.durationMs ?: 0L)
                    val precut = NativeAudioBridge.getDynamicPrecutGainDb()

                    val lyrics = _uiState.value.lyrics
                    val activeIdx = lyricsParser.findActiveLyricIndex(lyrics, pos)
                    val lyricText = if (activeIdx >= 0 && activeIdx < lyrics.size) lyrics[activeIdx].text else null

                    _uiState.value = _uiState.value.copy(
                        positionMs = pos,
                        durationMs = dur,
                        dynamicPrecutDb = precut,
                        activeLyricIndex = activeIdx,
                        currentLyricText = lyricText
                    )
                }
                delay(100)
            }
        }
    }

    fun setQueue(tracks: List<Track>, startIndex: Int = 0, autoPlay: Boolean = true) {
        if (tracks.isEmpty()) return
        playbackQueue = tracks
        queueIndex = startIndex.coerceIn(0, tracks.size - 1)
        loadAndPlayCurrent(autoPlay)
    }

    private fun loadAndPlayCurrent(startPlaying: Boolean) {
        val track = playbackQueue.getOrNull(queueIndex) ?: return
        NativeAudioBridge.loadTrack(track.filePath)

        if (startPlaying) {
            NativeAudioBridge.play()
        }

        // Mock Synchronized Lyrics for demonstration
        val sampleLrc = """
            [00:00.00]${track.title} • ${track.artist}
            [00:04.50]Audiophile Bit-Perfect Direct Stream
            [00:10.00]Zero-Phase Distortion Cascaded Biquad EQ
            [00:18.00]Direct Hardware HAL Low-Latency Bypass
            [00:30.00]Lossless Audio Processing Active
        """.trimIndent()
        val parsedLyrics = lyricsParser.parseLrc(sampleLrc)

        _uiState.value = _uiState.value.copy(
            currentTrack = track,
            isPlaying = startPlaying,
            positionMs = 0L,
            durationMs = track.durationMs,
            currentTrackIndex = queueIndex + 1,
            totalTracksInQueue = playbackQueue.size,
            lyrics = parsedLyrics,
            activeLyricIndex = 0,
            currentLyricText = parsedLyrics.firstOrNull()?.text
        )
    }

    fun togglePlayPause() {
        hapticEngine.performClick()
        if (_uiState.value.isPlaying) {
            NativeAudioBridge.pause()
            _uiState.value = _uiState.value.copy(isPlaying = false)
        } else {
            NativeAudioBridge.play()
            _uiState.value = _uiState.value.copy(isPlaying = true)
        }
    }

    fun nextTrack() {
        hapticEngine.performClick()
        if (playbackQueue.isNotEmpty()) {
            queueIndex = (queueIndex + 1) % playbackQueue.size
            loadAndPlayCurrent(true)
        }
    }

    fun prevTrack() {
        hapticEngine.performClick()
        if (_uiState.value.positionMs > 3000L) {
            NativeAudioBridge.seekTo(0)
            _uiState.value = _uiState.value.copy(positionMs = 0L)
        } else if (playbackQueue.isNotEmpty()) {
            queueIndex = if (queueIndex - 1 < 0) playbackQueue.size - 1 else queueIndex - 1
            loadAndPlayCurrent(true)
        }
    }

    fun adjustVolume(deltaTicks: Int) {
        val newVol = (_uiState.value.volume + deltaTicks * 0.04f).coerceIn(0.0f, 1.0f)
        NativeAudioBridge.setVolume(newVol)
        _uiState.value = _uiState.value.copy(volume = newVol, showVolumeOverlay = true)

        volumeDismissJob?.cancel()
        volumeDismissJob = viewModelScope.launch {
            delay(1500)
            _uiState.value = _uiState.value.copy(showVolumeOverlay = false)
        }
    }

    fun seekByTicks(deltaTicks: Int) {
        val deltaMs = deltaTicks * 3000L
        val newPos = (_uiState.value.positionMs + deltaMs).coerceIn(0L, _uiState.value.durationMs)
        NativeAudioBridge.seekTo(newPos)
        _uiState.value = _uiState.value.copy(positionMs = newPos)
    }

    // EQ Adjustments
    fun selectEqBand(index: Int) {
        _uiState.value = _uiState.value.copy(selectedEqBandIndex = index.coerceIn(0, 9))
    }

    fun adjustSelectedEqBand(deltaTicks: Int) {
        val bandIdx = _uiState.value.selectedEqBandIndex
        val currentGains = _uiState.value.eqGains.copyOf()
        val currentGain = currentGains[bandIdx]
        val newGain = (currentGain + deltaTicks * 0.5f).coerceIn(-12.0f, 12.0f)
        currentGains[bandIdx] = newGain

        NativeAudioBridge.setEqBandGain(bandIdx, newGain)
        val precut = NativeAudioBridge.getDynamicPrecutGainDb()

        _uiState.value = _uiState.value.copy(
            eqGains = currentGains,
            dynamicPrecutDb = precut,
            currentPresetName = "Custom EQ"
        )
    }

    fun applyEqPreset(preset: EqualizerPreset) {
        NativeAudioBridge.setEqAllBands(preset.bandGains)
        val precut = NativeAudioBridge.getDynamicPrecutGainDb()
        _uiState.value = _uiState.value.copy(
            eqGains = preset.bandGains.copyOf(),
            dynamicPrecutDb = precut,
            currentPresetName = preset.name
        )
    }
}
