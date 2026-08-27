package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.Album
import com.ipodmodern.audio.core.model.Artist
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.SleekCard
import com.ipodmodern.audio.ui.components.SleekIconButton
import com.ipodmodern.audio.ui.theme.MintAccent
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.theme.ObsidianBorder
import com.ipodmodern.audio.ui.theme.ObsidianElevated
import com.ipodmodern.audio.ui.theme.ObsidianPill
import com.ipodmodern.audio.ui.theme.ObsidianSurface
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusMd
import com.ipodmodern.audio.ui.theme.RadiusSm
import com.ipodmodern.audio.ui.theme.RadiusXl
import com.ipodmodern.audio.ui.theme.TextMuted
import com.ipodmodern.audio.ui.theme.TextPrimary
import com.ipodmodern.audio.ui.theme.TextSecondary
import java.io.File

enum class LibraryCategory(val title: String, val icon: ImageVector) {
    SONGS("Songs", Icons.Default.MusicNote),
    ALBUMS("Albums", Icons.Default.Album),
    ARTISTS("Artists", Icons.Default.Person),
    GENRES("Genres", Icons.Default.GraphicEq),
    FOLDERS("Folders", Icons.Default.Folder)
}

@Composable
fun ModernLibraryScreen(
    tracks: List<Track>,
    albums: List<Album>,
    artists: List<Artist>,
    activeTrack: Track?,
    isPlaying: Boolean,
    onTrackSelect: (Track) -> Unit,
    onShuffleAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var selectedCategory by remember { mutableStateOf<LibraryCategory?>(null) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Intercept hardware/gesture back to pop detail views
    BackHandler(enabled = selectedAlbum != null || selectedArtist != null || selectedCategory != null || isSearchActive) {
        when {
            selectedAlbum != null -> selectedAlbum = null
            selectedArtist != null -> selectedArtist = null
            selectedCategory != null -> selectedCategory = null
            isSearchActive -> {
                isSearchActive = false
                searchQuery = ""
            }
        }
    }

    if (selectedAlbum != null) {
        val album = selectedAlbum!!
        AlbumDetailScreen(
            album = album,
            tracks = tracks.filter { it.album.equals(album.title, ignoreCase = true) },
            activeTrack = activeTrack,
            isPlaying = isPlaying,
            onBack = { selectedAlbum = null },
            onTrackSelect = onTrackSelect,
            onShuffleAlbum = {
                val albumTracks = tracks.filter { it.album.equals(album.title, ignoreCase = true) }
                if (albumTracks.isNotEmpty()) onTrackSelect(albumTracks.random())
            }
        )
        return
    }

    if (selectedArtist != null) {
        val artist = selectedArtist!!
        ArtistDetailScreen(
            artist = artist,
            tracks = tracks.filter { it.artist.equals(artist.name, ignoreCase = true) },
            activeTrack = activeTrack,
            isPlaying = isPlaying,
            onBack = { selectedArtist = null },
            onTrackSelect = onTrackSelect,
            onShuffleArtist = {
                val artistTracks = tracks.filter { it.artist.equals(artist.name, ignoreCase = true) }
                if (artistTracks.isNotEmpty()) onTrackSelect(artistTracks.random())
            }
        )
        return
    }

    if (selectedCategory != null) {
        CategorySubScreen(
            category = selectedCategory!!,
            tracks = tracks,
            albums = albums,
            artists = artists,
            activeTrack = activeTrack,
            isPlaying = isPlaying,
            onBack = { selectedCategory = null },
            onTrackSelect = onTrackSelect,
            onAlbumSelect = { selectedAlbum = it },
            onArtistSelect = { selectedArtist = it }
        )
        return
    }

    // MAIN LIBRARY HOME VIEW
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // 1. Top Bar: "Library" Title + Search + 3-Dots
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Library",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SleekIconButton(
                        icon = Icons.Default.Search,
                        onClick = { isSearchActive = !isSearchActive },
                        size = 38.dp,
                        iconSize = 20.dp,
                        contentDescription = "Search"
                    )

                    SleekIconButton(
                        icon = Icons.Default.MoreVert,
                        onClick = {},
                        size = 38.dp,
                        iconSize = 20.dp,
                        contentDescription = "Options"
                    )
                }
            }
        }

        // Search Bar Dropdown
        if (isSearchActive) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RadiusFull)
                        .background(ObsidianPill)
                        .border(1.dp, ObsidianBorder, RadiusFull)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                            cursorBrush = SolidColor(MintAccent),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search tracks, artists, albums...",
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                                inner()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }
                }
            }
        }

        // 2. Category List Rows (Songs, Albums, Artists, Genres, Folders)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RadiusXl)
                    .background(ObsidianSurface)
                    .border(1.dp, ObsidianBorder, RadiusXl)
                    .padding(vertical = 4.dp)
            ) {
                LibraryCategoryRow(
                    icon = Icons.Default.MusicNote,
                    title = "Songs",
                    count = tracks.size,
                    onClick = { selectedCategory = LibraryCategory.SONGS }
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                LibraryCategoryRow(
                    icon = Icons.Default.Album,
                    title = "Albums",
                    count = albums.size,
                    onClick = { selectedCategory = LibraryCategory.ALBUMS }
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                LibraryCategoryRow(
                    icon = Icons.Default.Person,
                    title = "Artists",
                    count = artists.size,
                    onClick = { selectedCategory = LibraryCategory.ARTISTS }
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                LibraryCategoryRow(
                    icon = Icons.Default.GraphicEq,
                    title = "Genres",
                    count = 12,
                    onClick = { selectedCategory = LibraryCategory.GENRES }
                )

                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(1.dp).background(ObsidianBorder))

                LibraryCategoryRow(
                    icon = Icons.Default.Folder,
                    title = "Folders",
                    count = 4,
                    onClick = { selectedCategory = LibraryCategory.FOLDERS }
                )
            }
        }

        // 3. Recently Added Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recently Added",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "See all",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { selectedCategory = LibraryCategory.SONGS }
                )
            }
        }

        // Track items
        val filteredTracks = if (searchQuery.isEmpty()) {
            tracks.take(15)
        } else {
            tracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }

        items(filteredTracks) { track ->
            val isCurrent = track.id == activeTrack?.id
            val artworkFile = remember(track.artworkUri) {
                track.artworkUri?.let { File(it) }
            }

            SleekCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (isCurrent) ObsidianElevated else ObsidianSurface,
                shape = RadiusLg,
                onClick = { onTrackSelect(track) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Artwork Thumbnail
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RadiusMd)
                            .background(ObsidianElevated),
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
                                tint = MintAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Metadata
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = if (isCurrent) MintAccent else TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Play Trigger
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent) MintAccent else ObsidianElevated)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onTrackSelect(track)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = if (isCurrent) ObsidianBg else TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 3-dots
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryCategoryRow(
    icon: ImageVector,
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    val view = LocalView.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MintAccent,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = title,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = count.toString(),
            color = TextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun CategorySubScreen(
    category: LibraryCategory,
    tracks: List<Track>,
    albums: List<Album>,
    artists: List<Artist>,
    activeTrack: Track?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onTrackSelect: (Track) -> Unit,
    onAlbumSelect: (Album) -> Unit,
    onArtistSelect: (Artist) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SleekIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onBack,
                size = 38.dp,
                iconSize = 20.dp,
                contentDescription = "Back"
            )

            Text(
                text = category.title,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        when (category) {
            LibraryCategory.SONGS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(tracks) { track ->
                        val isCurrent = track.id == activeTrack?.id
                        SleekCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = if (isCurrent) ObsidianElevated else ObsidianSurface,
                            shape = RadiusLg,
                            onClick = { onTrackSelect(track) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        color = if (isCurrent) MintAccent else TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${track.artist} • ${track.album}",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = if (isCurrent) MintAccent else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            LibraryCategory.ALBUMS -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(albums) { album ->
                        val albumArt = remember(album.artworkUri) {
                            album.artworkUri?.let { File(it) }
                        }

                        Column(
                            modifier = Modifier
                                .clip(RadiusLg)
                                .clickable { onAlbumSelect(album) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RadiusLg)
                                    .background(ObsidianSurface)
                                    .border(1.dp, ObsidianBorder, RadiusLg),
                                contentAlignment = Alignment.Center
                            ) {
                                if (albumArt != null && albumArt.exists()) {
                                    AsyncImage(
                                        model = albumArt,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Album,
                                        contentDescription = null,
                                        tint = MintAccent,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = album.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = album.artist,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            LibraryCategory.ARTISTS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(artists) { artist ->
                        SleekCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = ObsidianSurface,
                            shape = RadiusLg,
                            onClick = { onArtistSelect(artist) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(ObsidianElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MintAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = artist.name,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${artist.trackCount} Songs",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(tracks) { track ->
                        SleekCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = ObsidianSurface,
                            shape = RadiusLg,
                            onClick = { onTrackSelect(track) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = track.title,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = track.artist,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumDetailScreen(
    album: Album,
    tracks: List<Track>,
    activeTrack: Track?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onTrackSelect: (Track) -> Unit,
    onShuffleAlbum: () -> Unit
) {
    val artworkFile = remember(album.artworkUri) {
        album.artworkUri?.let { File(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SleekIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onBack,
                size = 38.dp,
                iconSize = 20.dp,
                contentDescription = "Back"
            )

            Text(
                text = "Album",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RadiusXl)
                            .background(ObsidianSurface)
                            .border(1.dp, ObsidianBorder, RadiusXl),
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
                                imageVector = Icons.Default.Album,
                                contentDescription = null,
                                tint = MintAccent,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = album.title,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = album.artist,
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${tracks.size} tracks",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RadiusFull)
                            .background(MintAccent)
                            .clickable {
                                if (tracks.isNotEmpty()) onTrackSelect(tracks[0])
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(18.dp))
                            Text(text = "Play All", color = ObsidianBg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RadiusFull)
                            .background(ObsidianElevated)
                            .border(1.dp, ObsidianBorder, RadiusFull)
                            .clickable { onShuffleAlbum() }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Text(text = "Shuffle", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(tracks) { track ->
                val isCurrent = track.id == activeTrack?.id
                SleekCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (isCurrent) ObsidianElevated else ObsidianSurface,
                    shape = RadiusLg,
                    onClick = { onTrackSelect(track) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = track.title,
                            color = if (isCurrent) MintAccent else TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = if (isCurrent) MintAccent else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistDetailScreen(
    artist: Artist,
    tracks: List<Track>,
    activeTrack: Track?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onTrackSelect: (Track) -> Unit,
    onShuffleArtist: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SleekIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onBack,
                size = 38.dp,
                iconSize = 20.dp,
                contentDescription = "Back"
            )

            Text(
                text = "Artist",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(ObsidianSurface)
                            .border(1.dp, ObsidianBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MintAccent,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = artist.name,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${tracks.size} tracks",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RadiusFull)
                            .background(MintAccent)
                            .clickable {
                                if (tracks.isNotEmpty()) onTrackSelect(tracks[0])
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ObsidianBg, modifier = Modifier.size(18.dp))
                            Text(text = "Play All", color = ObsidianBg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RadiusFull)
                            .background(ObsidianElevated)
                            .border(1.dp, ObsidianBorder, RadiusFull)
                            .clickable { onShuffleArtist() }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Text(text = "Shuffle", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(tracks) { track ->
                val isCurrent = track.id == activeTrack?.id
                SleekCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (isCurrent) ObsidianElevated else ObsidianSurface,
                    shape = RadiusLg,
                    onClick = { onTrackSelect(track) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = track.title,
                            color = if (isCurrent) MintAccent else TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = if (isCurrent) MintAccent else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
