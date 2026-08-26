package com.ipodmodern.audio.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import com.ipodmodern.audio.ui.theme.LocalIpodColors
import kotlin.math.abs
import kotlin.math.sign

@Composable
fun CoverFlowScreen(
    albums: List<Album>,
    selectedIndex: Int,
    onAlbumSelect: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalIpodColors.current
    val animatedOffset = remember { Animatable(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) {
        animatedOffset.animateTo(
            targetValue = selectedIndex.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
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
                fontSize = 14.sp
            )
        }
        return
    }

    val currentAlbum = albums.getOrNull(selectedIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060709))
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 3D Spatial Cover Flow Stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Render visible window around selectedIndex (offset -3 to +3)
            for (i in (selectedIndex - 4)..(selectedIndex + 4)) {
                if (i < 0 || i >= albums.size) continue

                val album = albums[i]
                val offset = i - animatedOffset.value
                val absOffset = abs(offset)

                // 3D Projection Calculations
                val rotationY = when {
                    offset < -0.1f -> 55f
                    offset > 0.1f -> -55f
                    else -> -offset * 550f // Smooth transition across center
                }.coerceIn(-60f, 60f)

                val scale = (1.05f - absOffset * 0.14f).coerceIn(0.65f, 1.05f)
                val translationX = offset * 68f
                val zIndexVal = 100f - absOffset * 10f

                Box(
                    modifier = Modifier
                        .zIndex(zIndexVal)
                        .graphicsLayer {
                            this.cameraDistance = 16f
                            this.rotationY = rotationY
                            this.scaleX = scale
                            this.scaleY = scale
                            this.translationX = translationX * density
                            this.transformOrigin = TransformOrigin(
                                pivotFractionX = if (offset < 0) 0.8f else if (offset > 0) 0.2f else 0.5f,
                                pivotFractionY = 0.5f
                            )
                        }
                        .size(110.dp)
                        .clickable { onAlbumSelect(album) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Main Artwork Card
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .shadow(12.dp, RoundedCornerShape(6.dp))
                                .background(Color(0xFF1B1D22))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!album.artworkUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = album.artworkUri,
                                    contentDescription = album.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Album,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            // Glass sheen
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
                                .size(width = 96.dp, height = 36.dp)
                                .graphicsLayer {
                                    scaleY = -1f
                                }
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            if (!album.artworkUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = album.artworkUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            // Alpha gradient mask fading to transparent black
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF060709).copy(alpha = 0.45f),
                                                Color(0xFF060709).copy(alpha = 0.95f),
                                                Color(0xFF060709)
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
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentAlbum.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${currentAlbum.artist} • ${currentAlbum.trackCount} Tracks",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
