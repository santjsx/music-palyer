package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
import com.ipodmodern.audio.ui.theme.NeoBorderThick
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
import java.io.File
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

    // Drill-down states for Album and Artist Detail Views
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }

    // Handle system back navigation if inside Album or Artist detail
    BackHandler(enabled = selectedAlbum != null || selectedArtist != null) {
        if (selectedAlbum != null) selectedAlbum = null
        else if (selectedArtist != null) selectedArtist = null
    }

    if (selectedAlbum != null) {
        val album = selectedAlbum!!
        val albumTracks = remember(album, tracks) {
            tracks.filter { it.album.equals(album.title, ignoreCase = true) }
        }

        AlbumDetailScreen(
            album = album,
            albumTracks = albumTracks,
            activeTrack = activeTrack,
            isPlaying = isPlaying,
            favoriteTrackIds = favoriteTrackIds,
            onBackClick = { selectedAlbum = null },
            onTrackSelect = { idx -> onTrackSelect(albumTracks, idx) },
            onPlayAll = { onPlayAll(albumTracks) },
            onShuffleAll = { onShuffleAll(albumTracks) },
            onToggleFavorite = onToggleFavorite,
            modifier = modifier
        )
        return
    }

    if (selectedArtist != null) {
        val artist = selectedArtist!!
        val artistTracks = remember(artist, tracks) {
            tracks.filter { it.artist.equals(artist.name, ignoreCase = true) }
        }

        ArtistDetailScreen(
            artist = artist,
            artistTracks = artistTracks,
            activeTrack = activeTrack,
            isPlaying = isPlaying,
            favoriteTrackIds = favoriteTrackIds,
            onBackClick = { selectedArtist = null },
            onTrackSelect = { idx -> onTrackSelect(artistTracks, idx) },
            onPlayAll = { onPlayAll(artistTracks) },
            onShuffleAll = { onShuffleAll(artistTracks) },
            onToggleFavorite = onToggleFavorite,
            modifier = modifier
        )
        return
    }

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
                    text = "LOSSLESS MUSIC VAULT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = NeoWhite.copy(alpha = 0.8f)
                )
            }

            // Rescan / Scan Indicator
            NeoIconButton(
                icon = Icons.Default.Refresh,
                contentDescription = "Rescan",
                onClick = onRescanClick,
                backgroundColor = if (isScanning) NeoGreen else NeoWhite,
                size = 42.dp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar (High contrast Neo-Brutalist)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .neoShadow(offsetX = 3.dp, offsetY = 3.dp, color = NeoBlack, cornerRadius = 12.dp)
                .background(NeoWhite, NeoRadiusMd)
                .border(NeoBorderWidth, NeoBlack, NeoRadiusMd)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = NeoBlack,
                    modifier = Modifier.size(20.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search tracks, albums, artists...",
                            color = NeoMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = NeoBlack,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(NeoBlack),
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

        // Category Pills (Neo-Brutalist Segments)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LibraryCategory.entries.forEach { category ->
                val isSelected = selectedCategory == category
                NeoButton(
                    text = category.title.uppercase(),
                    backgroundColor = if (isSelected) NeoYellow else NeoWhite,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        selectedCategory = category
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Deck (Play All & Shuffle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NeoButton(
                text = "PLAY ALL",
                icon = Icons.Default.PlayArrow,
                backgroundColor = NeoGreen,
                textColor = NeoWhite,
                onClick = {
                    if (filteredTracks.isNotEmpty()) onPlayAll(filteredTracks)
                },
                modifier = Modifier.weight(1f)
            )

            NeoButton(
                text = "SHUFFLE",
                icon = Icons.Default.Shuffle,
                backgroundColor = NeoPurple,
                textColor = NeoWhite,
                onClick = {
                    if (filteredTracks.isNotEmpty()) onShuffleAll(filteredTracks)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area (Songs, Albums, Artists)
        Box(modifier = Modifier.weight(1f)) {
            when (selectedCategory) {
                LibraryCategory.SONGS -> {
                    if (filteredTracks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(text = "No songs found.", color = NeoWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 140.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(
                                items = filteredTracks,
                                key = { _, track -> track.id }
                            ) { index, track ->
                                val isCurrent = activeTrack?.id == track.id
                                NeoTrackRow(
                                    track = track,
                                    isCurrent = isCurrent,
                                    isPlaying = isCurrent && isPlaying,
                                    isFavorite = favoriteTrackIds.contains(track.id),
                                    onClick = {
                                        onTrackSelect(filteredTracks, index)
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
                            items(
                                items = filteredAlbums,
                                key = { it.title + "_" + it.artist }
                            ) { album ->
                                NeoAlbumCard(
                                    album = album,
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        selectedAlbum = album
                                    }
                                )
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
                                items = filteredArtists,
                                key = { it.name }
                            ) { artist ->
                                NeoArtistRow(
                                    artist = artist,
                                    onClick = {
                                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        selectedArtist = artist
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Rich Neo-Brutalist Album Detail View.
 */
@Composable
fun AlbumDetailScreen(
    album: Album,
    albumTracks: List<Track>,
    activeTrack: Track?,
    isPlaying: Boolean,
    favoriteTrackIds: Set<Long>,
    onBackClick: () -> Unit,
    onTrackSelect: (Int) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val artworkFile = remember(album.artworkUri) {
        album.artworkUri?.let { File(it) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(NeoBgDark)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeoIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBackClick,
                backgroundColor = NeoYellow,
                size = 42.dp
            )

            NeoBadge(
                text = "ALBUM",
                backgroundColor = NeoPink,
                textColor = NeoWhite
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Album Header Card
        NeoCard(
            backgroundColor = NeoWhite,
            cornerRadius = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Artwork Box
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(NeoRadiusMd)
                        .background(NeoBlack)
                        .border(NeoBorderWidth, NeoBlack, NeoRadiusMd),
                    contentAlignment = Alignment.Center
                ) {
                    if (artworkFile != null && artworkFile.exists()) {
                        AsyncImage(
                            model = artworkFile,
                            contentDescription = album.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = NeoYellow,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = album.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = album.artist,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoBlack.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${albumTracks.size} tracks",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Play All & Shuffle Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NeoButton(
                text = "PLAY ALL",
                icon = Icons.Default.PlayArrow,
                backgroundColor = NeoGreen,
                textColor = NeoWhite,
                onClick = onPlayAll,
                modifier = Modifier.weight(1f)
            )

            NeoButton(
                text = "SHUFFLE",
                icon = Icons.Default.Shuffle,
                backgroundColor = NeoPurple,
                textColor = NeoWhite,
                onClick = onShuffleAll,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tracks in this album
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = albumTracks,
                key = { _, track -> track.id }
            ) { index, track ->
                val isCurrent = activeTrack?.id == track.id
                NeoTrackRow(
                    track = track,
                    isCurrent = isCurrent,
                    isPlaying = isCurrent && isPlaying,
                    isFavorite = favoriteTrackIds.contains(track.id),
                    onClick = { onTrackSelect(index) },
                    onToggleFavorite = { onToggleFavorite(track.id) }
                )
            }
        }
    }
}

/**
 * Rich Neo-Brutalist Artist Detail View.
 */
@Composable
fun ArtistDetailScreen(
    artist: Artist,
    artistTracks: List<Track>,
    activeTrack: Track?,
    isPlaying: Boolean,
    favoriteTrackIds: Set<Long>,
    onBackClick: () -> Unit,
    onTrackSelect: (Int) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(NeoBgDark)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeoIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBackClick,
                backgroundColor = NeoYellow,
                size = 42.dp
            )

            NeoBadge(
                text = "ARTIST",
                backgroundColor = NeoPurple,
                textColor = NeoWhite
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Artist Header Card
        NeoCard(
            backgroundColor = NeoWhite,
            cornerRadius = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(NeoYellow)
                        .border(NeoBorderWidth, NeoBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NeoBlack,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = artist.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${artist.albumCount} albums • ${artistTracks.size} songs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Play All & Shuffle Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NeoButton(
                text = "PLAY ALL",
                icon = Icons.Default.PlayArrow,
                backgroundColor = NeoGreen,
                textColor = NeoWhite,
                onClick = onPlayAll,
                modifier = Modifier.weight(1f)
            )

            NeoButton(
                text = "SHUFFLE",
                icon = Icons.Default.Shuffle,
                backgroundColor = NeoPurple,
                textColor = NeoWhite,
                onClick = onShuffleAll,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tracks by this artist
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = artistTracks,
                key = { _, track -> track.id }
            ) { index, track ->
                val isCurrent = activeTrack?.id == track.id
                NeoTrackRow(
                    track = track,
                    isCurrent = isCurrent,
                    isPlaying = isCurrent && isPlaying,
                    isFavorite = favoriteTrackIds.contains(track.id),
                    onClick = { onTrackSelect(index) },
                    onToggleFavorite = { onToggleFavorite(track.id) }
                )
            }
        }
    }
}

/**
 * 120fps Async-Image Neo-Brutalist Track Row.
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
    val artworkFile = remember(track.artworkUri) {
        track.artworkUri?.let { File(it) }
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
            // Artwork Box (120fps AsyncImage with Coil)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(NeoRadiusSm)
                    .background(NeoBlack)
                    .border(2.dp, NeoBlack, NeoRadiusSm),
                contentAlignment = Alignment.Center
            ) {
                if (artworkFile != null && artworkFile.exists()) {
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
                        tint = NeoYellow,
                        modifier = Modifier.size(22.dp)
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
 * 120fps Async-Image Neo-Brutalist Album Card.
 */
@Composable
fun NeoAlbumCard(
    album: Album,
    onClick: () -> Unit = {}
) {
    val artworkFile = remember(album.artworkUri) {
        album.artworkUri?.let { File(it) }
    }

    NeoCard(
        backgroundColor = NeoWhite,
        cornerRadius = 14.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
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
                if (artworkFile != null && artworkFile.exists()) {
                    AsyncImage(
                        model = artworkFile,
                        contentDescription = album.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        tint = NeoYellow,
                        modifier = Modifier.size(44.dp)
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
fun NeoArtistRow(
    artist: Artist,
    onClick: () -> Unit = {}
) {
    NeoCard(
        backgroundColor = NeoWhite,
        cornerRadius = 12.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
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
                text = "VIEW ARTIST",
                backgroundColor = NeoPurple,
                textColor = NeoWhite
            )
        }
    }
}
