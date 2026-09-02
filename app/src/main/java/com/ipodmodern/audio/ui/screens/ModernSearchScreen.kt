package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.AddToPlaylistSheet
import com.ipodmodern.audio.ui.theme.LocalThemePalette
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusMd
import com.ipodmodern.audio.ui.viewmodel.PlayerViewModel
import java.io.File

private enum class SearchFilter {
    ALL, SONGS, ARTISTS, ALBUMS
}

@Composable
fun ModernSearchScreen(
    playerViewModel: PlayerViewModel,
    onTrackSelect: (Track) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val palette = LocalThemePalette.current
    val uiState by playerViewModel.uiState.collectAsState()
    val allTracks = uiState.allTracks
    val activeTrack = uiState.currentTrack

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }
    var selectedActionTrack by remember { mutableStateOf<Track?>(null) }

    val filteredTracks = remember(searchQuery, selectedFilter, allTracks) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            emptyList()
        } else {
            allTracks.filter { track ->
                when (selectedFilter) {
                    SearchFilter.ALL -> track.title.contains(query, ignoreCase = true) ||
                            track.artist.contains(query, ignoreCase = true) ||
                            track.album.contains(query, ignoreCase = true)
                    SearchFilter.SONGS -> track.title.contains(query, ignoreCase = true)
                    SearchFilter.ARTISTS -> track.artist.contains(query, ignoreCase = true)
                    SearchFilter.ALBUMS -> track.album.contains(query, ignoreCase = true)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // 1. Top Header: "Search" + Settings Gear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search",
                color = palette.textPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(palette.elevated)
                    .border(1.dp, palette.border, CircleShape)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onOpenSettings()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = palette.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 2. Search Input Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RadiusFull)
                .background(palette.pill)
                .border(1.dp, palette.border, RadiusFull)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = palette.textMuted,
                    modifier = Modifier.size(20.dp)
                )

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(palette.accent),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Songs, artists, albums, folders...",
                                color = palette.textMuted,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.weight(1f)
                )

                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                searchQuery = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(SearchFilter.values()) { filter ->
                val isSelected = filter == selectedFilter
                val title = when (filter) {
                    SearchFilter.ALL -> "All"
                    SearchFilter.SONGS -> "Songs"
                    SearchFilter.ARTISTS -> "Artists"
                    SearchFilter.ALBUMS -> "Albums"
                }

                Box(
                    modifier = Modifier
                        .clip(RadiusFull)
                        .background(if (isSelected) palette.pillBg else palette.elevated)
                        .border(1.dp, if (isSelected) palette.accent else palette.border, RadiusFull)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            selectedFilter = filter
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) palette.accent else palette.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Content Area
        if (searchQuery.isBlank()) {
            // Default Browse Hub
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 6.dp, bottom = 180.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Quick Discovery",
                        color = palette.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Quick Discovery Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Hi-Res Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RadiusLg)
                                .background(palette.elevated)
                                .border(1.dp, palette.border, RadiusLg)
                                .clickable {
                                    searchQuery = "FLAC"
                                }
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = palette.accent, modifier = Modifier.size(24.dp))
                                Text("Hi-Res Audio", color = palette.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Lossless & Studio Master", color = palette.textMuted, fontSize = 11.sp)
                            }
                        }

                        // Favorites Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RadiusLg)
                                .background(palette.elevated)
                                .border(1.dp, palette.border, RadiusLg)
                                .clickable {
                                    if (allTracks.isNotEmpty()) {
                                        val fav = allTracks.firstOrNull { uiState.favoriteTrackIds.contains(it.id) }
                                        if (fav != null) onTrackSelect(fav)
                                    }
                                }
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = palette.accent, modifier = Modifier.size(24.dp))
                                Text("Favorites", color = palette.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("${uiState.favoriteTrackIds.size} saved tracks", color = palette.textMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "All Songs (${allTracks.size})",
                        color = palette.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(items = allTracks.take(20), key = { it.id }) { track ->
                    SearchResultTrackRow(
                        track = track,
                        isCurrent = track.id == activeTrack?.id,
                        accentColor = palette.accent,
                        onTrackClick = {
                            onTrackSelect(track)
                        },
                        onActionClick = {
                            selectedActionTrack = track
                        }
                    )
                }
            }
        } else {
            // Live Search Results
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 180.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    Text(
                        text = "${filteredTracks.size} results found",
                        color = palette.textMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (filteredTracks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = palette.textMuted,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No results found for \"$searchQuery\"",
                                    color = palette.textSecondary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    items(items = filteredTracks, key = { it.id }) { track ->
                        SearchResultTrackRow(
                            track = track,
                            isCurrent = track.id == activeTrack?.id,
                            accentColor = palette.accent,
                            onTrackClick = {
                                onTrackSelect(track)
                            },
                            onActionClick = {
                                selectedActionTrack = track
                            }
                        )
                    }
                }
            }
        }
    }

    if (selectedActionTrack != null) {
        AddToPlaylistSheet(
            track = selectedActionTrack!!,
            playerViewModel = playerViewModel,
            onDismiss = { selectedActionTrack = null }
        )
    }
}

@Composable
private fun SearchResultTrackRow(
    track: Track,
    isCurrent: Boolean,
    accentColor: Color,
    onTrackClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val view = LocalView.current
    val artworkModel = remember(track.artworkUri) {
        track.artworkUri?.let { if (it.startsWith("content://")) android.net.Uri.parse(it) else File(it) }
    }

    val durMin = (track.durationMs / 1000) / 60
    val durSec = (track.durationMs / 1000) % 60
    val durText = String.format("%d:%02d", durMin, durSec)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RadiusMd)
            .background(if (isCurrent) Color(0xFF1C1417) else Color.Transparent)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onTrackClick()
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RadiusMd)
                .background(Color(0xFF1C1D22)),
            contentAlignment = Alignment.Center
        ) {
            if (artworkModel != null) {
                AsyncImage(
                    model = artworkModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isCurrent) accentColor else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${track.artist} • ${track.album}",
                color = Color(0xFF8E8E93),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = durText,
            color = Color(0xFF636366),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onActionClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = Color(0xFF8E8E93),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
