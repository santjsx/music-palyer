package com.ipodmodern.audio.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.ipodmodern.audio.ui.components.ChassisContainer
import com.ipodmodern.audio.ui.components.ClickWheel
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
            var chassisTheme by remember { mutableStateOf(ChassisMaterial.SPACE_TITANIUM) }
            var isHoldActive by remember { mutableStateOf(false) }

            IPodModernTheme(chassis = chassisTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    IPodAppContent(
                        playerViewModel = playerViewModel,
                        menuViewModel = menuViewModel,
                        coverFlowViewModel = coverFlowViewModel,
                        syncViewModel = syncViewModel,
                        isHoldActive = isHoldActive,
                        onToggleHold = { isHoldActive = it }
                    )
                }
            }
        }
    }
}

@Composable
fun IPodAppContent(
    playerViewModel: PlayerViewModel,
    menuViewModel: MenuViewModel,
    coverFlowViewModel: CoverFlowViewModel,
    syncViewModel: SyncViewModel,
    isHoldActive: Boolean,
    onToggleHold: (Boolean) -> Unit
) {
    val playerState by playerViewModel.uiState.collectAsState()
    val navState by menuViewModel.navState.collectAsState()
    val coverFlowState by coverFlowViewModel.uiState.collectAsState()
    val syncServerState by syncViewModel.serverState.collectAsState()

    var activeScreen by remember { mutableStateOf(ScreenType.MENU_MAIN) }

    // Synchronize menu navigation state
    val screenTitle = when (activeScreen) {
        ScreenType.NOW_PLAYING -> "Now Playing"
        ScreenType.COVER_FLOW -> "Cover Flow"
        ScreenType.EQUALIZER -> "10-Band EQ"
        ScreenType.LYRICS -> "Lyrics"
        ScreenType.SYNC_SERVER -> "Wi-Fi Sync"
        else -> navState.screenTitle
    }

    ChassisContainer(
        isHoldActive = isHoldActive,
        onToggleHold = {
            playerViewModel.hapticEngine.performClick()
            onToggleHold(it)
        },
        screenContent = {
            DisplayScreen(
                currentScreen = activeScreen,
                screenTitle = screenTitle,
                isPlaying = playerState.isPlaying,
                isHoldActive = isHoldActive
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
                            showVolumeOverlay = playerState.showVolumeOverlay
                        )
                    }
                    ScreenType.COVER_FLOW -> {
                        CoverFlowScreen(
                            albums = coverFlowState.albums,
                            selectedIndex = coverFlowState.selectedIndex,
                            onAlbumSelect = {
                                activeScreen = ScreenType.NOW_PLAYING
                            }
                        )
                    }
                    ScreenType.EQUALIZER -> {
                        EqualizerScreen(
                            bandGains = playerState.eqGains,
                            selectedBandIndex = playerState.selectedEqBandIndex,
                            onBandSelect = { playerViewModel.selectEqBand(it) },
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
                            onItemSelected = { index ->
                                menuViewModel.onRotate(index - navState.selectedIndex)
                            }
                        )
                    }
                }
            }
        },
        wheelContent = {
            ClickWheel(
                isHoldActive = isHoldActive,
                onMenuClick = {
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
                },
                onPlayPauseClick = {
                    playerViewModel.togglePlayPause()
                },
                onNextClick = {
                    when (activeScreen) {
                        ScreenType.EQUALIZER -> {
                            playerViewModel.selectEqBand((playerState.selectedEqBandIndex + 1) % 10)
                        }
                        else -> playerViewModel.nextTrack()
                    }
                },
                onPrevClick = {
                    when (activeScreen) {
                        ScreenType.EQUALIZER -> {
                            playerViewModel.selectEqBand(if (playerState.selectedEqBandIndex - 1 < 0) 9 else playerState.selectedEqBandIndex - 1)
                        }
                        else -> playerViewModel.prevTrack()
                    }
                },
                onCenterClick = {
                    when (activeScreen) {
                        ScreenType.NOW_PLAYING -> {
                            // Cycle between Now Playing and Lyrics
                            activeScreen = ScreenType.LYRICS
                        }
                        ScreenType.LYRICS -> {
                            activeScreen = ScreenType.NOW_PLAYING
                        }
                        ScreenType.COVER_FLOW -> {
                            activeScreen = ScreenType.NOW_PLAYING
                        }
                        ScreenType.EQUALIZER -> {
                            playerViewModel.selectEqBand((playerState.selectedEqBandIndex + 1) % 10)
                        }
                        else -> {
                            menuViewModel.onCenterAction(
                                onPlayTrack = { tracks, startIndex ->
                                    playerViewModel.setQueue(tracks, startIndex, autoPlay = true)
                                },
                                onNavigateScreen = { targetScreen ->
                                    activeScreen = targetScreen
                                }
                            )
                        }
                    }
                },
                onWheelRotate = { deltaTicks ->
                    when (activeScreen) {
                        ScreenType.NOW_PLAYING -> {
                            playerViewModel.adjustVolume(deltaTicks)
                        }
                        ScreenType.COVER_FLOW -> {
                            coverFlowViewModel.onRotate(deltaTicks)
                        }
                        ScreenType.EQUALIZER -> {
                            playerViewModel.adjustSelectedEqBand(deltaTicks)
                        }
                        ScreenType.LYRICS -> {
                            // Manual scrubbing in lyrics mode
                            playerViewModel.seekByTicks(deltaTicks)
                        }
                        else -> {
                            menuViewModel.onRotate(deltaTicks)
                        }
                    }
                }
            )
        }
    )
}
