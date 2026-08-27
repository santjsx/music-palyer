package com.ipodmodern.audio.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.Album
import com.ipodmodern.audio.core.model.Artist
import com.ipodmodern.audio.core.model.AudioQuality
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.AudioQualityBadge
import com.ipodmodern.audio.ui.components.TactileIconButton
import com.ipodmodern.audio.ui.theme.ModernAccentBlue
import com.ipodmodern.audio.ui.theme.ModernAccentCyan
import com.ipodmodern.audio.ui.theme.ModernAccentEmerald
import com.ipodmodern.audio.ui.theme.ModernAccentGold
import com.ipodmodern.audio.ui.theme.ModernAccentPurple
import com.ipodmodern.audio.ui.theme.ModernAccentRose
import com.ipodmodern.audio.ui.theme.ModernCardDark
import com.ipodmodern.audio.ui.theme.ModernCardHighlight
import com.ipodmodern.audio.ui.theme.ModernHeroGradient
import com.ipodmodern.audio.ui.theme.ModernTextMuted
import com.ipodmodern.audio.ui.theme.ModernTextPrimary
import com.ipodmodern.audio.ui.theme.ModernTextSecondary
import java.io.File

enum class LibraryTab {
    SONGS,
    ALBUMS,
    ARTISTS,
    FAVORITES
}

