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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.AddToPlaylistSheet
import com.ipodmodern.audio.ui.components.AmbientBackground
import com.ipodmodern.audio.ui.components.CategorySelectorRow
import com.ipodmodern.audio.ui.components.CollectionHeroCard
import com.ipodmodern.audio.ui.components.HomeSectionHeader
import com.ipodmodern.audio.ui.components.PopularSongCard
import com.ipodmodern.audio.ui.components.SongRowItem
import com.ipodmodern.audio.ui.theme.LocalThemePalette
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.viewmodel.PlayerViewModel
import java.util.Calendar

/**
 * ModernHomeScreen implements PRD Sections 12-15:
 * - Ambient Dark Canvas with artwork glow influence
 * - Personalized greeting header with quick search & sync actions
 * - Interactive horizontal category selector pills ([All], [Party], [Blues], [Soul], etc.)
 * - Popular Songs horizontal carousel with dominant artwork and quick play
 * - Curated collections hero cards with ambient gradient overlays
 * - Recently Played carousel
 * - Filtered songs list with high-contrast typography and instant playback
 */
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
    val palette = LocalThemePalette.current
    val uiState by playerViewModel.uiState.collectAsState()
    val view = LocalView.current

    val allTracks = uiState.allTracks
    val currentTrack = uiState.currentTrack
    val isPlaying = uiState.isPlaying
    val favoriteIds = uiState.favoriteTrackIds

    var selectedCategory by remember { mutableStateOf("All") }
    var selectedActionTrack by remember { mutableStateOf<Track?>(null) }

    val categories = remember {
        listOf("All", "Party", "Blues", "Soul", "Hip-Hop", "Rock", "Jazz", "Electronic")
    }

    // Dynamic greeting based on time of day
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    // Filtered tracks based on the category pill
    val filteredTracks = remember(selectedCategory, allTracks) {
        if (selectedCategory.equals("All", ignoreCase = true)) {
            allTracks
        } else {
            allTracks.filter { track ->
                track.genre.contains(selectedCategory, ignoreCase = true) ||
                        track.album.contains(selectedCategory, ignoreCase = true) ||
                        track.title.contains(selectedCategory, ignoreCase = true)
            }
        }
    }

    // Popular / Featured tracks (top slice or all)
    val popularTracks = remember(allTracks) {
        if (allTracks.size > 8) allTracks.take(8) else allTracks
    }

    // Recently played (first 6 or reversed)
    val recentlyPlayed = remember(allTracks) {
        if (allTracks.size > 4) allTracks.takeLast(6).reversed() else allTracks
    }

    AmbientBackground(
        modifier = modifier,
        ambientColor = palette.accent,
        ambientAlpha = 0.18f
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 140.dp) // Room for mini player & bottom nav island
        ) {
            // 1. Top Header: Time-aware greeting, profile / actions (PRD 12)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = greeting,
                            color = palette.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "TuneHive Music",
                            color = palette.textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Action buttons (Search, Sync Hub)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Wi-Fi Sync Indicator / Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(palette.surfaceElevated)
                                .border(1.dp, palette.borderSubtle, CircleShape)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onOpenSyncHub()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Wi-Fi Sync",
                                tint = palette.accent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Search Shortcut
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(palette.surfaceElevated)
                                .border(1.dp, palette.borderSubtle, CircleShape)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onNavigateToSearch()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = palette.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. Category Selector Chips (PRD 12)
            item {
                CategorySelectorRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { cat ->
                        selectedCategory = cat
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // 3. Popular Songs Carousel (PRD 13)
            if (popularTracks.isNotEmpty()) {
                item {
                    HomeSectionHeader(
                        title = "Popular Songs",
                        onSeeAllClick = onNavigateToSongs
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(popularTracks, key = { "pop_${it.id}" }) { track ->
                            PopularSongCard(
                                track = track,
                                isPlaying = isPlaying,
                                isCurrent = currentTrack?.id == track.id,
                                onClick = {
                                    playerViewModel.playTrack(track)
                                    onNavigateToNowPlaying()
                                },
                                onPlayDirect = {
                                    if (currentTrack?.id == track.id) {
                                        playerViewModel.togglePlayPause()
                                    } else {
                                        playerViewModel.playTrack(track)
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 4. Curated Collections Hero Banners (PRD 14)
            if (allTracks.isNotEmpty()) {
                item {
                    HomeSectionHeader(title = "Featured Collections")
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Card 1: Top Songs Global
                        item {
                            CollectionHeroCard(
                                title = "Top Songs Global",
                                subtitle = "Most played audio tracks",
                                gradientColors = listOf(
                                    Color(0xFF0D2818),
                                    Color(0xFF144D29),
                                    palette.surface
                                ),
                                trackCount = allTracks.size,
                                onPlayClick = {
                                    playerViewModel.playTrack(allTracks.first())
                                    onNavigateToNowPlaying()
                                },
                                onClick = onNavigateToSongs,
                                modifier = Modifier.width(260.dp)
                            )
                        }

                        // Card 2: Discover Mix
                        item {
                            CollectionHeroCard(
                                title = "Discover Flow",
                                subtitle = "Fresh selections tailored to you",
                                gradientColors = listOf(
                                    Color(0xFF0F2B26),
                                    Color(0xFF1B4D45),
                                    palette.surface
                                ),
                                trackCount = (allTracks.size / 2).coerceAtLeast(1),
                                onPlayClick = {
                                    val shuffled = allTracks.shuffled()
                                    if (shuffled.isNotEmpty()) {
                                        playerViewModel.playTrack(shuffled.first())
                                        onNavigateToNowPlaying()
                                    }
                                },
                                onClick = onNavigateToAlbums,
                                modifier = Modifier.width(260.dp)
                            )
                        }

                        // Card 3: Favorites Mix
                        item {
                            val favTracks = allTracks.filter { favoriteIds.contains(it.id) }
                            CollectionHeroCard(
                                title = "Favorites Mix",
                                subtitle = "${favTracks.size} saved favorites",
                                gradientColors = listOf(
                                    Color(0xFF281E0D),
                                    Color(0xFF4D3814),
                                    palette.surface
                                ),
                                trackCount = favTracks.size,
                                onPlayClick = {
                                    if (favTracks.isNotEmpty()) {
                                        playerViewModel.playTrack(favTracks.first())
                                        onNavigateToNowPlaying()
                                    } else if (allTracks.isNotEmpty()) {
                                        playerViewModel.playTrack(allTracks.first())
                                        onNavigateToNowPlaying()
                                    }
                                },
                                onClick = onNavigateToPlaylists,
                                modifier = Modifier.width(260.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 5. Recently Played Carousel (PRD 15)
            if (recentlyPlayed.isNotEmpty()) {
                item {
                    HomeSectionHeader(
                        title = "Recently Played",
                        onSeeAllClick = onNavigateToSongs
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(recentlyPlayed, key = { "recent_${it.id}" }) { track ->
                            PopularSongCard(
                                track = track,
                                isPlaying = isPlaying,
                                isCurrent = currentTrack?.id == track.id,
                                onClick = {
                                    playerViewModel.playTrack(track)
                                    onNavigateToNowPlaying()
                                },
                                onPlayDirect = {
                                    if (currentTrack?.id == track.id) {
                                        playerViewModel.togglePlayPause()
                                    } else {
                                        playerViewModel.playTrack(track)
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 6. Tracks List (Active Category Filtered)
            item {
                HomeSectionHeader(
                    title = if (selectedCategory == "All") "All Tracks" else "$selectedCategory Tracks",
                    onSeeAllClick = onNavigateToSongs
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (filteredTracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No songs found for $selectedCategory",
                            color = palette.textMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(filteredTracks.take(15), key = { "row_${it.id}" }) { track ->
                    SongRowItem(
                        track = track,
                        isCurrent = currentTrack?.id == track.id,
                        isPlaying = isPlaying,
                        isFavorite = favoriteIds.contains(track.id),
                        onTrackClick = {
                            playerViewModel.playTrack(track)
                            onNavigateToNowPlaying()
                        },
                        onFavoriteClick = {
                            playerViewModel.toggleFavorite(track.id)
                        },
                        onOptionsClick = {
                            selectedActionTrack = track
                        },
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }

    // Add to Playlist Bottom Sheet
    if (selectedActionTrack != null) {
        AddToPlaylistSheet(
            track = selectedActionTrack!!,
            playerViewModel = playerViewModel,
            onDismiss = { selectedActionTrack = null }
        )
    }
}
