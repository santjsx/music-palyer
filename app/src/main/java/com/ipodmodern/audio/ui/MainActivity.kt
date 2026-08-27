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
import androidx.core.content.ContextCompat
import com.ipodmodern.audio.ui.components.MiniPlayerBar
import com.ipodmodern.audio.ui.components.ModernBottomNavIsland
import com.ipodmodern.audio.ui.components.ModernTab
import com.ipodmodern.audio.ui.screens.EqualizerScreen
import com.ipodmodern.audio.ui.screens.LyricsScreen
import com.ipodmodern.audio.ui.screens.ModernLibraryScreen
import com.ipodmodern.audio.ui.screens.ModernNowPlayingScreen
import com.ipodmodern.audio.ui.screens.ScreenType
import com.ipodmodern.audio.ui.screens.SyncServerScreen
import com.ipodmodern.audio.ui.theme.AmberCanvas
import com.ipodmodern.audio.ui.theme.ModernAppTheme
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

            ModernAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AmberCanvas
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

    var activeScreen by remember { mutableStateOf(ScreenType.MENU_MAIN) }

    fun handleBack() {
        playerViewModel.hapticEngine.performClick()
        when (activeScreen) {
            ScreenType.LYRICS -> {
                activeScreen = ScreenType.NOW_PLAYING
            }
            ScreenType.NOW_PLAYING,
            ScreenType.COVER_FLOW,
            ScreenType.EQUALIZER,
            ScreenType.SYNC_SERVER -> {
                activeScreen = ScreenType.MENU_MAIN
            }
            else -> {
                if (!menuViewModel.onMenuBack()) {
                    activeScreen = ScreenType.MENU_MAIN
                }
            }
        }
    }

    BackHandler(enabled = activeScreen != ScreenType.MENU_MAIN) {
        handleBack()
    }

    val isFullScreenModal = activeScreen == ScreenType.NOW_PLAYING || activeScreen == ScreenType.LYRICS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmberCanvas)
    ) {
        // MARK: - Instant Zero-Lag Screen Switcher
        when (activeScreen) {
            ScreenType.NOW_PLAYING,
            ScreenType.COVER_FLOW -> {
                ModernNowPlayingScreen(
                    currentTrack = playerState.currentTrack,
                    allTracks = playerState.allTracks,
                    currentTrackIndex = playerState.currentTrackIndex,
                    positionMs = playerState.positionMs,
                    durationMs = playerState.durationMs,
                    isPlaying = playerState.isPlaying,
                    volumeLevel = playerState.volume,
                    isShuffle = playerState.isShuffle,
                    repeatMode = playerState.repeatMode,
                    isFavorite = playerState.currentTrack?.let { playerState.favoriteTrackIds.contains(it.id) } == true,
                    currentLyricText = playerState.currentLyricText,
                    onPlayPauseClick = { playerViewModel.togglePlayPause() },
                    onNextClick = { playerViewModel.nextTrack() },
                    onPrevClick = { playerViewModel.prevTrack() },
                    onTrackSelect = { index -> playerViewModel.playTrackAtIndex(index) },
                    onSeekTo = { targetMs -> playerViewModel.seekTo(targetMs) },
                    onVolumeChange = { vol -> playerViewModel.setVolumeDirect(vol) },
                    onToggleShuffle = { playerViewModel.toggleShuffle() },
                    onToggleRepeat = { playerViewModel.toggleRepeat() },
                    onToggleFavorite = {
                        playerState.currentTrack?.let { playerViewModel.toggleFavorite(it.id) }
                    },
                    onLyricsClick = { activeScreen = ScreenType.LYRICS },
                    onEqClick = { activeScreen = ScreenType.EQUALIZER },
                    onCollapseClick = { activeScreen = ScreenType.MENU_MAIN }
                )
            }
            ScreenType.LYRICS -> {
                LyricsScreen(
                    lyrics = playerState.lyrics,
                    activeLyricIndex = playerState.activeLyricIndex,
                    songTitle = playerState.currentTrack?.title ?: "Now Playing",
                    onSeekTo = { timestampMs ->
                        playerViewModel.seekTo(timestampMs)
                    },
                    onBackClick = { activeScreen = ScreenType.NOW_PLAYING }
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
                    presetName = playerState.currentPresetName
                )
            }
            ScreenType.SYNC_SERVER -> {
                SyncServerScreen(
                    serverState = syncServerState,
                    onRescanClick = { playerViewModel.rescanLibrary() }
                )
            }
            else -> {
                ModernLibraryScreen(
                    tracks = playerState.allTracks,
                    albums = menuViewModel.cachedAlbums,
                    artists = menuViewModel.cachedArtists,
                    activeTrack = playerState.currentTrack,
                    isPlaying = playerState.isPlaying,
                    isScanning = playerState.isScanning,
                    favoriteTrackIds = playerState.favoriteTrackIds,
                    onTrackSelect = { trackList, index ->
                        playerViewModel.setQueue(trackList, index, autoPlay = true)
                        activeScreen = ScreenType.NOW_PLAYING
                    },
                    onShuffleAll = { trackList ->
                        playerViewModel.shuffleAll(trackList)
                        activeScreen = ScreenType.NOW_PLAYING
                    },
                    onPlayAll = { trackList ->
                        playerViewModel.playAll(trackList, 0)
                        activeScreen = ScreenType.NOW_PLAYING
                    },
                    onToggleFavorite = { trackId ->
                        playerViewModel.toggleFavorite(trackId)
                    },
                    onRescanClick = {
                        playerViewModel.rescanLibrary()
                    }
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
                    MiniPlayerBar(
                        track = playerState.currentTrack,
                        isPlaying = playerState.isPlaying,
                        positionMs = playerState.positionMs,
                        durationMs = playerState.durationMs,
                        onBarClick = { activeScreen = ScreenType.NOW_PLAYING },
                        onPlayPauseClick = { playerViewModel.togglePlayPause() },
                        onNextClick = { playerViewModel.nextTrack() }
                    )
                }

                ModernBottomNavIsland(
                    currentScreen = activeScreen,
                    onTabSelected = { tab ->
                        playerViewModel.hapticEngine.performClick()
                        activeScreen = when (tab) {
                            ModernTab.LIBRARY -> ScreenType.MENU_MAIN
                            ModernTab.NOW_PLAYING -> ScreenType.NOW_PLAYING
                            ModernTab.EQUALIZER -> ScreenType.EQUALIZER
                            ModernTab.SYNC -> ScreenType.SYNC_SERVER
                        }
                    }
                )
            }
        }
    }
}