@Composable
fun ModernLibraryScreen(
    tracks: List<Track>,
    albums: List<Album>,
    artists: List<Artist>,
    activeTrack: Track?,
    isPlaying: Boolean,
    isScanning: Boolean,
    favoriteTrackIds: Set<Long>,
    onTrackSelect: (List<Track>, Int) -> Unit,
    onShuffleAll: (List<Track>) -> Unit,
    onPlayAll: (List<Track>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRescanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(LibraryTab.SONGS) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Filtered lists
    val filteredTracks = remember(tracks, searchQuery) {
        if (searchQuery.isBlank()) tracks
        else tracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true) ||
                    it.album.contains(searchQuery, ignoreCase = true)
        }
    }

    val favoriteTracks = remember(tracks, favoriteTrackIds) {
        tracks.filter { favoriteTrackIds.contains(it.id) }
    }

    val filteredAlbums = remember(albums, searchQuery) {
        if (searchQuery.isBlank()) albums
        else albums.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredArtists = remember(artists, searchQuery) {
        if (searchQuery.isBlank()) artists
        else artists.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp)
    ) {
        // MARK: - Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Library",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ModernTextPrimary
                )
                Text(
                    text = "${tracks.size} Local Tracks • Hi-Res Direct",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ModernTextMuted
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Toggle Button
                TactileIconButton(
                    icon = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "Search",
                    isActive = isSearchActive,
                    onClick = {
                        isSearchActive = !isSearchActive
                        if (!isSearchActive) searchQuery = ""
                    },
                    size = 42.dp,
                    iconSize = 20.dp
                )

                // Rescan Button
                TactileIconButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = "Rescan",
                    isActive = isScanning,
                    activeColor = ModernAccentCyan,
                    onClick = onRescanClick,
                    size = 42.dp,
                    iconSize = 20.dp
                )
            }
        }

        // MARK: - Expandable Search Bar
        AnimatedVisibility(
            visible = isSearchActive,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141822))
                    .border(1.dp, Color(0x2AFFFFFF), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = ModernTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = ModernTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(ModernAccentBlue),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search tracks, artists, albums...",
                                    color = ModernTextMuted,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = ModernTextMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }
        }

        // MARK: - Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                title = "Songs",
                count = tracks.size,
                isSelected = activeTab == LibraryTab.SONGS,
                onClick = { activeTab = LibraryTab.SONGS }
            )
            CategoryChip(
                title = "Albums",
                count = albums.size,
                isSelected = activeTab == LibraryTab.ALBUMS,
                onClick = { activeTab = LibraryTab.ALBUMS }
            )
            CategoryChip(
                title = "Artists",
                count = artists.size,
                isSelected = activeTab == LibraryTab.ARTISTS,
                onClick = { activeTab = LibraryTab.ARTISTS }
            )
            CategoryChip(
                title = "Favorites",
                count = favoriteTracks.size,
                isSelected = activeTab == LibraryTab.FAVORITES,
                onClick = { activeTab = LibraryTab.FAVORITES }
            )
        }

        // MARK: - Quick Action Hero Row (Shuffle All & Play All)
        if (tracks.isNotEmpty() && activeTab != LibraryTab.ALBUMS && activeTab != LibraryTab.ARTISTS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Hero Shuffle All Button
                val shuffleSource = remember { MutableInteractionSource() }
                val isShufflePressed by shuffleSource.collectIsPressedAsState()
                val shuffleScale by animateFloatAsState(
                    targetValue = if (isShufflePressed) 0.94f else 1.0f,
                    animationSpec = spring(stiffness = 600f),
                    label = "shuffle_scale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .scale(shuffleScale)
                        .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = ModernAccentBlue.copy(alpha = 0.4f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(ModernHeroGradient)
                        .clickable(
                            interactionSource = shuffleSource,
                            indication = null,
                            onClick = {
                                val target = if (activeTab == LibraryTab.FAVORITES) favoriteTracks else filteredTracks
                                onShuffleAll(target)
                            }
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Shuffle All",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Play All Button
                val playSource = remember { MutableInteractionSource() }
                val isPlayPressed by playSource.collectIsPressedAsState()
                val playScale by animateFloatAsState(
                    targetValue = if (isPlayPressed) 0.94f else 1.0f,
                    animationSpec = spring(stiffness = 600f),
                    label = "play_scale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .scale(playScale)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF161A24))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = playSource,
                            indication = null,
                            onClick = {
                                val target = if (activeTab == LibraryTab.FAVORITES) favoriteTracks else filteredTracks
                                onPlayAll(target)
                            }
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = ModernTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Play All",
                            color = ModernTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // MARK: - Content Views
        if (tracks.isEmpty() && !isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = ModernAccentBlue,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "No Audio Files Found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ModernTextPrimary
                    )
                    Text(
                        text = "Place FLAC, WAV, MP3, or DSD music files in your phone's Music or Download folder and tap Scan.",
                        fontSize = 13.sp,
                        color = ModernTextSecondary,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ModernHeroGradient)
                            .clickable { onRescanClick() }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Scan Device for Audio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else if (isScanning && tracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = ModernAccentBlue)
                    Text(text = "Scanning Device Audio...", color = ModernTextSecondary, fontSize = 13.sp)
                }
            }
        } else {
            when (activeTab) {
                LibraryTab.SONGS -> {
                    SongListView(
                        tracks = filteredTracks,
                        activeTrack = activeTrack,
                        isPlaying = isPlaying,
                        favoriteTrackIds = favoriteTrackIds,
                        onTrackSelect = { index -> onTrackSelect(filteredTracks, index) },
                        onToggleFavorite = onToggleFavorite
                    )
                }
                LibraryTab.FAVORITES -> {
                    if (favoriteTracks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No Favorite Songs Yet", color = ModernTextMuted, fontSize = 14.sp)
                        }
                    } else {
                        SongListView(
                            tracks = favoriteTracks,
                            activeTrack = activeTrack,
                            isPlaying = isPlaying,
                            favoriteTrackIds = favoriteTrackIds,
                            onTrackSelect = { index -> onTrackSelect(favoriteTracks, index) },
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }
                LibraryTab.ALBUMS -> {
                    AlbumGridView(
                        albums = filteredAlbums,
                        allTracks = tracks,
                        onPlayAlbum = { albumTracks ->
                            onPlayAll(albumTracks)
                        }
                    )
                }
                LibraryTab.ARTISTS -> {
                    ArtistListView(
                        artists = filteredArtists,
                        allTracks = tracks,
                        onPlayArtist = { artistTracks ->
                            onPlayAll(artistTracks)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgBrush = if (isSelected) {
        ModernHeroGradient
    } else {
        Brush.linearGradient(listOf(Color(0xFF141720), Color(0xFF101218)))
    }

    val borderStroke = if (isSelected) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.10f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
            .border(1.dp, borderStroke, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else ModernTextSecondary
            )
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFF1E232E),
                            CircleShape
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else ModernTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun SongListView(
    tracks: List<Track>,
    activeTrack: Track?,
    isPlaying: Boolean,
    favoriteTrackIds: Set<Long>,
    onTrackSelect: (Int) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(tracks) { index, track ->
            val isActive = track.id == activeTrack?.id

            SongItemCard(
                track = track,
                isActive = isActive,
                isPlaying = isPlaying && isActive,
                isFavorite = favoriteTrackIds.contains(track.id),
                onClick = { onTrackSelect(index) },
                onFavoriteClick = { onToggleFavorite(track.id) }
            )
        }
    }
}

@Composable
private fun SongItemCard(
    track: Track,
    isActive: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(stiffness = 600f),
        label = "song_item_scale"
    )

    fun formatDuration(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%02d:%02d", min, sec)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (isActive) 12.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isActive) ModernAccentBlue.copy(alpha = 0.4f) else Color.Black
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (isActive) {
                    Brush.linearGradient(
                        listOf(Color(0xFF1E2433), Color(0xFF141824))
                    )
                } else {
                    Brush.linearGradient(
                        listOf(Color(0xFF12151C), Color(0xFF0D0F14))
                    )
                }
            )
            .border(
                1.dp,
                if (isActive) ModernAccentBlue.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Artwork Thumbnail
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E222D)),
                contentAlignment = Alignment.Center
            ) {
                if (!track.artworkUri.isNullOrEmpty()) {
                    val model = if (track.artworkUri.startsWith("/")) File(track.artworkUri) else track.artworkUri
                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = ModernAccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = track.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) ModernAccentBlue else ModernTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = track.artist,
                        fontSize = 12.sp,
                        color = ModernTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    AudioQualityBadge(
                        quality = when {
                            track.sampleRate > 48000 || track.bitDepth > 16 -> AudioQuality.HI_RES_LOSSLESS
                            track.formatName == "MP3" || track.formatName == "AAC" -> AudioQuality.LOSSY
                            else -> AudioQuality.LOSSLESS
                        },
                        badgeText = if (track.sampleRate > 48000) "HI-RES" else track.formatName
                    )
                }
            }

            // Duration & Favorite Heart
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatDuration(track.durationMs),
                    fontSize = 11.sp,
                    color = ModernTextMuted,
                    fontWeight = FontWeight.Medium
                )

                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) ModernAccentRose else ModernTextMuted.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onFavoriteClick
                        )
                )
            }
        }
    }
}

