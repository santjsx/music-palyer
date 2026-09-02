package com.ipodmodern.audio.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.ipodmodern.audio.core.ota.OtaUpdateManager
import com.ipodmodern.audio.ui.components.InAppUpdateSheet
import com.ipodmodern.audio.ui.components.MiniPlayerBar
import com.ipodmodern.audio.ui.components.ModernBottomNavIsland
import com.ipodmodern.audio.ui.components.ModernTab
import com.ipodmodern.audio.ui.screens.EffectsScreen
import com.ipodmodern.audio.ui.screens.EqualizerScreen
import com.ipodmodern.audio.ui.screens.LyricsScreen
import com.ipodmodern.audio.ui.screens.ModernHomeScreen
import com.ipodmodern.audio.ui.screens.ModernLibraryScreen
import com.ipodmodern.audio.ui.screens.ModernNowPlayingScreen
import com.ipodmodern.audio.ui.screens.ModernSearchScreen
import com.ipodmodern.audio.ui.screens.PlayingQueueScreen
import com.ipodmodern.audio.ui.screens.PlaylistDetailScreen
import com.ipodmodern.audio.ui.screens.PlaylistsScreen
import com.ipodmodern.audio.ui.screens.ScreenType
import com.ipodmodern.audio.ui.screens.SettingsScreen
import com.ipodmodern.audio.ui.screens.SyncServerScreen
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.viewmodel.CoverFlowViewModel
import com.ipodmodern.audio.ui.viewmodel.MenuViewModel
import com.ipodmodern.audio.ui.viewmodel.PlayerViewModel
import com.ipodmodern.audio.ui.viewmodel.SyncViewModel

