package com.ipodmodern.audio.ui.screens

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.components.SleekCard
import com.ipodmodern.audio.ui.components.SleekIconButton
import com.ipodmodern.audio.ui.theme.ModernThemeTokens
import com.ipodmodern.audio.ui.theme.ObsidianBg
import com.ipodmodern.audio.ui.theme.ObsidianBorder
import com.ipodmodern.audio.ui.theme.ObsidianElevated
import com.ipodmodern.audio.ui.theme.ObsidianPill
import com.ipodmodern.audio.ui.theme.ObsidianSurface
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusMd
import com.ipodmodern.audio.ui.theme.TextMuted
import com.ipodmodern.audio.ui.theme.TextPrimary
import com.ipodmodern.audio.ui.theme.TextSecondary
import com.ipodmodern.audio.ui.viewmodel.PlayerViewModel

private val PRESET_COLORS = listOf(
    0xFF256BFE, // Royal Blue
    0xFFE65100, // Telugu Orange
    0xFF8E24AA, // Tamil Purple
    0xFFD81B60, // Bollywood Rose
    0xFFF57F17, // Punjabi Amber
    0xFF00897B, // Malayalam Teal
    0xFF3949AB, // Kannada Indigo
    0xFF43A047, // Workout Green
    0xFF7AE898  // Mint
)

@Composable
fun PlaylistsScreen(
    playerViewModel: PlayerViewModel,
    onPlaylistClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val playlists by playerViewModel.playlists.collectAsState()
    val palette = ModernThemeTokens.palette

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "AI", "Custom"
    var showCreateDialog by remember { mutableStateOf(false) }
    var isGeneratingAi by remember { mutableStateOf(false) }

    // Filter Logic
    val filteredPlaylists = remember(playlists, searchQuery, selectedFilter) {
        playlists.filter { item ->
            val matchesFilter = when (selectedFilter) {
                "AI" -> item.playlist.isAiGenerated
                "Custom" -> !item.playlist.isAiGenerated
                else -> true
            }
            val matchesSearch = item.playlist.name.contains(searchQuery, ignoreCase = true) ||
                    item.playlist.description.contains(searchQuery, ignoreCase = true)
            matchesFilter && (searchQuery.isBlank() || matchesSearch)
        }
    }

    val aiCount = remember(playlists) { playlists.count { it.playlist.isAiGenerated } }
    val customCount = remember(playlists) { playlists.count { !it.playlist.isAiGenerated } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(palette.bg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
    ) {
        // 1. Top Header Bar
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
                        size = 40.dp,
                        iconSize = 20.dp,
                        contentDescription = "Search"
                    )

                    SleekIconButton(
                        icon = Icons.Default.Add,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            showCreateDialog = true
                        },
                        size = 40.dp,
                        iconSize = 22.dp,
                        contentDescription = "Create Playlist"
                    )
                }
            }
        }

        // 2. Search Field (Collapsible)
        item {
            AnimatedVisibility(visible = isSearchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RadiusFull)
                        .background(ObsidianElevated)
                        .border(1.dp, ObsidianBorder, RadiusFull)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(palette.accent),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search playlists...",
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { searchQuery = "" }
                            )
                        }
                    }
                }
            }
        }

        // 3. AI SMART CLUSTER BANNER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6200EA),
                                Color(0xFF2979FF),
                                Color(0xFF00B0FF)
                            )
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                    .clickable {
                        if (!isGeneratingAi) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            isGeneratingAi = true
                            playerViewModel.generateAiPlaylists { count ->
                                isGeneratingAi = false
                                Toast.makeText(
                                    context,
                                    "✨ AI analyzed library & generated $count language playlists!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SMART AI CLUSTERING",
                                color = Color(0xFFFFD54F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isGeneratingAi) "Analyzing songs & languages..." else "Auto-Generate Language Playlists",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Classifies Telugu, Tamil, Hindi, English, Punjabi & moods",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Generate",
                            tint = Color(0xFF6200EA),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 4. Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val filters = listOf(
                    "All" to "All (${playlists.size})",
                    "AI" to "✨ AI Smart ($aiCount)",
                    "Custom" to "Custom ($customCount)"
                )
                items(filters) { (key, label) ->
                    val isSelected = selectedFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RadiusFull)
                            .background(if (isSelected) palette.accent else ObsidianPill)
                            .clickable {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                selectedFilter = key
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else TextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 5. Playlist Cards List
        if (filteredPlaylists.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (playlists.isEmpty()) "No playlists found. Tap '✨ Auto-Generate' above!" else "No matching playlists.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(filteredPlaylists, key = { it.playlist.id }) { item ->
                val playlist = item.playlist
                val songCount = item.tracks.size
                val pColor = Color(playlist.colorHex)

                SleekCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = ObsidianSurface,
                    shape = RadiusLg,
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onPlaylistClick(playlist.id)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Playlist Icon Box
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RadiusMd)
                                .background(pColor.copy(alpha = 0.25f))
                                .border(1.dp, pColor.copy(alpha = 0.5f), RadiusMd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (playlist.isAiGenerated) Icons.Default.AutoAwesome else Icons.Default.FeaturedPlayList,
                                contentDescription = null,
                                tint = pColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Title & Subtitle
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = playlist.name,
                                    color = TextPrimary,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (playlist.isAiGenerated) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(pColor.copy(alpha = 0.20f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "AI",
                                            color = pColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (playlist.description.isNotBlank()) playlist.description else "$songCount Songs",
                                color = TextSecondary,
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Play Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.10f))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    playerViewModel.playPlaylist(item)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Options Menu (Delete)
                        var menuOpen by remember { mutableStateOf(false) }
                        Box {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = TextMuted,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .size(20.dp)
                                    .clickable { menuOpen = true }
                            )

                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false },
                                modifier = Modifier.background(ObsidianElevated)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete", color = Color(0xFFFF5252)) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                                    onClick = {
                                        menuOpen = false
                                        playerViewModel.deletePlaylist(playlist.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE PLAYLIST DIALOG
    if (showCreateDialog) {
        var newTitle by remember { mutableStateOf("") }
        var selectedColorHex by remember { mutableStateOf(PRESET_COLORS.first()) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = ObsidianElevated,
            title = {
                Text("Create New Playlist", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text("Enter playlist name:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RadiusMd)
                            .background(ObsidianSurface)
                            .border(1.dp, ObsidianBorder, RadiusMd)
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                            cursorBrush = SolidColor(palette.accent),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (newTitle.isEmpty()) {
                                    Text("Playlist name (e.g. My Favorites)", color = TextMuted, fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Theme Color:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PRESET_COLORS) { colorHex ->
                            val isChosen = selectedColorHex == colorHex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(if (isChosen) 2.5.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { selectedColorHex = colorHex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            playerViewModel.createPlaylist(newTitle.trim(), selectedColorHex)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Create", color = palette.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
