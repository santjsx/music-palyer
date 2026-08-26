package com.ipodmodern.audio.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ipodmodern.audio.ui.components.MiniPlayerBar
import com.ipodmodern.audio.ui.screens.CoverFlowScreen
import com.ipodmodern.audio.ui.screens.DisplayScreen
import com.ipodmodern.audio.ui.screens.EqualizerScreen
import com.ipodmodern.audio.ui.screens.LyricsScreen
import com.ipodmodern.audio.ui.screens.MenuListScreen
import com.ipodmodern.audio.ui.screens.NowPlayingScreen
import com.ipodmodern.audio.ui.screens.ScreenType
import com.ipodmodern.audio.ui.screens.SyncServerScreen
import com.ipodmodern.audio.ui.theme.ChassisMaterial
import com.ipodmodern.audio.ui.theme.IPodModernTheme
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
            val chassisTheme by remember { mutableStateOf(ChassisMaterial.SPACE_TITANIUM) }

            IPodModernTheme(chassis = chassisTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    IPodAppModernContent(
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
fun IPodAppModernContent(
    playerViewModel: PlayerViewModel,
    menuViewModel: MenuViewModel,
    coverFlowViewModel: CoverFlowViewModel,
    syncViewModel: SyncViewModel
) {
    val playerState by playerViewModel.uiState.collectAsState()
    val navState by menuViewModel.navState.collectAsState()
    val coverFlowState by coverFlowViewModel.uiState.collectAsState()
    val syncServerState by syncViewModel.serverState.collectAsState()

    var activeScreen by remember { mutableStateOf(ScreenType.MENU_MAIN) }

    fun handleBack() {
        playerViewModel.hapticEngine.performClick()
        when (activeScreen) {
            ScreenType.NOW_PLAYING,
            ScreenType.COVER_FLOW,
            ScreenType.EQUALIZER,
            ScreenType.LYRICS,
            ScreenType.SYNC_SERVER -> {
                activeScreen = ScreenType.MENU_MAIN
                menuViewModel.loadMainMenu()
            }
            else -> {
                if (!menuViewModel.onMenuBack()) {
                    activeScreen = ScreenType.MENU_MAIN
                }
            }
        }
    }

    // Android hardware / gesture back handling
    BackHandler(enabled = activeScreen != ScreenType.MENU_MAIN || navState.backStack.isNotEmpty()) {
        handleBack()
    }

    val screenTitle = when (activeScreen) {
        ScreenType.NOW_PLAYING -> "Now Playing"
        ScreenType.COVER_FLOW -> "Cover Flow"
        ScreenType.EQUALIZER -> "10-Band EQ"
        ScreenType.LYRICS -> "Lyrics"
        ScreenType.SYNC_SERVER -> "Wi-Fi Sync"
        else -> navState.screenTitle
    }

    DisplayScreen(
        currentScreen = activeScreen,
        screenTitle = screenTitle,
        isPlaying = playerState.isPlaying,
        onBackClick = if (activeScreen != ScreenType.MENU_MAIN || navState.backStack.isNotEmpty()) {
            { handleBack() }
        } else null,
        bottomBar = if (activeScreen != ScreenType.NOW_PLAYING && activeScreen != ScreenType.LYRICS && playerState.currentTrack != null) {
            {
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
        } else null
    ) {
        when (activeScreen) {
            ScreenType.NOW_PLAYING -> {
                NowPlayingScreen(
                    track = playerState.currentTrack,
                    positionMs = playerState.positionMs,
                    durationMs = playerState.durationMs,
                    isPlaying = playerState.isPlaying,
                    currentTrackIndex = playerState.currentTrackIndex,
                    totalTracks = playerState.totalTracksInQueue,
                    currentLyricText = playerState.currentLyricText,
                    volumeLevel = playerState.volume,
                    onPlayPauseClick = { playerViewModel.togglePlayPause() },
                    onNextClick = { playerViewModel.nextTrack() },
                    onPrevClick = { playerViewModel.prevTrack() },
                    onSeekTo = { playerViewModel.seekByTicks(0) },
                    onVolumeChange = { playerViewModel.adjustVolume(((it - playerState.volume) * 25).toInt()) },
                    onLyricsClick = { activeScreen = ScreenType.LYRICS },
                    onEqClick = { activeScreen = ScreenType.EQUALIZER }
                )
            }
            ScreenType.COVER_FLOW -> {
                CoverFlowScreen(
                    albums = coverFlowState.albums,
                    selectedIndex = coverFlowState.selectedIndex,
                    onIndexChanged = { coverFlowViewModel.onRotate(it - coverFlowState.selectedIndex) },
                    onAlbumSelect = { album ->
                        activeScreen = ScreenType.NOW_PLAYING
                    }
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
            ScreenType.LYRICS -> {
                LyricsScreen(
                    lyrics = playerState.lyrics,
                    activeLyricIndex = playerState.activeLyricIndex,
                    songTitle = playerState.currentTrack?.title ?: ""
                )
            }
            ScreenType.SYNC_SERVER -> {
                SyncServerScreen(serverState = syncServerState)
            }
            else -> {
                MenuListScreen(
                    items = navState.items,
                    selectedIndex = navState.selectedIndex,
                    onItemClick = { index ->
                        menuViewModel.onRotate(index - navState.selectedIndex)
                        menuViewModel.onCenterAction(
                            onPlayTrack = { tracks, startIndex ->
                                playerViewModel.setQueue(tracks, startIndex, autoPlay = true)
                            },
                            onNavigateScreen = { targetScreen ->
                                activeScreen = targetScreen
                            }
                        )
                    }
                )
            }
        }
    }
}
