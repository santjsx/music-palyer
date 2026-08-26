package com.ipodmodern.audio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.theme.LocalIpodColors

data class MenuItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val hasSubMenu: Boolean = true,
    val badge: String? = null
)

private fun getMenuIcon(id: String, title: String): ImageVector {
    return when {
        id.contains("cover_flow", true) || title.contains("Cover Flow", true) -> Icons.Default.ViewCarousel
        id.contains("songs", true) || title.contains("Songs", true) -> Icons.Default.MusicNote
        id.contains("albums", true) || title.contains("Albums", true) -> Icons.Default.Album
        id.contains("artists", true) || title.contains("Artists", true) -> Icons.Default.Person
        id.contains("playlists", true) || title.contains("Playlists", true) -> Icons.Default.QueueMusic
        id.contains("eq", true) || title.contains("Equalizer", true) -> Icons.Default.GraphicEq
        id.contains("sync", true) || title.contains("Sync", true) -> Icons.Default.Wifi
        id.contains("settings", true) || title.contains("Settings", true) -> Icons.Default.Settings
        else -> Icons.Default.LibraryMusic
    }
}

@Composable
fun MenuListScreen(
    items: List<MenuItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalIpodColors.current
    val listState = rememberLazyListState()

    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Music Found\nUse Wi-Fi Sync to Add Lossless Tracks",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07080B))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        itemsIndexed(items) { index, item ->
            val isSelected = index == selectedIndex
            val icon = getMenuIcon(item.id, item.title)

            val itemBg = if (isSelected) {
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF007AFF),
                        Color(0xFF0055D4)
                    )
                )
            } else {
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF14171E).copy(alpha = 0.6f),
                        Color(0xFF101217).copy(alpha = 0.6f)
                    )
                )
            }

            val textColor = if (isSelected) Color.White else Color(0xFFF0F2F5)
            val subtextColor = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF8A8F9E)
            val iconTint = if (isSelected) Color.White else Color(0xFF0A84FF)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(itemBg)
                    .border(
                        1.dp,
                        if (isSelected) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.06f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onItemClick(index)
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Category Icon Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color(0xFF1A1E27)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.title,
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!item.subtitle.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.subtitle,
                            color = subtextColor,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (item.badge != null) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFF1E222D))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.badge,
                            color = if (isSelected) Color.White else Color(0xFF9EACB9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (item.hasSubMenu) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
