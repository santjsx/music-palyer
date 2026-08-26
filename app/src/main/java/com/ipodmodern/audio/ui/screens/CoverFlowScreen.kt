package com.ipodmodern.audio.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.ipodmodern.audio.core.model.Album
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CoverFlowScreen(
    albums: List<Album>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit = {},
    onAlbumSelect: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedOffset = remember { Animatable(selectedIndex.toFloat()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedIndex) {
        animatedOffset.animateTo(
            targetValue = selectedIndex.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    if (albums.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Albums Found",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
        return
    }

    val currentAlbum = albums.getOrNull(selectedIndex.coerceIn(0, albums.size - 1))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07080A))
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    coroutineScope.launch {
                        val currentVal = animatedOffset.value - (delta / 180f)
                        val clamped = currentVal.coerceIn(0f, (albums.size - 1).toFloat())
                        animatedOffset.snapTo(clamped)
                    }
                },
                onDragStopped = {
                    val target = animatedOffset.value.roundToInt().coerceIn(0, albums.size - 1)
                    onIndexChanged(target)
                }
            )
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3D Spatial Cover Flow Stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Render visible album cards window
            for (i in (selectedIndex - 4)..(selectedIndex + 4)) {
                if (i < 0 || i >= albums.size) continue

                val album = albums[i]
                val offset = i - animatedOffset.value
                val absOffset = abs(offset)

                // 3D Projection calculations
                val rotationY = when {
                    offset < -0.1f -> 55f
                    offset > 0.1f -> -55f
                    else -> -offset * 550f
                }.coerceIn(-60f, 60f)

                val scale = (1.15f - absOffset * 0.16f).coerceIn(0.65f, 1.15f)
                val translationX = offset * 110f
                val zIndexVal = 100f - absOffset * 10f

                Box(
                    modifier = Modifier
                        .zIndex(zIndexVal)
                        .graphicsLayer {
                            this.cameraDistance = 18f
                            this.rotationY = rotationY
                            this.scaleX = scale
                            this.scaleY = scale
                            this.translationX = translationX * density
                            this.transformOrigin = TransformOrigin(
                                pivotFractionX = if (offset < 0) 0.85f else if (offset > 0) 0.15f else 0.5f,
                                pivotFractionY = 0.5f
                            )
                        }
                        .size(190.dp)
                        .clickable {
                            if (absOffset < 0.4f) {
                                onAlbumSelect(album)
                            } else {
                                onIndexChanged(i)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Main Artwork Card
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .shadow(24.dp, RoundedCornerShape(10.dp))
                                .background(Color(0xFF1B1D22))
                                .border(1.5.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!album.artworkUri.isNullOrEmpty()) {
                                val model = if (album.artworkUri.startsWith("/")) java.io.File(album.artworkUri) else album.artworkUri
                                AsyncImage(
                                    model = model,
                                    contentDescription = album.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Album,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(72.dp)
                                )
                            }

                            // Glass reflection
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                                        )
                                    )
                            )
                        }

                        // Dynamic Inverted Mirror Floor Reflection
                        Box(
                            modifier = Modifier
                                .size(width = 160.dp, height = 64.dp)
                                .graphicsLayer {
                                    scaleY = -1f
                                }
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            if (!album.artworkUri.isNullOrEmpty()) {
                                val model = if (album.artworkUri.startsWith("/")) java.io.File(album.artworkUri) else album.artworkUri
                                AsyncImage(
                                    model = model,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // Gradient fade mask
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF07080A).copy(alpha = 0.40f),
                                                Color(0xFF07080A).copy(alpha = 0.92f),
                                                Color(0xFF07080A)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Selected Album Title & Artist Footer
        if (currentAlbum != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAlbumSelect(currentAlbum) }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentAlbum.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${currentAlbum.artist} • ${currentAlbum.trackCount} Tracks",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap album to play",
                    fontSize = 11.sp,
                    color = Color(0xFF0A84FF),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
