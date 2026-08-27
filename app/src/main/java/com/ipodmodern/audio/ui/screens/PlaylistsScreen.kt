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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.ipodmodern.audio.ui.theme.RadiusXl
import com.ipodmodern.audio.ui.theme.TextMuted
import com.ipodmodern.audio.ui.theme.TextPrimary
import com.ipodmodern.audio.ui.theme.TextSecondary
import com.ipodmodern.audio.ui.viewmodel.PlayerViewModel

data class CustomPlaylist(
    val id: String,
    val title: String,
    val songCount: Int,
    val color: Color
)

val DEFAULT_PLAYLISTS = listOf(
    CustomPlaylist("1", "Chill Vibes", 24, Color(0xFFE57373)),
    CustomPlaylist("2", "Workout Mix", 35, Color(0xFFFFB74D)),
    CustomPlaylist("3", "Feel Good", 42, Color(0xFFF06292)),
    CustomPlaylist("4", "Night Drive", 18, Color(0xFF64B5F6)),
    CustomPlaylist("5", "Acoustic Sessions", 29, Color(0xFF81C784)),
    CustomPlaylist("6", "Focus Flow", 31, Color(0xFFBA68C8))
)

@Composable
fun PlaylistsScreen(
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var playlists by remember { mutableStateOf(DEFAULT_PLAYLISTS) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // 1. Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playlists",
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
                        icon = Icons.Default.Add,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            val newId = (playlists.size + 1).toString()
                            playlists = playlists + CustomPlaylist(newId, "New Playlist $newId", 0, MintAccent)
                        },
                        size = 38.dp,
                        iconSize = 22.dp,
                        contentDescription = "Create Playlist"
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
                                        text = "Search playlists...",
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

        // 2. Playlists List Cards
        val filtered = playlists.filter { it.title.contains(searchQuery, ignoreCase = true) }

        items(filtered) { playlist ->
            SleekCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = ObsidianSurface,
                shape = RadiusLg,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    // Play random track from library
                    val songs = playerViewModel.uiState.value.allTracks
                    if (songs.isNotEmpty()) playerViewModel.playTrack(songs.random())
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Custom Mosaic / Gradient Thumbnail
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RadiusMd)
                            .background(playlist.color.copy(alpha = 0.25f))
                            .border(1.dp, playlist.color.copy(alpha = 0.5f), RadiusMd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FeaturedPlayList,
                            contentDescription = null,
                            tint = playlist.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Metadata
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = playlist.title,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${playlist.songCount} Songs",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Play Button Circle
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ObsidianElevated)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                val songs = playerViewModel.uiState.value.allTracks
                                if (songs.isNotEmpty()) playerViewModel.playTrack(songs.random())
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = MintAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 3-dots Menu
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
