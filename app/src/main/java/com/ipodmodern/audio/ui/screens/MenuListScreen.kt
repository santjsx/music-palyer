package com.ipodmodern.audio.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.ui.theme.LocalIpodColors
import com.ipodmodern.audio.ui.theme.iPodSelectionBlue
import com.ipodmodern.audio.ui.theme.iPodSelectionBlueDark

data class MenuItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val hasSubMenu: Boolean = true,
    val badge: String? = null
)

@Composable
fun MenuListScreen(
    items: List<MenuItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalIpodColors.current
    val listState = rememberLazyListState()

    // Automatically scroll to keep selected index visible
    LaunchedEffect(selectedIndex) {
        if (items.isNotEmpty()) {
            listState.animateScrollToItem(
                selectedIndex.coerceIn(0, items.size - 1)
            )
        }
    }

    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Music Found\nUse Wi-Fi Sync to Add Tracks",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 4.dp)
    ) {
        itemsIndexed(items) { index, item ->
            val isSelected = index == selectedIndex
            val itemBg = if (isSelected) {
                Brush.horizontalGradient(
                    listOf(iPodSelectionBlue, iPodSelectionBlueDark)
                )
            } else {
                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
            }

            val textColor = if (isSelected) Color.White else colors.screenText

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(itemBg)
                    .padding(horizontal = 10.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.title,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!item.subtitle.isNullOrEmpty()) {
                        Text(
                            text = item.subtitle,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                if (item.badge != null) {
                    Text(
                        text = item.badge,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                if (item.hasSubMenu) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
