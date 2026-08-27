package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.components.SleekCard
import com.ipodmodern.audio.ui.theme.ModernThemeTokens
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.theme.ObsidianElevated
import com.ipodmodern.audio.ui.theme.ObsidianSurface
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusMd
import com.ipodmodern.audio.ui.theme.TextMuted
import com.ipodmodern.audio.ui.theme.TextPrimary
import com.ipodmodern.audio.ui.theme.TextSecondary
import com.ipodmodern.audio.ui.viewmodel.PlayerViewModel
import java.io.File

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    playerViewModel: PlayerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playlists by playerViewModel.playlists.collectAsState()
    val playerState by playerViewModel.uiState.collectAsState()
    val palette = ModernThemeTokens.palette
    val view = LocalView.current

    val playlistWithTracks = remember(playlists, playlistId) {
        playlists.find { it.playlist.id == playlistId }
    }

    if (playlistWithTracks == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(palette.bg)
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Playlist not found", color = TextSecondary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { onBackClick() }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("Go Back", color = Color.White)
                }
            }
        }
        return
    }

    val playlist = playlistWithTracks.playlist
    val tracks = remember(playlistWithTracks) { playlistWithTracks.toDomainTracks() }
    val themeColor = Color(playlist.colorHex)

    var showMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Top Navigation Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onBackClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = 2.dp)
                    )
                }

                Text(
                    text = playlist.name,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false).padding(horizontal = 12.dp)
                )

                Box {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                            .clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(ObsidianElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Playlist", color = Color(0xFFFF5252)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                            onClick = {
                                showMenu = false
                                playerViewModel.deletePlaylist(playlist.id)
                                onBackClick()
                            }
                        )
                    }
                }
            }
        }

        // 2. Hero Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                themeColor.copy(alpha = 0.85f),
                                themeColor.copy(alpha = 0.40f),
                                ObsidianSurface
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(26.dp))
                    .padding(22.dp)
            ) {
                Column {
                    if (playlist.isAiGenerated) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.20f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "AI SMART CLUSTER",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text(
                        text = playlist.name,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (playlist.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = playlist.description,
                            color = Color.White.copy(alpha = 0.80f),
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${tracks.size} Tracks • High-Res DSP Engine",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Play & Shuffle Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play All Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.playPlaylist(playlistWithTracks)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Play All",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Shuffle Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape)
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    if (tracks.isNotEmpty()) {
                                        playerViewModel.playTrack(tracks.random(), tracks)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Shuffle",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Tracks List
        if (tracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs in this playlist yet.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                val isCurrentPlaying = playerState.currentTrack?.id == track.id
                val artFile = remember(track.artworkUri) { track.artworkUri?.let { File(it) } }

                SleekCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (isCurrentPlaying) themeColor.copy(alpha = 0.15f) else ObsidianSurface,
                    shape = RadiusLg,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        playerViewModel.playPlaylist(playlistWithTracks, startTrack = track)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Artwork Thumbnail
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RadiusMd)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            if (artFile != null && artFile.exists()) {
                                AsyncImage(
                                    model = artFile,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = palette.accent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Title & Artist
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                color = if (isCurrentPlaying) palette.accent else TextPrimary,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = track.artist,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Remove Track Action
                        var itemMenuOpen by remember { mutableStateOf(false) }
                        Box {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = TextMuted,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { itemMenuOpen = true }
                            )

                            DropdownMenu(
                                expanded = itemMenuOpen,
                                onDismissRequest = { itemMenuOpen = false },
                                modifier = Modifier.background(ObsidianElevated)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Remove from Playlist", color = Color(0xFFFF5252)) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                                    onClick = {
                                        itemMenuOpen = false
                                        playerViewModel.removeTrackFromPlaylist(playlist.id, track.id)
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
