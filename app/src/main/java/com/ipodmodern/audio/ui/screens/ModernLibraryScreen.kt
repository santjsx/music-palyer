package com.ipodmodern.audio.ui.screens

import android.graphics.BitmapFactory
import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.ipodmodern.audio.ui.components.RaycastCard
import com.ipodmodern.audio.ui.components.RaycastPrimaryButton
import com.ipodmodern.audio.ui.components.RaycastSecondaryButton
import com.ipodmodern.audio.ui.theme.AetherCanvas
import com.ipodmodern.audio.ui.theme.AetherCyan
import com.ipodmodern.audio.ui.theme.AetherHairline
import com.ipodmodern.audio.ui.theme.AetherHairlineStrong
import com.ipodmodern.audio.ui.theme.AetherInk
import com.ipodmodern.audio.ui.theme.AetherMute
import com.ipodmodern.audio.ui.theme.AetherPrimaryWhite
import com.ipodmodern.audio.ui.theme.AetherRadiusMd
import com.ipodmodern.audio.ui.theme.AetherRadiusSm
import com.ipodmodern.audio.ui.theme.AetherRose
import com.ipodmodern.audio.ui.theme.AetherSurface
import com.ipodmodern.audio.ui.theme.AetherSurfaceCard
import com.ipodmodern.audio.ui.theme.AetherSurfaceElevated
import java.util.Locale

enum class LibraryCategory {
    SONGS, ALBUMS, ARTISTS, FAVORITES
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
    var selectedCategory by remember { mutableStateOf(LibraryCategory.SONGS) }
    var searchQuery by remember { mutableStateOf("") }

    // Real-time search filtering
    val filteredTracks = remember(tracks, searchQuery, selectedCategory, favoriteTrackIds) {
        val base = if (selectedCategory == LibraryCategory.FAVORITES) {
            tracks.filter { favoriteTrackIds.contains(it.id) }
        } else {
            tracks
        }
        if (searchQuery.isBlank()) {
            base
        } else {
            base.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredAlbums = remember(albums, searchQuery) {
        if (searchQuery.isBlank()) albums
        else albums.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) }
    }

