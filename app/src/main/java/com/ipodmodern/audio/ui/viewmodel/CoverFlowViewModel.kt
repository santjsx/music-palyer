package com.ipodmodern.audio.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ipodmodern.audio.core.database.MusicDatabase
import com.ipodmodern.audio.core.haptics.HapticEngine
import com.ipodmodern.audio.core.model.Album
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CoverFlowUiState(
    val albums: List<Album> = emptyList(),
    val selectedIndex: Int = 0
)

class CoverFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MusicDatabase.getInstance(application)
    val hapticEngine = HapticEngine(application)

    private val _uiState = MutableStateFlow(CoverFlowUiState())
    val uiState: StateFlow<CoverFlowUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            db.albumDao().getAllAlbums().collect { list ->
                val albumList = list.map {
                    Album(it.id, it.title, it.artist, it.trackCount, it.year, it.artworkUri, it.isHiRes)
                }
                _uiState.value = _uiState.value.copy(
                    albums = albumList,
                    selectedIndex = _uiState.value.selectedIndex.coerceIn(0, (albumList.size - 1).coerceAtLeast(0))
                )
            }
        }
    }

    fun onRotate(deltaTicks: Int) {
        val albums = _uiState.value.albums
        if (albums.isEmpty()) return

        val oldIndex = _uiState.value.selectedIndex
        val newIndex = (oldIndex + deltaTicks).coerceIn(0, albums.size - 1)

        if (newIndex != oldIndex) {
            hapticEngine.performTick()
            _uiState.value = _uiState.value.copy(selectedIndex = newIndex)
        } else if (deltaTicks != 0) {
            hapticEngine.performThud()
        }
    }
}
