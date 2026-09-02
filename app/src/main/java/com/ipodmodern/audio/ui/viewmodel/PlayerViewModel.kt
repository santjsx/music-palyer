package com.ipodmodern.audio.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ipodmodern.audio.core.audio.AudioPlaybackService
import com.ipodmodern.audio.core.audio.UnifiedAudioEngine
import com.ipodmodern.audio.core.database.MusicDatabase
import com.ipodmodern.audio.core.database.entity.AlbumEntity
import com.ipodmodern.audio.core.database.entity.ArtistEntity
import com.ipodmodern.audio.core.database.entity.TrackEntity
import com.ipodmodern.audio.core.haptics.HapticEngine
import com.ipodmodern.audio.core.lyrics.LyricsRepository
import com.ipodmodern.audio.core.model.EqualizerPreset
import com.ipodmodern.audio.core.model.LyricLine
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.core.parser.LocalMusicScanner
import com.ipodmodern.audio.core.parser.LyricsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlaybackProgress(
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val activeLyricIndex: Int = -1,
    val currentLyricText: String? = null
)

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
    val isShuffle: Boolean = false,
    val repeatMode: Int = 0, // 0 = OFF, 1 = ALL, 2 = ONE
    val favoriteTrackIds: Set<Long> = emptySet(),
    val lyrics: List<LyricLine> = emptyList(),
    val activeLyricIndex: Int = -1,
    val currentLyricText: String? = null,
    val eqGains: FloatArray = FloatArray(10) { 0.0f },
    val selectedEqBandIndex: Int = 0,
    val dynamicPrecutDb: Float = 0.0f,
    val currentPresetName: String = "Neo Flat",
    val isScanning: Boolean = false,
    val themeBase: String = "Obsidian Dark",
    val accentColor: String = "Mint Green",
    val playbackSpeed: Float = 1.0f,
    val lastPlayedTrack: Track? = null,
    val lastSavedPositionMs: Long = 0L
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MusicDatabase.getInstance(application)
    val hapticEngine = HapticEngine(application)
    private val lyricsParser = LyricsParser()
    private val lyricsRepository = LyricsRepository.getInstance(application)
    private val localMusicScanner = LocalMusicScanner(application)
    val audioEngine = UnifiedAudioEngine(application)

    private var lyricsFetchJob: Job? = null

    private val prefs = application.getSharedPreferences("aether_player_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME_BASE = "pref_theme_base"
        private const val KEY_ACCENT_COLOR = "pref_accent_color"
        private const val KEY_LAST_TRACK_ID = "pref_last_track_id"
        private const val KEY_LAST_POSITION_MS = "pref_last_pos_ms"
        private const val KEY_FAVORITES = "pref_favorites_set"
    }

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            themeBase = prefs.getString(KEY_THEME_BASE, "Obsidian Dark") ?: "Obsidian Dark",
            accentColor = prefs.getString(KEY_ACCENT_COLOR, "Mint Green") ?: "Mint Green",
            favoriteTrackIds = prefs.getStringSet(KEY_FAVORITES, emptySet())?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _playbackProgress = MutableStateFlow(PlaybackProgress())
    val playbackProgress: StateFlow<PlaybackProgress> = _playbackProgress.asStateFlow()

    val playlists: StateFlow<List<com.ipodmodern.audio.core.database.entity.PlaylistWithTracks>> = db.playlistDao()
        .getAllPlaylistsWithTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var originalQueue: List<Track> = emptyList()
    private var playbackQueue: List<Track> = emptyList()
    private var queueIndex: Int = 0
    private var tickerJob: Job? = null
    private var volumeDismissJob: Job? = null

    init {
        AudioPlaybackService.playbackActionListener = { action ->
            when (action) {
                AudioPlaybackService.ACTION_PLAY -> if (!_uiState.value.isPlaying) togglePlayPause()
                AudioPlaybackService.ACTION_PAUSE -> if (_uiState.value.isPlaying) togglePlayPause()
                AudioPlaybackService.ACTION_TOGGLE_PLAY -> togglePlayPause()
                AudioPlaybackService.ACTION_NEXT -> nextTrack()
                AudioPlaybackService.ACTION_PREV -> prevTrack()
            }
        }
        AudioPlaybackService.seekActionListener = { targetMs ->
            seekTo(targetMs)
        }

        audioEngine.onPlaybackCompleted = {
            onSongCompleted()
        }
        audioEngine.onPlaybackError = { errorMsg ->
            android.util.Log.w("PlayerViewModel", "Auto-recovering from playback error: $errorMsg")
            viewModelScope.launch(Dispatchers.Main) {
                if (playbackQueue.isNotEmpty()) {
                    nextTrack()
                }
            }
        }
        scanAndLoadLocalMusic()
        startPositionTicker()
    }

    fun setThemeBase(base: String) {
        prefs.edit().putString(KEY_THEME_BASE, base).apply()
        _uiState.value = _uiState.value.copy(themeBase = base)
    }

    fun setAccentColor(accent: String) {
        prefs.edit().putString(KEY_ACCENT_COLOR, accent).apply()
        _uiState.value = _uiState.value.copy(accentColor = accent)
    }

    private fun persistLastPlayback(trackId: Long, positionMs: Long) {
        prefs.edit()
            .putLong(KEY_LAST_TRACK_ID, trackId)
            .putLong(KEY_LAST_POSITION_MS, positionMs)
            .apply()
    }

    private fun updateForegroundNotification(track: Track?, isPlaying: Boolean) {
        val pos = _playbackProgress.value.positionMs
        val dur = if (_playbackProgress.value.durationMs > 0) _playbackProgress.value.durationMs else (track?.durationMs ?: 0L)
        AudioPlaybackService.updateService(getApplication(), track, isPlaying, pos, dur)
    }

    private fun onSongCompleted() {
        val state = _uiState.value
        when (state.repeatMode) {
            2 -> {
                seekTo(0)
                audioEngine.play()
                _uiState.value = _uiState.value.copy(isPlaying = true)
                updateForegroundNotification(_uiState.value.currentTrack, true)
            }
            1 -> {
                nextTrack()
            }
            else -> {
                if (queueIndex < playbackQueue.size - 1) {
                    nextTrack()
                } else {
                    seekTo(0)
                    audioEngine.pause()
                    _uiState.value = _uiState.value.copy(isPlaying = false)
                    updateForegroundNotification(_uiState.value.currentTrack, false)
                }
            }
        }
    }

    fun toggleShuffle() {
        hapticEngine.performClick()
        val newShuffle = !_uiState.value.isShuffle
        _uiState.value = _uiState.value.copy(isShuffle = newShuffle)

        if (playbackQueue.isEmpty()) return
        val current = playbackQueue.getOrNull(queueIndex)

        if (newShuffle) {
            val shuffled = originalQueue.shuffled().toMutableList()
            if (current != null) {
                shuffled.remove(current)
                shuffled.add(0, current)
            }
            playbackQueue = shuffled
            queueIndex = 0
        } else {
            playbackQueue = originalQueue
            queueIndex = if (current != null) originalQueue.indexOf(current).coerceAtLeast(0) else 0
        }

        _uiState.value = _uiState.value.copy(
            allTracks = playbackQueue,
            currentTrackIndex = queueIndex + 1,
            totalTracksInQueue = playbackQueue.size
        )
    }

    fun toggleRepeat() {
        hapticEngine.performClick()
        val nextMode = (_uiState.value.repeatMode + 1) % 3
        _uiState.value = _uiState.value.copy(repeatMode = nextMode)
    }

    fun toggleFavorite(trackId: Long? = null) {
        hapticEngine.performClick()
        val targetId = trackId ?: _uiState.value.currentTrack?.id ?: return
        val favs = _uiState.value.favoriteTrackIds.toMutableSet()
        if (favs.contains(targetId)) {
            favs.remove(targetId)
        } else {
            favs.add(targetId)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favs.map { it.toString() }.toSet()).apply()
        _uiState.value = _uiState.value.copy(favoriteTrackIds = favs)
    }

    fun shuffleAll(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        hapticEngine.performClick()
        originalQueue = tracks
        val shuffled = tracks.shuffled()
        playbackQueue = shuffled
        queueIndex = 0
        _uiState.value = _uiState.value.copy(isShuffle = true)
        loadAndPlayCurrent(true)
    }

    fun playAll(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        hapticEngine.performClick()
        originalQueue = tracks
        playbackQueue = tracks
        queueIndex = startIndex.coerceIn(0, (tracks.size - 1).coerceAtLeast(0))
        _uiState.value = _uiState.value.copy(isShuffle = false)
        loadAndPlayCurrent(true)
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
                    val activeTrack = _uiState.value.currentTrack
                    val isPlaying = _uiState.value.isPlaying

                    originalQueue = scannedTracks
                    if (playbackQueue.isEmpty()) {
                        playbackQueue = scannedTracks
                    }

                    val savedTrackId = prefs.getLong(KEY_LAST_TRACK_ID, -1L)
                    val savedPosMs = prefs.getLong(KEY_LAST_POSITION_MS, 0L)
                    val candidate = scannedTracks.firstOrNull { it.id == savedTrackId } ?: scannedTracks.firstOrNull()

                    _uiState.value = _uiState.value.copy(
                        isScanning = false,
                        allTracks = scannedTracks,
                        totalTracksInQueue = if (playbackQueue.isNotEmpty()) playbackQueue.size else scannedTracks.size,
                        lastPlayedTrack = candidate,
                        lastSavedPositionMs = savedPosMs
                    )

                    if (activeTrack == null && !isPlaying && candidate != null) {
                        val initIndex = scannedTracks.indexOf(candidate).coerceAtLeast(0)
                        setQueue(scannedTracks, initIndex, autoPlay = false)
                        if (savedPosMs > 0) {
                            _playbackProgress.value = _playbackProgress.value.copy(
                                positionMs = savedPosMs,
                                durationMs = candidate.durationMs
                            )
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(isScanning = false)
                }
            }
        }
    }

    private fun startPositionTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            var tickCount = 0
            while (isActive) {
                if (_uiState.value.isPlaying) {
                    val pos = audioEngine.getCurrentPosition()
                    val dur = audioEngine.getDuration().coerceAtLeast(_uiState.value.currentTrack?.durationMs ?: 0L)

                    val lyrics = _uiState.value.lyrics
                    val activeIdx = if (lyrics.isNotEmpty()) lyricsParser.findActiveLyricIndex(lyrics, pos) else -1
                    val lyricText = if (activeIdx >= 0 && activeIdx < lyrics.size) lyrics[activeIdx].text else null

                    _playbackProgress.value = PlaybackProgress(
                        positionMs = pos,
                        durationMs = dur,
                        activeLyricIndex = activeIdx,
                        currentLyricText = lyricText
                    )

                    // Persist timestamp every 2.5 seconds during playback
                    tickCount++
                    if (tickCount % 20 == 0) {
                        _uiState.value.currentTrack?.let {
                            persistLastPlayback(it.id, pos)
                        }
                    }
                }
                delay(120)
            }
        }
    }

    fun setQueue(tracks: List<Track>, startIndex: Int = 0, autoPlay: Boolean = true) {
        if (tracks.isEmpty()) {
            originalQueue = emptyList()
            playbackQueue = emptyList()
            queueIndex = 0
            _uiState.value = _uiState.value.copy(
                currentTrack = null,
                allTracks = emptyList(),
                totalTracksInQueue = 0,
                isPlaying = false,
                lyrics = emptyList()
            )
            _playbackProgress.value = PlaybackProgress()
            updateForegroundNotification(null, false)
            return
        }
        originalQueue = tracks
        playbackQueue = if (_uiState.value.isShuffle) {
            val s = tracks.shuffled().toMutableList()
            val initial = tracks.getOrNull(startIndex)
            if (initial != null) {
                s.remove(initial)
                s.add(0, initial)
            }
            s
        } else tracks

        queueIndex = if (_uiState.value.isShuffle) 0 else startIndex.coerceIn(0, (tracks.size - 1).coerceAtLeast(0))
        loadAndPlayCurrent(autoPlay)
    }

    private fun loadAndPlayCurrent(startPlaying: Boolean) {
        val track = playbackQueue.getOrNull(queueIndex) ?: return
        audioEngine.loadAndPlay(track.filePath, autoPlay = startPlaying)

        val cachedLyrics = lyricsRepository.getCachedLyrics(track) ?: emptyList()

        _uiState.value = _uiState.value.copy(
            currentTrack = track,
            allTracks = playbackQueue,
            isPlaying = startPlaying,
            currentTrackIndex = queueIndex + 1,
            totalTracksInQueue = playbackQueue.size,
            lyrics = cachedLyrics,
            lastPlayedTrack = track
        )

        _playbackProgress.value = PlaybackProgress(
            positionMs = 0L,
            durationMs = track.durationMs,
            activeLyricIndex = if (cachedLyrics.isNotEmpty()) 0 else -1,
            currentLyricText = cachedLyrics.firstOrNull()?.text
        )

        // If lyrics not found in local/cache, asynchronously fetch from LRCLIB
        lyricsFetchJob?.cancel()
        if (cachedLyrics.isEmpty()) {
            lyricsFetchJob = viewModelScope.launch {
                val fetchedLyrics = lyricsRepository.fetchLyricsOnline(track)
                if (fetchedLyrics.isNotEmpty() && _uiState.value.currentTrack?.id == track.id) {
                    _uiState.value = _uiState.value.copy(lyrics = fetchedLyrics)
                    val pos = audioEngine.getCurrentPosition()
                    val activeIdx = lyricsParser.findActiveLyricIndex(fetchedLyrics, pos)
                    _playbackProgress.value = _playbackProgress.value.copy(
                        activeLyricIndex = activeIdx,
                        currentLyricText = if (activeIdx >= 0 && activeIdx < fetchedLyrics.size) fetchedLyrics[activeIdx].text else null
                    )
                }
            }
        }

        persistLastPlayback(track.id, 0L)
        updateForegroundNotification(track, startPlaying)
    }

    fun resumeContinueListening() {
        hapticEngine.performClick()
        val track = _uiState.value.lastPlayedTrack ?: _uiState.value.currentTrack ?: return
        val isCurrentlyActive = _uiState.value.currentTrack?.id == track.id

        if (isCurrentlyActive && _uiState.value.isPlaying) {
            togglePlayPause()
            return
        }

        if (isCurrentlyActive && !_uiState.value.isPlaying) {
            val savedPos = _playbackProgress.value.positionMs
            audioEngine.play()
            if (savedPos > 0) {
                audioEngine.seekTo(savedPos)
            }
            _uiState.value = _uiState.value.copy(isPlaying = true)
            updateForegroundNotification(track, true)
            return
        }

        // Switch to the continue listening track
        val index = playbackQueue.indexOfFirst { it.id == track.id }
        if (index >= 0) {
            queueIndex = index
        }
        val targetPos = _uiState.value.lastSavedPositionMs.coerceAtLeast(0L)
        audioEngine.loadAndPlay(track.filePath, autoPlay = true)
        if (targetPos > 0) {
            audioEngine.seekTo(targetPos)
        }
        _uiState.value = _uiState.value.copy(
            currentTrack = track,
            isPlaying = true,
            currentTrackIndex = queueIndex + 1,
            lastPlayedTrack = track
        )
        _playbackProgress.value = PlaybackProgress(
            positionMs = targetPos,
            durationMs = track.durationMs
        )
        updateForegroundNotification(track, true)
    }

    fun playTrack(track: Track, customQueue: List<Track>? = null) {
        hapticEngine.performClick()
        if (customQueue != null && customQueue.isNotEmpty()) {
            val startIdx = customQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
            setQueue(customQueue, startIdx, autoPlay = true)
            return
        }
        val index = playbackQueue.indexOfFirst { it.id == track.id }
        if (index >= 0) {
            queueIndex = index
            loadAndPlayCurrent(true)
        } else {
            playbackQueue = listOf(track) + playbackQueue
            queueIndex = 0
            loadAndPlayCurrent(true)
        }
    }

    fun nextTrack() {
        if (playbackQueue.isEmpty()) return
        hapticEngine.performClick()
        queueIndex = (queueIndex + 1) % playbackQueue.size
        loadAndPlayCurrent(true)
    }

    fun prevTrack() {
        if (playbackQueue.isEmpty()) return
        hapticEngine.performClick()
        val currentPos = _playbackProgress.value.positionMs
        if (currentPos > 3000L) {
            seekTo(0L)
            return
        }
        queueIndex = if (queueIndex - 1 < 0) playbackQueue.size - 1 else queueIndex - 1
        loadAndPlayCurrent(true)
    }

    fun togglePlayPause() {
        hapticEngine.performClick()
        val state = _uiState.value
        val track = state.currentTrack ?: playbackQueue.getOrNull(queueIndex) ?: return

        if (state.isPlaying) {
            audioEngine.pause()
            _uiState.value = state.copy(isPlaying = false)
            val currentPos = _playbackProgress.value.positionMs
            persistLastPlayback(track.id, currentPos)
            updateForegroundNotification(track, false)
        } else {
            if (state.currentTrack == null) {
                loadAndPlayCurrent(true)
            } else {
                audioEngine.play()
                _uiState.value = state.copy(isPlaying = true)
                updateForegroundNotification(track, true)
            }
        }
    }

    fun seekTo(targetMs: Long) {
        val maxDuration = _playbackProgress.value.durationMs.coerceAtLeast(_uiState.value.currentTrack?.durationMs ?: 0L)
        val safePos = targetMs.coerceIn(0L, maxDuration.coerceAtLeast(0L))
        audioEngine.seekTo(safePos)
        _playbackProgress.value = _playbackProgress.value.copy(positionMs = safePos)
        _uiState.value.currentTrack?.let {
            persistLastPlayback(it.id, safePos)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        hapticEngine.performClick()
        audioEngine.setPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun seekForward10s() {
        hapticEngine.performClick()
        val currentPos = _playbackProgress.value.positionMs
        val maxDuration = _playbackProgress.value.durationMs.coerceAtLeast(_uiState.value.currentTrack?.durationMs ?: 0L)
        seekTo((currentPos + 10000L).coerceAtMost(maxDuration))
    }

    fun seekBackward10s() {
        hapticEngine.performClick()
        val currentPos = _playbackProgress.value.positionMs
        seekTo((currentPos - 10000L).coerceAtLeast(0L))
    }

    fun setVolume(volume: Float) {
        val safeVolume = volume.coerceIn(0.0f, 1.0f)
        audioEngine.setVolume(safeVolume)
        _uiState.value = _uiState.value.copy(
            volume = safeVolume,
            showVolumeOverlay = true
        )
        volumeDismissJob?.cancel()
        volumeDismissJob = viewModelScope.launch {
            delay(1500)
            _uiState.value = _uiState.value.copy(showVolumeOverlay = false)
        }
    }

    fun selectEqBand(bandIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedEqBandIndex = bandIndex.coerceIn(0, 9))
    }

    fun adjustSelectedEqBand(step: Int) {
        val band = _uiState.value.selectedEqBandIndex
        val current = _uiState.value.eqGains.getOrNull(band) ?: 0f
        val newGain = (current + step * 0.5f).coerceIn(-12f, 12f)
        setEqBandGain(band, newGain)
    }

    fun applyEqPreset(preset: EqualizerPreset) {
        hapticEngine.performClick()
        val gains = preset.bandGains.copyOf()
        for (i in gains.indices) {
            audioEngine.setEqBandGain(i, gains[i])
        }
        _uiState.value = _uiState.value.copy(
            eqGains = gains,
            currentPresetName = preset.name
        )
    }

    fun setPreampGain(gainDb: Float) {
        _uiState.value = _uiState.value.copy(dynamicPrecutDb = gainDb)
    }

    fun setEqBandGain(bandIndex: Int, gainDb: Float) {
        if (bandIndex in _uiState.value.eqGains.indices) {
            val updated = _uiState.value.eqGains.copyOf()
            updated[bandIndex] = gainDb.coerceIn(-12.0f, 12.0f)
            audioEngine.setEqBandGain(bandIndex, updated[bandIndex])
            _uiState.value = _uiState.value.copy(
                eqGains = updated,
                currentPresetName = "Custom"
            )
        }
    }

    fun createPlaylist(name: String, colorHex: Long = 0xFF256BFE, trackIds: List<Long> = emptyList()) {
        viewModelScope.launch(Dispatchers.IO) {
            db.playlistDao().createPlaylistWithTracks(
                com.ipodmodern.audio.core.database.entity.PlaylistEntity(
                    name = name,
                    colorHex = colorHex,
                    isAiGenerated = false,
                    createdAt = System.currentTimeMillis()
                ),
                trackIds = trackIds
            )
        }
    }

    fun generateAiPlaylists(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val tracks = _uiState.value.allTracks
            val aiPlaylists = com.ipodmodern.audio.core.ai.AiPlaylistClassifier.classifyLibrary(tracks)
            var count = 0
            for (res in aiPlaylists) {
                db.playlistDao().createPlaylistWithTracks(res.entity, res.trackIds)
                count++
            }
            withContext(Dispatchers.Main) {
                onComplete(count)
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            db.playlistDao().insertCrossRef(
                com.ipodmodern.audio.core.database.entity.PlaylistTrackCrossRef(
                    playlistId = playlistId,
                    trackId = trackId
                )
            )
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            db.playlistDao().removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            db.playlistDao().clearPlaylistTracks(playlistId)
            db.playlistDao().deletePlaylist(playlistId)
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.playlistDao().renamePlaylist(playlistId, newName)
        }
    }

    fun playPlaylist(playlistWithTracks: com.ipodmodern.audio.core.database.entity.PlaylistWithTracks, startTrack: Track? = null) {
        val domainTracks = playlistWithTracks.toDomainTracks()
        if (domainTracks.isNotEmpty()) {
            val first = startTrack ?: domainTracks.first()
            playTrack(first, domainTracks)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        volumeDismissJob?.cancel()
    }
}
