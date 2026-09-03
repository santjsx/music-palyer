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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
    onOpenSettings: () -> Unit = {},
    initialCategory: LibraryCategory = LibraryCategory.SONGS,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var selectedCategory by remember { mutableStateOf<LibraryCategory?>(null) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    var selectedArtist by remember { mutableStateOf<Artist?>(null) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Intercept hardware/gesture back to pop detail views cleanly
    BackHandler(enabled = selectedFolder != null || selectedGenre != null || selectedAlbum != null || selectedArtist != null || selectedCategory != null || isSearchActive) {
        when {
            selectedFolder != null -> selectedFolder = null
            selectedGenre != null -> selectedGenre = null
            selectedAlbum != null -> selectedAlbum = null
            selectedArtist != null -> selectedArtist = null
            selectedCategory != null -> selectedCategory = null
            isSearchActive -> {
                isSearchActive = false
                searchQuery = ""
            }
        }
    }

    if (selectedFolder != null) {
        val folderName = selectedFolder!!
        val folderTracks = tracks.filter { (File(it.filePath).parentFile?.name ?: "Storage") == folderName }
        GenericTrackListScreen(
            title = folderName,
            subtitle = "Folder",
            tracks = folderTracks,
            activeTrack = activeTrack,
            onBack = { selectedFolder = null },
            onTrackSelect = onTrackSelect
        )
        return
    }

    if (selectedGenre != null) {
        val genreName = selectedGenre!!
        val genreTracks = tracks.filter { it.genre.ifBlank { "Pop / Soundtrack" }.equals(genreName, ignoreCase = true) }
        GenericTrackListScreen(
            title = genreName,
            subtitle = "Genre",
            tracks = genreTracks,
            activeTrack = activeTrack,
            onBack = { selectedGenre = null },
            onTrackSelect = onTrackSelect
        )
        return
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
            onArtistSelect = { selectedArtist = it },
            onFolderSelect = { selectedFolder = it },
            onGenreSelect = { selectedGenre = it }
        )
        return
    }

    // MAIN LIBRARY HOME VIEW
    var activeCategoryTab by remember(initialCategory) { mutableStateOf(initialCategory) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // 1. Top Bar: Back Arrow + Red Checkmark + Profile Avatar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Red Tick / Checkmark Action
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Select",
                    tint = Color(0xFFE50914),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                )

                // User Profile Avatar -> Opens Settings
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF282A30))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onOpenSettings()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile & Settings",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Search Box Pill: "Search this folder"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RadiusFull)
                .background(Color(0xFF16171B))
                .border(1.dp, Color(0x1AFFFFFF), RadiusFull)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF8E8E93),
                    modifier = Modifier.size(18.dp)
                )
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(Color(0xFFE50914)),
                    decorationBox = { inner ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search this folder",
                                color = Color(0xFF636366),
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
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Category Filter Tabs (Songs, Artists, Albums, Folders)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val categories = listOf(
                LibraryCategory.SONGS to "Songs",
                LibraryCategory.ARTISTS to "Artists",
                LibraryCategory.ALBUMS to "Albums",
                LibraryCategory.FOLDERS to "Folders"
            )

            categories.forEach { (cat, title) ->
                val isSelected = activeCategoryTab == cat
                val themeAccent = com.ipodmodern.audio.ui.theme.LocalThemePalette.current.accent
                Column(
                    modifier = Modifier
                        .clip(RadiusFull)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            activeCategoryTab = cat
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = title,
                            tint = if (isSelected) themeAccent else Color(0xFF8E8E93),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = title,
                            color = if (isSelected) themeAccent else Color(0xFF8E8E93),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Dynamic underline indicator
                    Box(
                        modifier = Modifier
                            .width(if (isSelected) 40.dp else 0.dp)
                            .height(2.5.dp)
                            .clip(RadiusFull)
                            .background(if (isSelected) themeAccent else Color.Transparent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Tab Content (Default: Songs list with counter & tracks)
        when (activeCategoryTab) {
            LibraryCategory.SONGS -> {
                val filteredTracks = remember(tracks, searchQuery) {
                    if (searchQuery.isEmpty()) {
                        tracks
                    } else {
                        tracks.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                                    it.artist.contains(searchQuery, ignoreCase = true) ||
                                    it.album.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Song Count Subheader
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${filteredTracks.size} songs",
                                color = Color(0xFF8E8E93),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    items(
                        items = filteredTracks,
                        key = { it.id },
                        contentType = { "track_row" }
                    ) { track ->
                        ModernTrackRow(
                            track = track,
                            isCurrent = track.id == activeTrack?.id,
                            isPlaying = isPlaying,
                            onClick = { onTrackSelect(track) }
                        )
                    }
                }
            }
            LibraryCategory.ARTISTS -> {
                val filteredArtists = remember(artists, searchQuery) {
                    if (searchQuery.isEmpty()) artists else artists.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = filteredArtists,
                        key = { it.id },
                        contentType = { "artist_row" }
                    ) { artist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RadiusLg)
                                .clickable { selectedArtist = artist }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1F24)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFE50914), modifier = Modifier.size(24.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = artist.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${artist.trackCount} tracks", color = Color(0xFF8E8E93), fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF636366), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            LibraryCategory.ALBUMS -> {
                val filteredAlbums = remember(albums, searchQuery) {
                    if (searchQuery.isEmpty()) albums else albums.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 180.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredAlbums,
                        key = { it.id },
                        contentType = { "album_card" }
                    ) { album ->
                        val context = LocalContext.current
                        val albumArt = remember(album.artworkUri) { album.artworkUri?.let { File(it) } }
                        val albumArtRequest = remember(albumArt) {
                            albumArt?.let {
                                ImageRequest.Builder(context)
                                    .data(it)
                                    .size(360)
                                    .crossfade(true)
                                    .build()
                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RadiusLg)
                                .background(Color(0xFF121316))
                                .clickable { selectedAlbum = album }
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RadiusMd)
                                    .background(Color(0xFF1C1D22)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (albumArtRequest != null) {
                                    AsyncImage(model = albumArtRequest, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                } else {
                                    Icon(Icons.Default.Album, contentDescription = null, tint = Color(0xFFE50914), modifier = Modifier.size(36.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = album.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(text = album.artist, color = Color(0xFF8E8E93), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            LibraryCategory.FOLDERS -> {
                val folders = tracks.groupBy { File(it.filePath).parentFile?.name ?: "Storage" }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(folders.keys.toList()) { folderName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RadiusLg)
                                .clickable { selectedFolder = folderName }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RadiusMd)
                                    .background(Color(0xFF1E1F24)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFE50914), modifier = Modifier.size(24.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = folderName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${folders[folderName]?.size ?: 0} tracks", color = Color(0xFF8E8E93), fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF636366), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ModernTrackRow(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    val view = LocalView.current
    val context = LocalContext.current
    val artworkFile = remember(track.artworkUri) {
        track.artworkUri?.let { File(it) }
    }
    val artworkRequest = remember(artworkFile) {
        artworkFile?.let {
            ImageRequest.Builder(context)
                .data(it)
                .size(144)
                .crossfade(true)
                .build()
        }
    }

    val durMin = (track.durationMs / 1000) / 60
    val durSec = (track.durationMs / 1000) % 60
    val durationText = String.format("%d:%02d", durMin, durSec)

    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RadiusMd)
            .background(if (isCurrent) Color(0xFF1C1417) else Color.Transparent)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Artwork Thumbnail (Rounded square)
        val rowAccent = com.ipodmodern.audio.ui.theme.LocalThemePalette.current.accent
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RadiusMd)
                .background(Color(0xFF1C1D22)),
            contentAlignment = Alignment.Center
        ) {
            if (artworkRequest != null) {
                AsyncImage(
                    model = artworkRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = rowAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Title & Artist Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.title,
                color = if (isCurrent) rowAccent else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist.ifBlank { "Unknown Artist" },
                color = Color(0xFF8E8E93),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Duration (e.g. 6:48)
        Text(
            text = durationText,
            color = Color(0xFF8E8E93),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        )

        // 3-Dots Vertical Action Menu
        Box {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = Color(0xFF8E8E93),
                modifier = Modifier
                    .size(22.dp)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        showMenu = true
                    }
            )
        }
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
    onArtistSelect: (Artist) -> Unit,
    onFolderSelect: (String) -> Unit,
    onGenreSelect: (String) -> Unit
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
                    items(items = tracks, key = { it.id }) { track ->
                        ModernTrackRow(
                            track = track,
                            isCurrent = track.id == activeTrack?.id,
                            onClick = { onTrackSelect(track) }
                        )
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
                    items(items = albums, key = { it.id }) { album ->
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
                                    .aspectRatio(1f)
                                    .clip(RadiusLg)
                                    .background(ObsidianSurface)
                                    .border(1.dp, ObsidianBorder, RadiusLg),
                                contentAlignment = Alignment.Center
                            ) {
                                if (albumArt != null) {
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
                    items(items = artists, key = { it.name }) { artist ->
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
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
            LibraryCategory.FOLDERS -> {
                val folderGroups = tracks.groupBy { File(it.filePath).parentFile?.name ?: "Storage" }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(items = folderGroups.entries.toList(), key = { it.key }) { (folderName, folderTracks) ->
                        SleekCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = ObsidianSurface,
                            shape = RadiusLg,
                            onClick = { onFolderSelect(folderName) }
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
                                        .clip(RadiusMd)
                                        .background(ObsidianElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = MintAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = folderName,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${folderTracks.size} Songs",
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
            LibraryCategory.GENRES -> {
                val genreGroups = tracks.groupBy { it.genre.ifBlank { "Pop / Soundtrack" } }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(items = genreGroups.entries.toList(), key = { it.key }) { (genreName, genreTracks) ->
                        SleekCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = ObsidianSurface,
                            shape = RadiusLg,
                            onClick = { onGenreSelect(genreName) }
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
                                        .clip(RadiusMd)
                                        .background(ObsidianElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = MintAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = genreName,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${genreTracks.size} Songs",
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
        }
    }
}

@Composable
fun GenericTrackListScreen(
    title: String,
    subtitle: String,
    tracks: List<Track>,
    activeTrack: Track?,
    onBack: () -> Unit,
    onTrackSelect: (Track) -> Unit
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

            Column {
                Text(
                    text = subtitle.uppercase(),
                    color = MintAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            items(items = tracks, key = { it.id }) { track ->
                ModernTrackRow(
                    track = track,
                    isCurrent = track.id == activeTrack?.id,
                    onClick = { onTrackSelect(track) }
                )
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
                            .aspectRatio(1f)
                            .clip(RadiusXl)
                            .background(ObsidianSurface)
                            .border(1.dp, ObsidianBorder, RadiusXl),
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
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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

            items(items = tracks, key = { it.id }) { track ->
                ModernTrackRow(
                    track = track,
                    isCurrent = track.id == activeTrack?.id,
                    onClick = { onTrackSelect(track) }
                )
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

            items(items = tracks, key = { it.id }) { track ->
                ModernTrackRow(
                    track = track,
                    isCurrent = track.id == activeTrack?.id,
                    onClick = { onTrackSelect(track) }
                )
            }
        }
    }
}
