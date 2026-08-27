package com.ipodmodern.audio.ui.viewmodel

import android.app.Application
import android.media.MediaMetadataRetriever
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ipodmodern.audio.core.audio.UnifiedAudioEngine
import com.ipodmodern.audio.core.database.MusicDatabase
import com.ipodmodern.audio.core.database.entity.AlbumEntity
import com.ipodmodern.audio.core.database.entity.ArtistEntity
import com.ipodmodern.audio.core.database.entity.TrackEntity
import com.ipodmodern.audio.core.haptics.HapticEngine
import com.ipodmodern.audio.core.model.EqualizerPreset
import com.ipodmodern.audio.core.model.LyricLine
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.core.parser.LocalMusicScanner
import com.ipodmodern.audio.core.parser.LyricsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlayerUiState(
    val currentTrack: Track? = null,
    val allTracks: List<Track> = emptyList(),
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 1.0f,
    val showVolumeOverlay: Boolean = false,
    val currentTrackIndex: Int = 1,
    val totalTracksInQueue: Int = 0,
    val lyrics: List<LyricLine> = emptyList(),
    val activeLyricIndex: Int = -1,
    val currentLyricText: String? = null,
    val eqGains: FloatArray = FloatArray(10) { 0.0f },
    val selectedEqBandIndex: Int = 0,
    val dynamicPrecutDb: Float = 0.0f,
    val currentPresetName: String = "Audiophile Flat",
    val isScanning: Boolean = false
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MusicDatabase.getInstance(application)
    val hapticEngine = HapticEngine(application)
    private val lyricsParser = LyricsParser()
    private val localMusicScanner = LocalMusicScanner(application)
    val audioEngine = UnifiedAudioEngine(application)

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var playbackQueue: List<Track> = emptyList()
    private var queueIndex: Int = 0
    private var tickerJob: Job? = null
    private var volumeDismissJob: Job? = null

    init {
        scanAndLoadLocalMusic()
        startPositionTicker()
    }

    fun rescanLibrary() {
        scanAndLoadLocalMusic()
    }

    private fun scanAndLoadLocalMusic() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isScanning = true)
            val scannedTracks = localMusicScanner.scanDeviceAudio()

            if (scannedTracks.isNotEmpty()) {
                db.trackDao().clearAll()
                db.albumDao().clearAll()
                db.artistDao().clearAll()

                db.trackDao().insertTracks(scannedTracks.map { TrackEntity.fromDomain(it) })

                val albumGroups = scannedTracks.groupBy { it.album }
                val albums = albumGroups.map { (albumTitle, tracks) ->
                    AlbumEntity(
                        title = albumTitle,
                        artist = tracks.first().artist,
                        trackCount = tracks.size,
                        year = tracks.first().year,
                        artworkUri = tracks.firstOrNull { it.artworkUri != null }?.artworkUri,
                        isHiRes = tracks.any { it.sampleRate > 48000 }
                    )
                }
                db.albumDao().insertAlbums(albums)

                val artistGroups = scannedTracks.groupBy { it.artist }
                val artists = artistGroups.map { (artistName, tracks) ->
                    ArtistEntity(
                        name = artistName,
                        albumCount = tracks.map { it.album }.distinct().size,
                        trackCount = tracks.size
                    )
                }
                db.artistDao().insertArtists(artists)

                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(isScanning = false)
                    setQueue(scannedTracks, 0, autoPlay = false)
                }
            } else {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isScanning = false,
                        currentTrack = null,
                        allTracks = emptyList(),
                        totalTracksInQueue = 0,
                        isPlaying = false,
                        positionMs = 0L,
                        durationMs = 0L,
                        lyrics = emptyList(),
                        currentLyricText = null
                    )
                }
            }
        }
    }

    private fun startPositionTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                if (_uiState.value.isPlaying) {
                    val pos = audioEngine.getCurrentPosition()
                    val dur = audioEngine.getDuration().coerceAtLeast(_uiState.value.currentTrack?.durationMs ?: 0L)

                    val lyrics = _uiState.value.lyrics
                    val activeIdx = if (lyrics.isNotEmpty()) lyricsParser.findActiveLyricIndex(lyrics, pos) else -1
                    val lyricText = if (activeIdx >= 0 && activeIdx < lyrics.size) lyrics[activeIdx].text else null

                    _uiState.value = _uiState.value.copy(
                        positionMs = pos,
                        durationMs = dur,
                        activeLyricIndex = activeIdx,
                        currentLyricText = lyricText
                    )
                }
                delay(150)
            }
        }
    }

    fun setQueue(tracks: List<Track>, startIndex: Int = 0, autoPlay: Boolean = true) {
        if (tracks.isEmpty()) {
            playbackQueue = emptyList()
            queueIndex = 0
            _uiState.value = _uiState.value.copy(
                currentTrack = null,
                allTracks = emptyList(),
                totalTracksInQueue = 0,
                isPlaying = false,
                lyrics = emptyList(),
                currentLyricText = null
            )
            return
        }
        playbackQueue = tracks
        queueIndex = startIndex.coerceIn(0, tracks.size - 1)
        loadAndPlayCurrent(autoPlay)
    }

    private fun loadAndPlayCurrent(startPlaying: Boolean) {
        val track = playbackQueue.getOrNull(queueIndex) ?: return
        audioEngine.loadAndPlay(track.filePath, autoPlay = startPlaying)

        // Load real lyrics from local .lrc file if present (no mock strings)
        val realLyrics = localMusicScanner.loadLyricsForTrack(track)

        _uiState.value = _uiState.value.copy(
            currentTrack = track,
            allTracks = playbackQueue,
            isPlaying = startPlaying,
            positionMs = 0L,
            durationMs = track.durationMs,
            currentTrackIndex = queueIndex + 1,
            totalTracksInQueue = playbackQueue.size,
            lyrics = realLyrics,
            activeLyricIndex = if (realLyrics.isNotEmpty()) 0 else -1,
            currentLyricText = realLyrics.firstOrNull()?.text
        )
    }

    fun playTrackAtIndex(index: Int) {
        hapticEngine.performClick()
        if (playbackQueue.isNotEmpty() && index in playbackQueue.indices) {
            queueIndex = index
            loadAndPlayCurrent(true)
        }
    }

    fun togglePlayPause() {
        hapticEngine.performClick()
        if (_uiState.value.isPlaying) {
            audioEngine.pause()
            _uiState.value = _uiState.value.copy(isPlaying = false)
        } else {
            audioEngine.play()
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
            audioEngine.seekTo(0)
            _uiState.value = _uiState.value.copy(positionMs = 0L)
        } else if (playbackQueue.isNotEmpty()) {
            queueIndex = if (queueIndex - 1 < 0) playbackQueue.size - 1 else queueIndex - 1
            loadAndPlayCurrent(true)
        }
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
        _uiState.value = _uiState.value.copy(positionMs = positionMs)
    }

    fun adjustVolume(deltaTicks: Int) {
        val newVol = (_uiState.value.volume + deltaTicks * 0.04f).coerceIn(0.0f, 1.0f)
        audioEngine.setVolume(newVol)
        _uiState.value = _uiState.value.copy(volume = newVol, showVolumeOverlay = true)

        volumeDismissJob?.cancel()
        volumeDismissJob = viewModelScope.launch {
            delay(1500)
            _uiState.value = _uiState.value.copy(showVolumeOverlay = false)
        }
    }

    fun setVolumeDirect(vol: Float) {
        val newVol = vol.coerceIn(0.0f, 1.0f)
        audioEngine.setVolume(newVol)
        _uiState.value = _uiState.value.copy(volume = newVol)
    }

    fun seekByTicks(deltaTicks: Int) {
        val deltaMs = deltaTicks * 3000L
        val newPos = (_uiState.value.positionMs + deltaMs).coerceIn(0L, _uiState.value.durationMs)
        audioEngine.seekTo(newPos)
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

        audioEngine.setEqBandGain(bandIdx, newGain)

        _uiState.value = _uiState.value.copy(
            eqGains = currentGains,
            currentPresetName = "Custom EQ"
        )
    }

    fun applyEqPreset(preset: EqualizerPreset) {
        for (i in 0 until 10) {
            audioEngine.setEqBandGain(i, preset.bandGains[i])
        }
        _uiState.value = _uiState.value.copy(
            eqGains = preset.bandGains.copyOf(),
            currentPresetName = preset.name
        )
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
