package com.ipodmodern.audio.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ipodmodern.audio.core.database.MusicDatabase
import com.ipodmodern.audio.core.haptics.HapticEngine
import com.ipodmodern.audio.core.model.Album
import com.ipodmodern.audio.core.model.Artist
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.screens.MenuItem
import com.ipodmodern.audio.ui.screens.ScreenType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class MenuNavigationState(
    val currentScreen: ScreenType = ScreenType.MENU_MAIN,
    val screenTitle: String = "iPod",
    val items: List<MenuItem> = emptyList(),
    val selectedIndex: Int = 0,
    val backStack: List<ScreenType> = emptyList()
)

class MenuViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MusicDatabase.getInstance(application)
    val hapticEngine = HapticEngine(application)

    private val _navState = MutableStateFlow(MenuNavigationState())
    val navState: StateFlow<MenuNavigationState> = _navState.asStateFlow()

    private var cachedTracks: List<Track> = emptyList()
    private var cachedAlbums: List<Album> = emptyList()
    private var cachedArtists: List<Artist> = emptyList()
    private var currentFilterArtist: String? = null
    private var currentFilterAlbum: String? = null

    init {
        loadMainMenu()
        observeDatabase()
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            db.trackDao().getAllTracks().collect { list ->
                cachedTracks = list.map { it.toDomain() }
                refreshCurrentSubmenu()
            }
        }
        viewModelScope.launch {
            db.albumDao().getAllAlbums().collect { list ->
                cachedAlbums = list.map { Album(it.id, it.title, it.artist, it.trackCount, it.year, it.artworkUri, it.isHiRes) }
                refreshCurrentSubmenu()
            }
        }
        viewModelScope.launch {
            db.artistDao().getAllArtists().collect { list ->
                cachedArtists = list.map { Artist(it.id, it.name, it.albumCount, it.trackCount) }
                refreshCurrentSubmenu()
            }
        }
    }

    private fun refreshCurrentSubmenu() {
        when (_navState.value.currentScreen) {
            ScreenType.MENU_SONGS -> {
                val tracks = if (currentFilterAlbum != null) {
                    cachedTracks.filter { it.album.equals(currentFilterAlbum, ignoreCase = true) }
                } else cachedTracks
                _navState.value = _navState.value.copy(
                    items = tracks.map { MenuItem(it.id.toString(), it.title, "${it.artist} • ${it.badgeText}", hasSubMenu = false) }
                )
            }
            ScreenType.MENU_ALBUMS -> {
                val albums = if (currentFilterArtist != null) {
                    cachedAlbums.filter { it.artist.equals(currentFilterArtist, ignoreCase = true) }
                } else cachedAlbums
                _navState.value = _navState.value.copy(
                    items = albums.map { MenuItem(it.title, it.title, it.artist, badge = if (it.isHiRes) "HI-RES" else null) }
                )
            }
            ScreenType.MENU_ARTISTS -> {
                _navState.value = _navState.value.copy(
                    items = cachedArtists.map { MenuItem(it.name, it.name, "${it.albumCount} Albums • ${it.trackCount} Tracks") }
                )
            }
            else -> {}
        }
    }

    fun loadMainMenu() {
        currentFilterArtist = null
        currentFilterAlbum = null
        _navState.value = MenuNavigationState(
            currentScreen = ScreenType.MENU_MAIN,
            screenTitle = "iPod",
            items = listOf(
                MenuItem("music", "Music", "Playlists, Artists, Albums"),
                MenuItem("cover_flow", "Cover Flow", "3D Album Gallery"),
                MenuItem("now_playing", "Now Playing", "Active Track"),
                MenuItem("equalizer", "Equalizer", "10-Band Biquad DSP"),
                MenuItem("lyrics", "Lyrics", "Synchronized Text"),
                MenuItem("sync", "Wi-Fi Sync", "Local Music Ingestion"),
                MenuItem("settings", "Settings", "Audio HAL & Themes")
            ),
            selectedIndex = 0,
            backStack = emptyList()
        )
    }

    fun onRotate(deltaTicks: Int) {
        val items = _navState.value.items
        if (items.isEmpty()) return

        val oldIndex = _navState.value.selectedIndex
        val newIndex = (oldIndex + deltaTicks).coerceIn(0, items.size - 1)

        if (newIndex != oldIndex) {
            hapticEngine.performTick()
            _navState.value = _navState.value.copy(selectedIndex = newIndex)
        } else if (deltaTicks != 0) {
            hapticEngine.performThud()
        }
    }

    fun onCenterAction(
        explicitIndex: Int? = null,
        onPlayTrack: (List<Track>, Int) -> Unit,
        onNavigateScreen: (ScreenType) -> Unit,
        onRescan: (() -> Unit)? = null
    ) {
        val items = _navState.value.items
        val sel = explicitIndex ?: _navState.value.selectedIndex
        val item = items.getOrNull(sel) ?: return
        hapticEngine.performClick()
        _navState.value = _navState.value.copy(selectedIndex = sel)

        when (_navState.value.currentScreen) {
            ScreenType.MENU_MAIN -> {
                when (item.id) {
                    "music" -> navigateTo(ScreenType.MENU_MUSIC, "Music", getMusicMenuItems())
                    "cover_flow" -> onNavigateScreen(ScreenType.COVER_FLOW)
                    "now_playing" -> onNavigateScreen(ScreenType.NOW_PLAYING)
                    "equalizer" -> onNavigateScreen(ScreenType.EQUALIZER)
                    "lyrics" -> onNavigateScreen(ScreenType.LYRICS)
                    "sync" -> onNavigateScreen(ScreenType.SYNC_SERVER)
                    "settings" -> navigateTo(ScreenType.SETTINGS, "Settings", getSettingsMenuItems())
                }
            }
            ScreenType.MENU_MUSIC -> {
                when (item.id) {
                    "artists" -> navigateTo(ScreenType.MENU_ARTISTS, "Artists", cachedArtists.map {
                        MenuItem(it.name, it.name, "${it.albumCount} Albums • ${it.trackCount} Tracks")
                    })
                    "albums" -> navigateTo(ScreenType.MENU_ALBUMS, "Albums", cachedAlbums.map {
                        MenuItem(it.title, it.title, it.artist, badge = if (it.isHiRes) "HI-RES" else null)
                    })
                    "songs" -> navigateTo(ScreenType.MENU_SONGS, "Songs", cachedTracks.mapIndexed { idx, t ->
                        MenuItem(t.id.toString(), t.title, "${t.artist} • ${t.badgeText}", hasSubMenu = false)
                    })
                    "rescan" -> {
                        onRescan?.invoke()
                    }
                }
            }
            ScreenType.MENU_ARTISTS -> {
                currentFilterArtist = item.id
                val artistAlbums = cachedAlbums.filter { it.artist.equals(item.id, ignoreCase = true) }
                navigateTo(ScreenType.MENU_ALBUMS, item.title, artistAlbums.map {
                    MenuItem(it.title, it.title, it.artist, badge = if (it.isHiRes) "HI-RES" else null)
                })
            }
            ScreenType.MENU_ALBUMS -> {
                currentFilterAlbum = item.id
                val albumTracks = cachedTracks.filter { it.album.equals(item.id, ignoreCase = true) }
                navigateTo(ScreenType.MENU_SONGS, item.title, albumTracks.map {
                    MenuItem(it.id.toString(), it.title, "${it.artist} • ${it.badgeText}", hasSubMenu = false)
                })
            }
            ScreenType.MENU_SONGS -> {
                val tracks = if (currentFilterAlbum != null) {
                    cachedTracks.filter { it.album.equals(currentFilterAlbum, ignoreCase = true) }
                } else cachedTracks

                if (tracks.isNotEmpty()) {
                    onPlayTrack(tracks, sel)
                    onNavigateScreen(ScreenType.NOW_PLAYING)
                }
            }
            else -> {}
        }
    }

    fun onMenuBack(): Boolean {
        val stack = _navState.value.backStack
        if (stack.isNotEmpty()) {
            hapticEngine.performClick()
            val prevScreen = stack.last()
            val newStack = stack.dropLast(1)
            when (prevScreen) {
                ScreenType.MENU_MAIN -> loadMainMenu()
                ScreenType.MENU_MUSIC -> {
                    currentFilterArtist = null
                    currentFilterAlbum = null
                    navigateTo(ScreenType.MENU_MUSIC, "Music", getMusicMenuItems(), backStack = newStack)
                }
                ScreenType.MENU_ARTISTS -> {
                    currentFilterAlbum = null
                    navigateTo(ScreenType.MENU_ARTISTS, "Artists", cachedArtists.map {
                        MenuItem(it.name, it.name, "${it.albumCount} Albums")
                    }, backStack = newStack)
                }
                ScreenType.MENU_ALBUMS -> navigateTo(ScreenType.MENU_ALBUMS, "Albums", cachedAlbums.map {
                    MenuItem(it.title, it.title, it.artist)
                }, backStack = newStack)
                else -> loadMainMenu()
            }
            return true
        }
        return false
    }

    private fun navigateTo(screen: ScreenType, title: String, items: List<MenuItem>, backStack: List<ScreenType>? = null) {
        val currentStack = backStack ?: (_navState.value.backStack + _navState.value.currentScreen)
        _navState.value = MenuNavigationState(
            currentScreen = screen,
            screenTitle = title,
            items = items,
            selectedIndex = 0,
            backStack = currentStack
        )
    }

    private fun getMusicMenuItems() = listOf(
        MenuItem("artists", "Artists", "By Performer"),
        MenuItem("albums", "Albums", "By Title"),
        MenuItem("songs", "Songs", "All Local Tracks"),
        MenuItem("rescan", "Rescan Music", "Scan Device for Audio", hasSubMenu = false)
    )

    private fun getSettingsMenuItems() = listOf(
        MenuItem("hal_exclusive", "Audio HAL Mode", "AAudio Exclusive Direct", hasSubMenu = false),
        MenuItem("haptics", "LRA Haptic Feedback", "15° Tick Enabled", hasSubMenu = false),
        MenuItem("theme_titanium", "Theme", "Space Titanium", hasSubMenu = false),
        MenuItem("about", "About", "iPod Modern v1.0.0 PRO", hasSubMenu = false)
    )
}