    val filteredArtists = remember(artists, searchQuery) {
        if (searchQuery.isBlank()) artists
        else artists.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(AetherCanvas)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // MARK: - Header Bar with Aether branding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AETHER AUDIO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AetherCyan,
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${tracks.size} Lossless Tracks",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AetherInk,
                    letterSpacing = 0.2.sp
                )
            }

            // Rescan / Refresh Button
            Box(
                modifier = Modifier
                    .clip(AetherRadiusMd)
                    .background(AetherSurfaceElevated)
                    .border(1.dp, AetherHairline, AetherRadiusMd)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onRescanClick()
                    }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = AetherCyan,
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rescan",
                            tint = AetherInk,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = if (isScanning) "SCANNING..." else "RESCAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AetherInk,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MARK: - Unclipped Precision Search Input (BasicTextField)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(AetherRadiusMd)
                .background(AetherSurfaceElevated)
                .border(
                    1.dp,
                    if (searchQuery.isNotEmpty()) AetherCyan else AetherHairline,
                    AetherRadiusMd
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (searchQuery.isNotEmpty()) AetherCyan else AetherMute,
                    modifier = Modifier.size(16.dp)
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search tracks, artists, albums...",
                            color = AetherMute,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        cursorBrush = SolidColor(AetherCyan),
                        textStyle = TextStyle(
                            color = AetherInk,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = AetherInk,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                searchQuery = ""
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MARK: - Pill-Tabs (Songs, Albums, Artists, Favorites)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                LibraryCategory.SONGS to "Songs (${tracks.size})",
                LibraryCategory.ALBUMS to "Albums (${albums.size})",
                LibraryCategory.ARTISTS to "Artists (${artists.size})",
                LibraryCategory.FAVORITES to "Favorites (${favoriteTrackIds.size})"
            ).forEach { (category, title) ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) AetherSurfaceElevated else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) AetherCyan else AetherHairline,
                            CircleShape
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            selectedCategory = category
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AetherPrimaryWhite else AetherMute
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // MARK: - Action Buttons (Shuffle All / Play All)
        if (selectedCategory == LibraryCategory.SONGS || selectedCategory == LibraryCategory.FAVORITES) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RaycastPrimaryButton(
                    text = "Shuffle All",
                    icon = Icons.Default.Shuffle,
                    onClick = {
                        if (filteredTracks.isNotEmpty()) onShuffleAll(filteredTracks)
                    },
                    modifier = Modifier.weight(1f)
                )

                RaycastSecondaryButton(
                    text = "Play All",
                    icon = Icons.Default.PlayArrow,
                    onClick = {
                        if (filteredTracks.isNotEmpty()) onPlayAll(filteredTracks)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // MARK: - Content Views with 120fps Stable Keys
        when (selectedCategory) {
            LibraryCategory.SONGS,
            LibraryCategory.FAVORITES -> {
                if (filteredTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = if (selectedCategory == LibraryCategory.FAVORITES) "No favorite tracks yet." else "No tracks found.",
                            color = AetherMute,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(
                            items = filteredTracks,
                            key = { _, track -> track.id }
                        ) { index, track ->
                            val isCurrentPlaying = activeTrack?.id == track.id
                            CommandPaletteTrackRow(
                                track = track,
                                isPlaying = isCurrentPlaying && isPlaying,
                                isFavorite = favoriteTrackIds.contains(track.id),
                                onClick = { onTrackSelect(filteredTracks, index) },
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
                        Text(text = "No albums found.", color = AetherMute, fontSize = 13.sp)
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
                            RaycastAlbumCard(album = album)
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
                        Text(text = "No artists found.", color = AetherMute, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(
                            filteredArtists,
                            key = { it.name }
                        ) { artist ->
                            RaycastArtistRow(artist = artist)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Aether Single Track Row with instant touch reaction.
 */
@Composable
fun CommandPaletteTrackRow(
    track: Track,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val view = LocalView.current
    val artworkBitmap = remember(track.artworkUri) {
        track.artworkUri?.let { path ->
            try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
        }
    }

    val durationText = remember(track.durationMs) {
        val totalSec = track.durationMs / 1000
        val m = totalSec / 60
        val s = totalSec % 60
        String.format(Locale.US, "%d:%02d", m, s)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AetherRadiusSm)
            .background(if (isPlaying) AetherSurfaceCard else AetherSurface)
            .border(
                1.dp,
                if (isPlaying) AetherCyan else AetherHairline,
                AetherRadiusSm
            )
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 44px Album Art Tile
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(AetherRadiusSm)
                .background(AetherSurfaceElevated)
                .border(1.dp, AetherHairline, AetherRadiusSm),
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
                    tint = AetherMute,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    AetherAudioVisualizer(isPlaying = true)
                }
            }
        }

        // Title & Artist
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = track.title,
                fontSize = 13.sp,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                color = if (isPlaying) AetherCyan else AetherInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${track.artist} · ${track.album}",
                fontSize = 11.sp,
                color = AetherMute,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Duration Keycap
        Text(
            text = durationText,
            fontSize = 11.sp,
            color = AetherMute,
            fontFamily = FontFamily.Monospace
        )

        // Favorite Heart Button
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) AetherRose else AetherMute,
            modifier = Modifier
                .size(18.dp)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onToggleFavorite()
                }
        )
    }
}

/**
 * 2-Column Aether Album Card.
 */
@Composable
fun RaycastAlbumCard(album: Album) {
    val artworkBitmap = remember(album.artworkUri) {
        album.artworkUri?.let { path ->
            try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
        }
    }

    RaycastCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AetherRadiusMd
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.0f)
                    .clip(AetherRadiusSm)
                    .background(AetherSurfaceElevated)
                    .border(1.dp, AetherHairline, AetherRadiusSm),
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
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        tint = AetherMute,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = album.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AetherInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${album.artist} · ${album.trackCount} tracks",
                fontSize = 10.sp,
                color = AetherMute,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Aether Artist Row.
 */
@Composable
fun RaycastArtistRow(artist: Artist) {
    RaycastCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AetherRadiusSm
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AetherSurfaceElevated)
                    .border(1.dp, AetherHairline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AetherInk,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AetherInk
                )
                Text(
                    text = "${artist.albumCount} albums · ${artist.trackCount} tracks",
                    fontSize = 11.sp,
                    color = AetherMute
                )
            }
        }
    }
}
