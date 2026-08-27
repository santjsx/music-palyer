package com.ipodmodern.audio.ui.screens

import android.graphics.BitmapFactory
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.model.Album
import com.ipodmodern.audio.core.model.Artist
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.AetherAudioVisualizer
import com.ipodmodern.audio.ui.components.NeoBadge
import com.ipodmodern.audio.ui.components.NeoButton
import com.ipodmodern.audio.ui.components.NeoCard
import com.ipodmodern.audio.ui.components.NeoIconButton
import com.ipodmodern.audio.ui.components.neoShadow
import com.ipodmodern.audio.ui.theme.NeoBgDark
import com.ipodmodern.audio.ui.theme.NeoBlack
import com.ipodmodern.audio.ui.theme.NeoBlue
import com.ipodmodern.audio.ui.theme.NeoBorderWidth
import com.ipodmodern.audio.ui.theme.NeoGreen
import com.ipodmodern.audio.ui.theme.NeoMuted
import com.ipodmodern.audio.ui.theme.NeoPink
import com.ipodmodern.audio.ui.theme.NeoPurple
import com.ipodmodern.audio.ui.theme.NeoRadiusLg
import com.ipodmodern.audio.ui.theme.NeoRadiusMd
import com.ipodmodern.audio.ui.theme.NeoRadiusSm
import com.ipodmodern.audio.ui.theme.NeoWhite
import com.ipodmodern.audio.ui.theme.NeoYellow
import java.util.Locale

enum class LibraryCategory(val title: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists")
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
    val view = LocalView.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(LibraryCategory.SONGS) }

    val filteredTracks = remember(tracks, searchQuery) {
        if (searchQuery.isBlank()) tracks
        else tracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true) ||
                    it.album.contains(searchQuery, ignoreCase = true)
        }
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
            .background(NeoBgDark)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AETHER",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoYellow,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${tracks.size} LOSSLESS TRACKS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = NeoWhite
                )
            }

            NeoIconButton(
                icon = Icons.Default.Refresh,
                contentDescription = "Scan",
                onClick = onRescanClick,
                backgroundColor = NeoWhite,
                size = 42.dp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Neo-Brutalist Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neoShadow(
                    offsetX = 3.dp,
                    offsetY = 3.dp,
                    color = NeoBlack,
                    cornerRadius = 12.dp
                )
                .clip(NeoRadiusMd)
                .background(NeoWhite)
                .border(NeoBorderWidth, NeoBlack, NeoRadiusMd)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = NeoBlack,
                    modifier = Modifier.size(18.dp)
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search tracks, artists, albums...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeoMuted
                        )
                    }

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        cursorBrush = SolidColor(NeoBlack),
                        textStyle = TextStyle(
                            color = NeoBlack,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = NeoBlack,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Neo Category Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LibraryCategory.values().forEach { cat ->
                val isSelected = cat == selectedCategory
                NeoCard(
                    backgroundColor = if (isSelected) NeoYellow else NeoWhite,
                    shadowOffset = if (isSelected) 3.dp else 2.dp,
                    cornerRadius = 10.dp,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        selectedCategory = cat
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat.title.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = NeoBlack
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Shuffle / Play All Row
        if (selectedCategory == LibraryCategory.SONGS && filteredTracks.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeoButton(
                    text = "Shuffle",
                    icon = Icons.Default.Shuffle,
                    backgroundColor = NeoPurple,
                    textColor = NeoWhite,
                    modifier = Modifier.weight(1f),
                    onClick = { onShuffleAll(filteredTracks) }
                )

                NeoButton(
                    text = "Play All",
                    icon = Icons.Default.PlayArrow,
                    backgroundColor = NeoGreen,
                    textColor = NeoWhite,
                    modifier = Modifier.weight(1f),
                    onClick = { onPlayAll(filteredTracks) }
                )
            }
        }

        // Main List Content
        when (selectedCategory) {
            LibraryCategory.SONGS -> {
                if (filteredTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = if (isScanning) "Scanning device audio..." else "No tracks found.",
                            color = NeoWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            filteredTracks,
                            key = { it.id }
                        ) { track ->
                            val isCurrent = activeTrack?.id == track.id
                            NeoTrackRow(
                                track = track,
                                isCurrent = isCurrent,
                                isPlaying = isCurrent && isPlaying,
                                isFavorite = favoriteTrackIds.contains(track.id),
                                onClick = {
                                    val idx = filteredTracks.indexOf(track)
                                    onTrackSelect(filteredTracks, idx)
                                },
                                onToggleFavorite = { onToggleFavorite(track.id) }
                            )
                        }
                    }
                }
            }
            LibraryCategory.ALBUMS -> {
                if (filteredAlbums.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(text = "No albums found.", color = NeoWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 140.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        gridItems(
                            items = filteredAlbums,
                            key = { it.title + "_" + it.artist }
                        ) { album ->
                            NeoAlbumCard(album = album)
                        }
                    }
                }
            }
            LibraryCategory.ARTISTS -> {
                if (filteredArtists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(text = "No artists found.", color = NeoWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            filteredArtists,
                            key = { it.name }
                        ) { artist ->
                            NeoArtistRow(artist = artist)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Neo-Brutalist Track Row.
 */
@Composable
fun NeoTrackRow(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val artworkBitmap = remember(track.artworkUri) {
        track.artworkUri?.let { path ->
            try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
        }
    }

    NeoCard(
        backgroundColor = if (isCurrent) NeoYellow else NeoWhite,
        shadowOffset = if (isCurrent) 3.dp else 2.dp,
        cornerRadius = 12.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Artwork Box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(NeoRadiusSm)
                    .background(NeoBlack)
                    .border(2.dp, NeoBlack, NeoRadiusSm),
                contentAlignment = Alignment.Center
            ) {
                if (artworkBitmap != null) {
                    Image(
                        bitmap = artworkBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = NeoYellow,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Title & Artist
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = track.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoBlack.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Visualizer if playing
            if (isCurrent && isPlaying) {
                AetherAudioVisualizer(
                    isPlaying = true
                )
            }

            // Favorite Button
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) NeoPink else NeoBlack,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onToggleFavorite() }
            )
        }
    }
}

/**
 * Neo-Brutalist Album Card.
 */
@Composable
fun NeoAlbumCard(album: Album) {
    val artworkBitmap = remember(album.artworkUri) {
        album.artworkUri?.let { path ->
            try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
        }
    }

    NeoCard(
        backgroundColor = NeoWhite,
        cornerRadius = 14.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(NeoBlack)
                    .border(NeoBorderWidth, NeoBlack, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (artworkBitmap != null) {
                    Image(
                        bitmap = artworkBitmap.asImageBitmap(),
                        contentDescription = album.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = NeoYellow,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = album.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${album.artist} • ${album.trackCount} tracks",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Neo-Brutalist Artist Row.
 */
@Composable
fun NeoArtistRow(artist: Artist) {
    NeoCard(
        backgroundColor = NeoWhite,
        cornerRadius = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = artist.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoBlack
                )
                Text(
                    text = "${artist.albumCount} albums • ${artist.trackCount} tracks",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoMuted
                )
            }
            NeoBadge(
                text = "ARTIST",
                backgroundColor = NeoPurple,
                textColor = NeoWhite
            )
        }
    }
}