class MainActivity : ComponentActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()
    private val menuViewModel: MenuViewModel by viewModels()
    private val coverFlowViewModel: CoverFlowViewModel by viewModels()
    private val syncViewModel: SyncViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val audioGranted = permissions[Manifest.permission.READ_MEDIA_AUDIO] == true ||
                        permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                if (audioGranted) {
                    playerViewModel.rescanLibrary()
                }
            }

            LaunchedEffect(Unit) {
                val permissionsToRequest = mutableListOf<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
                    }
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }

                if (permissionsToRequest.isNotEmpty()) {
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                }
            }

            val playerState by playerViewModel.uiState.collectAsState()

            com.ipodmodern.audio.ui.theme.AppTheme(
                baseTheme = playerState.themeBase,
                accentColor = playerState.accentColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBg
                ) {
                    ModernMusicAppContent(
                        playerViewModel = playerViewModel,
                        menuViewModel = menuViewModel,
                        coverFlowViewModel = coverFlowViewModel,
                        syncViewModel = syncViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ModernMusicAppContent(
    playerViewModel: PlayerViewModel,
    menuViewModel: MenuViewModel,
    coverFlowViewModel: CoverFlowViewModel,
    syncViewModel: SyncViewModel
) {
    val playerState by playerViewModel.uiState.collectAsState()
    val syncServerState by syncViewModel.serverState.collectAsState()

    val context = LocalContext.current
    val otaManager = remember { OtaUpdateManager.getInstance(context) }
    val otaState by otaManager.updateState.collectAsState()

    LaunchedEffect(Unit) {
        otaManager.checkForUpdates(isManualCheck = false)
    }

    var activeScreen by remember { mutableStateOf(ScreenType.MENU_MAIN) }
    var selectedPlaylistId by remember { mutableStateOf<Long?>(null) }
    var libraryCategory by remember { mutableStateOf(com.ipodmodern.audio.ui.screens.LibraryCategory.SONGS) }

    fun handleBack() {
        playerViewModel.hapticEngine.performClick()
        when (activeScreen) {
            ScreenType.NOW_PLAYING -> activeScreen = ScreenType.MENU_MAIN
            ScreenType.EQUALIZER, ScreenType.EFFECTS, ScreenType.PLAYING_QUEUE -> activeScreen = ScreenType.NOW_PLAYING
            ScreenType.LYRICS -> activeScreen = ScreenType.NOW_PLAYING
            ScreenType.PLAYLIST_DETAIL -> activeScreen = ScreenType.PLAYLISTS
            ScreenType.SYNC_SERVER -> activeScreen = ScreenType.SETTINGS
            ScreenType.SETTINGS -> activeScreen = ScreenType.MENU_MAIN
            ScreenType.SEARCH -> activeScreen = ScreenType.MENU_MAIN
            else -> activeScreen = ScreenType.MENU_MAIN
        }
    }

    BackHandler(enabled = activeScreen != ScreenType.MENU_MAIN) {
        handleBack()
    }

    val isFullScreenModal = activeScreen == ScreenType.NOW_PLAYING ||
            activeScreen == ScreenType.LYRICS ||
            activeScreen == ScreenType.EQUALIZER ||
            activeScreen == ScreenType.EFFECTS ||
            activeScreen == ScreenType.PLAYING_QUEUE ||
            activeScreen == ScreenType.PLAYLIST_DETAIL

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // MARK: - Screen Router
        when (activeScreen) {
            ScreenType.MENU_MAIN -> {
                ModernHomeScreen(
                    playerViewModel = playerViewModel,
                    onNavigateToSongs = {
                        libraryCategory = com.ipodmodern.audio.ui.screens.LibraryCategory.SONGS
                        activeScreen = ScreenType.MENU_MUSIC
                    },
                    onNavigateToAlbums = {
                        libraryCategory = com.ipodmodern.audio.ui.screens.LibraryCategory.ALBUMS
                        activeScreen = ScreenType.MENU_MUSIC
                    },
                    onNavigateToArtists = {
                        libraryCategory = com.ipodmodern.audio.ui.screens.LibraryCategory.ARTISTS
                        activeScreen = ScreenType.MENU_MUSIC
                    },
                    onNavigateToPlaylists = { activeScreen = ScreenType.PLAYLISTS },
                    onNavigateToNowPlaying = { activeScreen = ScreenType.NOW_PLAYING },
                    onOpenSyncHub = { activeScreen = ScreenType.SETTINGS },
                    onNavigateToSearch = { activeScreen = ScreenType.SEARCH }
                )
            }
            ScreenType.MENU_MUSIC,
            ScreenType.MENU_SONGS,
            ScreenType.MENU_ALBUMS,
            ScreenType.MENU_ARTISTS -> {
                ModernLibraryScreen(
                    tracks = playerState.allTracks,
                    albums = menuViewModel.cachedAlbums,
                    artists = menuViewModel.cachedArtists,
                    activeTrack = playerState.currentTrack,
                    isPlaying = playerState.isPlaying,
                    onTrackSelect = { track ->
                        playerViewModel.playTrack(track)
                        activeScreen = ScreenType.NOW_PLAYING
                    },
                    onShuffleAll = {
                        if (playerState.allTracks.isNotEmpty()) {
                            playerViewModel.playTrack(playerState.allTracks.random())
                            activeScreen = ScreenType.NOW_PLAYING
                        }
                    },
                    onOpenSettings = { activeScreen = ScreenType.SETTINGS },
                    initialCategory = libraryCategory
                )
            }
            ScreenType.SEARCH -> {
                ModernSearchScreen(
                    playerViewModel = playerViewModel,
                    onTrackSelect = { track ->
                        playerViewModel.playTrack(track)
                        activeScreen = ScreenType.NOW_PLAYING
                    },
                    onOpenSettings = { activeScreen = ScreenType.SETTINGS }
                )
            }
            ScreenType.NOW_PLAYING,
            ScreenType.COVER_FLOW -> {
                ModernNowPlayingScreen(
                    playerViewModel = playerViewModel,
                    onBackClick = { activeScreen = ScreenType.MENU_MAIN },
                    onOpenEqualizer = { activeScreen = ScreenType.EQUALIZER },
                    onOpenQueue = { activeScreen = ScreenType.PLAYING_QUEUE },
                    onOpenLyrics = { activeScreen = ScreenType.LYRICS }
                )
            }
            ScreenType.PLAYLISTS -> {
                PlaylistsScreen(
                    playerViewModel = playerViewModel,
                    onPlaylistClick = { pId ->
                        selectedPlaylistId = pId
                        activeScreen = ScreenType.PLAYLIST_DETAIL
                    }
                )
            }
            ScreenType.PLAYLIST_DETAIL -> {
                PlaylistDetailScreen(
                    playlistId = selectedPlaylistId ?: 0L,
                    playerViewModel = playerViewModel,
                    onBackClick = { activeScreen = ScreenType.PLAYLISTS }
                )
            }
            ScreenType.SETTINGS -> {
                SettingsScreen(
                    playerViewModel = playerViewModel,
                    onOpenEqualizer = { activeScreen = ScreenType.EQUALIZER },
                    onOpenEffects = { activeScreen = ScreenType.EFFECTS },
                    onOpenSyncHub = { activeScreen = ScreenType.SYNC_SERVER },
                    onCheckUpdates = { otaManager.checkForUpdates(isManualCheck = true) }
                )
            }
            ScreenType.EQUALIZER -> {
                EqualizerScreen(
                    bandGains = playerState.eqGains,
                    selectedBandIndex = playerState.selectedEqBandIndex,
                    onBandGainChange = { band, gain ->
                        playerViewModel.selectEqBand(band)
                        playerViewModel.adjustSelectedEqBand(((gain - playerState.eqGains[band]) * 2).toInt())
                    },
                    onPresetSelect = { playerViewModel.applyEqPreset(it) },
                    dynamicPrecutDb = playerState.dynamicPrecutDb,
                    presetName = playerState.currentPresetName,
                    onBackClick = { activeScreen = ScreenType.NOW_PLAYING }
                )
            }
            ScreenType.EFFECTS -> {
                EffectsScreen(
                    onBackClick = { activeScreen = ScreenType.SETTINGS }
                )
            }
            ScreenType.PLAYING_QUEUE -> {
                PlayingQueueScreen(
                    playerViewModel = playerViewModel,
                    onBackClick = { activeScreen = ScreenType.NOW_PLAYING }
                )
            }
            ScreenType.LYRICS -> {
                val progress by playerViewModel.playbackProgress.collectAsState()
                LyricsScreen(
                    lyrics = playerState.lyrics,
                    activeLyricIndex = progress.activeLyricIndex,
                    songTitle = playerState.currentTrack?.title ?: "Now Playing",
                    onSeekTo = { timestampMs ->
                        playerViewModel.seekTo(timestampMs)
                    },
                    onBackClick = { activeScreen = ScreenType.NOW_PLAYING }
                )
            }
            ScreenType.SYNC_SERVER -> {
                SyncServerScreen(
                    serverState = syncServerState,
                    onRescanClick = { playerViewModel.rescanLibrary() }
                )
            }
        }

        // MARK: - Floating Bottom Elements (Mini Player & Nav Island)
        if (!isFullScreenModal) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            ) {
                if (playerState.currentTrack != null) {
                    val progress by playerViewModel.playbackProgress.collectAsState()
                    MiniPlayerBar(
                        track = playerState.currentTrack,
                        isPlaying = playerState.isPlaying,
                        positionMs = progress.positionMs,
                        durationMs = progress.durationMs,
                        isFavorite = playerState.currentTrack?.let { playerState.favoriteTrackIds.contains(it.id) } == true,
                        onBarClick = { activeScreen = ScreenType.NOW_PLAYING },
                        onPlayPauseClick = { playerViewModel.togglePlayPause() },
                        onNextClick = { playerViewModel.nextTrack() },
                        onPrevClick = { playerViewModel.prevTrack() },
                        onFavoriteClick = {
                            playerState.currentTrack?.let { playerViewModel.toggleFavorite(it.id) }
                        },
                        onQueueClick = { activeScreen = ScreenType.PLAYING_QUEUE }
                    )
                }

                ModernBottomNavIsland(
                    currentScreen = activeScreen,
                    onTabSelected = { tab ->
                        playerViewModel.hapticEngine.performClick()
                        activeScreen = when (tab) {
                            ModernTab.EXPLORE -> ScreenType.MENU_MAIN
                            ModernTab.LIBRARY -> {
                                libraryCategory = com.ipodmodern.audio.ui.screens.LibraryCategory.SONGS
                                ScreenType.MENU_MUSIC
                            }
                            ModernTab.PLAY -> {
                                if (playerState.currentTrack != null) {
                                    ScreenType.NOW_PLAYING
                                } else if (playerState.allTracks.isNotEmpty()) {
                                    playerViewModel.playTrack(playerState.allTracks.first())
                                    ScreenType.NOW_PLAYING
                                } else {
                                    ScreenType.MENU_MUSIC
                                }
                            }
                            ModernTab.SEARCH -> ScreenType.SEARCH
                        }
                    }
                )
            }
        }

        // MARK: - Play Store Level In-App OTA Update Modal Sheet
        InAppUpdateSheet(
            updateStatus = otaState,
            updateManager = otaManager,
            onDismiss = { otaManager.dismissUpdate() }
        )
    }
}
