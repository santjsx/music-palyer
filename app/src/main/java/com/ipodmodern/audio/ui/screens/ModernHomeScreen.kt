package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.SleekCard
import com.ipodmodern.audio.ui.components.SleekIconButton
import com.ipodmodern.audio.ui.theme.MintAccent
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.theme.ObsidianBorder
import com.ipodmodern.audio.ui.theme.ObsidianElevated
import com.ipodmodern.audio.ui.theme.ObsidianPill
import com.ipodmodern.audio.ui.theme.ObsidianSurface
import com.ipodmodern.audio.ui.theme.ObsidianTrackBg
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusMd
import com.ipodmodern.audio.ui.theme.RadiusSm
import com.ipodmodern.audio.ui.theme.RadiusXl
import com.ipodmodern.audio.ui.theme.TextMuted
import com.ipodmodern.audio.ui.theme.TextPrimary
import com.ipodmodern.audio.ui.theme.TextSecondary
import com.ipodmodern.audio.ui.viewmodel.PlayerViewModel
import java.io.File
import java.util.Calendar

@Composable
fun ModernHomeScreen(
    playerViewModel: PlayerViewModel,
    onNavigateToSongs: () -> Unit,
    onNavigateToAlbums: () -> Unit,
    onNavigateToArtists: () -> Unit,
    onNavigateToPlaylists: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
    onOpenSyncHub: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by playerViewModel.uiState.collectAsState()
    val progressState by playerViewModel.playbackProgress.collectAsState()
    val view = LocalView.current

    var searchQuery by remember { mutableStateOf("") }

    // Dynamic greeting based on time of day
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 180.dp)
    ) {
        // 1. Top Bar: Hamburger Menu + Notifications Bell
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SleekIconButton(
                    icon = Icons.Default.Menu,
                    onClick = { onOpenSyncHub() },
                    size = 40.dp,
                    iconSize = 20.dp,
                    contentDescription = "Menu"
                )

                SleekIconButton(
                    icon = Icons.Default.NotificationsNone,
                    onClick = { onOpenSyncHub() },
                    size = 40.dp,
                    iconSize = 20.dp,
                    contentDescription = "Notifications"
                )
            }
        }

        // 2. Greeting Header
        item {
            Column {
                Text(
                    text = greeting,
                    color = MintAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Let the music\nheal your soul.",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 30.sp
                )
            }
        }

        // 3. Search Bar Capsule -> Tapping opens dedicated real-time search
        item {
            val homePalette = com.ipodmodern.audio.ui.theme.LocalThemePalette.current
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RadiusFull)
                    .background(ObsidianPill)
                    .border(1.dp, ObsidianBorder, RadiusFull)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onNavigateToSearch()
                    }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = "Search songs, albums, artists...",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 4. Continue Listening (Hero Track Card)
        val continueTrack = uiState.lastPlayedTrack ?: uiState.currentTrack ?: uiState.allTracks.firstOrNull()
        if (continueTrack != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Continue Listening",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val artworkFile = remember(continueTrack.artworkUri) {
                        continueTrack.artworkUri?.let { File(it) }
                    }

                    val currentPos = if (uiState.currentTrack?.id == continueTrack.id && (uiState.isPlaying || progressState.positionMs > 0)) {
                        progressState.positionMs
                    } else {
                        uiState.lastSavedPositionMs
                    }
                    val currentDur = if (progressState.durationMs > 0) progressState.durationMs else continueTrack.durationMs

                    val progress = if (currentDur > 0) {
                        (currentPos.toFloat() / currentDur.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    val posMin = (currentPos / 1000) / 60
                    val posSec = (currentPos / 1000) % 60
                    val durMin = (currentDur / 1000) / 60
                    val durSec = (currentDur / 1000) % 60
                    val timeFormatted = String.format("%d:%02d / %d:%02d", posMin, posSec, durMin, durSec)

                    val isTrackActive = uiState.currentTrack?.id == continueTrack.id && uiState.isPlaying

                    SleekCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = ObsidianSurface,
                        shape = RadiusLg,
                        onClick = {
                            if (!uiState.isPlaying) {
                                playerViewModel.resumeContinueListening()
                            }
                            onNavigateToNowPlaying()
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Artwork
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RadiusMd)
                                    .background(ObsidianElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                if (artworkFile != null) {
                                    AsyncImage(
                                        model = artworkFile,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = MintAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Metadata & Progress Line
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = continueTrack.title,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = continueTrack.artist,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    // Apple/Tidal Style Audio Quality Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RadiusSm)
                                            .background(if (continueTrack.isHiRes) Color(0xFF2C2411) else Color(0x33FFFFFF))
                                            .border(0.5.dp, if (continueTrack.isHiRes) Color(0xFFFFD159) else Color(0x44FFFFFF), RadiusSm)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = continueTrack.displayBadge,
                                            color = if (continueTrack.isHiRes) Color(0xFFFFD159) else Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                // Progress Line + Time Text
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(3.dp)
                                            .clip(RadiusFull)
                                            .background(ObsidianTrackBg)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progress)
                                                .fillMaxHeight()
                                                .background(MintAccent)
                                        )
                                    }

                                    Text(
                                        text = timeFormatted,
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }

                            // Play/Pause Trigger
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isTrackActive) MintAccent else ObsidianElevated)
                                    .clickable {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        playerViewModel.resumeContinueListening()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isTrackActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isTrackActive) "Pause" else "Play",
                                    tint = if (isTrackActive) ObsidianBg else MintAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Recently Played (Horizontal Carousel)
        val allSongs = uiState.allTracks
        if (allSongs.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recently Played",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "See all",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onNavigateToSongs() }
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(items = allSongs.take(8), key = { it.id }) { track ->
                            val trackArt = remember(track.artworkUri) {
                                track.artworkUri?.let { File(it) }
                            }

                            Column(
                                modifier = Modifier
                                    .width(110.dp)
                                    .clip(RadiusMd)
                                    .clickable {
                                        playerViewModel.playTrack(track)
                                        onNavigateToNowPlaying()
                                    }
                            ) {
                                // Square Card with Play Overlay
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(RadiusLg)
                                        .background(ObsidianSurface)
                                        .border(1.dp, ObsidianBorder, RadiusLg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (trackArt != null) {
                                        AsyncImage(
                                            model = trackArt,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = MintAccent,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    // Audio Quality Badge (Top Left of Card)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                            .clip(RadiusSm)
                                            .background(Color(0xCC000000))
                                            .border(0.5.dp, if (track.isHiRes) Color(0xFFFFD159) else Color(0x44FFFFFF), RadiusSm)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (track.isHiRes) "HI-RES" else "LOSSLESS",
                                            color = if (track.isHiRes) Color(0xFFFFD159) else Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    // Play icon overlay pill
                                    val cardAccent = com.ipodmodern.audio.ui.theme.LocalThemePalette.current.accent
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(ObsidianBg.copy(alpha = 0.8f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = cardAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = track.title,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.artist,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Your Library Shortcuts (4-Column Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Your Library",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LibraryShortcutItem(
                        icon = Icons.Default.MusicNote,
                        title = "Songs",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSongs
                    )

                    LibraryShortcutItem(
                        icon = Icons.Default.Album,
                        title = "Albums",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAlbums
                    )

                    LibraryShortcutItem(
                        icon = Icons.Default.Person,
                        title = "Artists",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToArtists
                    )

                    LibraryShortcutItem(
                        icon = Icons.AutoMirrored.Filled.QueueMusic,
                        title = "Playlists",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPlaylists
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryShortcutItem(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val view = LocalView.current

    SleekCard(
        modifier = modifier.height(78.dp),
        backgroundColor = ObsidianSurface,
        shape = RadiusLg,
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val itemAccent = com.ipodmodern.audio.ui.theme.LocalThemePalette.current.accent
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = itemAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