@Composable
private fun AlbumGridView(
    albums: List<Album>,
    allTracks: List<Track>,
    onPlayAlbum: (List<Track>) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(albums) { album ->
            val albumTracks = remember(album.title, allTracks) {
                allTracks.filter { it.album.equals(album.title, ignoreCase = true) }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF11141B))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                    .clickable { onPlayAlbum(albumTracks) }
                    .padding(10.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1A1E27)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!album.artworkUri.isNullOrEmpty()) {
                            val model = if (album.artworkUri.startsWith("/")) File(album.artworkUri) else album.artworkUri
                            AsyncImage(
                                model = model,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Album,
                                contentDescription = null,
                                tint = ModernAccentCyan,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = album.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ModernTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${album.artist} • ${album.trackCount} Tracks",
                        fontSize = 11.sp,
                        color = ModernTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistListView(
    artists: List<Artist>,
    allTracks: List<Track>,
    onPlayArtist: (List<Track>) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(artists) { _, artist ->
            val artistTracks = remember(artist.name, allTracks) {
                allTracks.filter { it.artist.equals(artist.name, ignoreCase = true) }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF11141B))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                    .clickable { onPlayArtist(artistTracks) }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(ModernHeroGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = artist.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernTextPrimary
                        )
                        Text(
                            text = "${artist.albumCount} Albums • ${artist.trackCount} Songs",
                            fontSize = 12.sp,
                            color = ModernTextSecondary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = ModernAccentBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
