package com.ipodmodern.audio.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ipodmodern.audio.core.model.Track
import com.ipodmodern.audio.ui.theme.LocalThemePalette
import com.ipodmodern.audio.ui.theme.RadiusFull
import com.ipodmodern.audio.ui.theme.RadiusLg
import com.ipodmodern.audio.ui.theme.RadiusMd
import com.ipodmodern.audio.ui.theme.RadiusSm
import java.io.File
import java.util.Locale

/**
 * Section header with high-contrast title and optional "See All" button.
 */
@Composable
fun HomeSectionHeader(
    title: String,
    onSeeAllClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = palette.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
        )
        if (onSeeAllClick != null) {
            Text(
                text = "See All",
                color = palette.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RadiusFull)
                    .clickable { onSeeAllClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * CategorySelectorRow provides the horizontal scroll filter pills
 * [ All ] [ Party ] [ Blues ] [ Soul ] [ Hip-Hop ] [ Rock ] [ Jazz ]
 * specified in PRD Section 12.
 */
@Composable
fun CategorySelectorRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val view = LocalView.current

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category.equals(selectedCategory, ignoreCase = true)
            var isPressed by remember { mutableStateOf(false) }

            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.93f else 1.0f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 1800f),
                label = "chip_scale"
            )

            val bg = if (isSelected) palette.accent else palette.surfaceElevated
            val textCol = if (isSelected) palette.bg else palette.textPrimary
            val borderCol = if (isSelected) palette.accentLight else palette.borderSubtle

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RadiusFull)
                    .background(bg)
                    .border(1.dp, borderCol, RadiusFull)
                    .pointerInput(category) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = {
                                onCategorySelected(category)
                            }
                        )
                    }
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category,
                    color = textCol,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

/**
 * PopularSongCard provides the dominant album-art carousel card specified in PRD Section 13.
 */
@Composable
fun PopularSongCard(
    track: Track,
    isPlaying: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onPlayDirect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val context = LocalContext.current
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 1600f),
        label = "pop_song_scale"
    )

    val artworkRequest = remember(track.artworkUri) {
        track.artworkUri?.let { uri ->
            ImageRequest.Builder(context)
                .data(uri)
                .size(360)
                .crossfade(true)
                .memoryCacheKey("pop_${track.id}_$uri")
                .diskCacheKey("pop_${track.id}_$uri")
                .allowHardware(true)
                .build()
        }
    }

    Column(
        modifier = modifier
            .width(155.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(track.id) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
    ) {
        // Prominent artwork container with play trigger overlay
        Box(
            modifier = Modifier
                .size(155.dp)
                .clip(RadiusLg)
                .background(palette.surfaceElevated)
                .border(
                    width = if (isCurrent) 1.5.dp else 1.dp,
                    color = if (isCurrent) palette.accent else palette.borderSubtle,
                    shape = RadiusLg
                )
        ) {
            if (artworkRequest != null) {
                AsyncImage(
                    model = artworkRequest,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = palette.textMuted,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            // Quick Play pill button in bottom-right corner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(palette.accent)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onPlayDirect()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCurrent && isPlaying) Icons.Default.Equalizer else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = palette.bg,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = track.title,
            color = if (isCurrent) palette.accent else palette.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Artist
        Text(
            text = track.artist.ifBlank { "Unknown Artist" },
            color = palette.textSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * CollectionHeroCard provides high-impact hero gradient cards ("Top Songs Global", "Discover", etc.)
 * specified in PRD Section 14.
 */
@Composable
fun CollectionHeroCard(
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    trackCount: Int,
    onPlayClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 1600f),
        label = "hero_card_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RadiusLg)
            .background(Brush.linearGradient(gradientColors))
            .border(1.dp, palette.borderHighlight, RadiusLg)
            .pointerInput(title) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            }
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Badge
                Box(
                    modifier = Modifier
                        .clip(RadiusFull)
                        .background(Color(0x33000000))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$trackCount SONGS",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Quick Play Action Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(palette.accent)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onPlayClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Collection",
                        tint = palette.bg,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

/**
 * SongRowItem displays an individual song row with thumbnail, metadata, duration,
 * and favorite icon.
 */
@Composable
fun SongRowItem(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onTrackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalThemePalette.current
    val context = LocalContext.current
    val view = LocalView.current

    val artworkRequest = remember(track.artworkUri) {
        track.artworkUri?.let { uri ->
            ImageRequest.Builder(context)
                .data(uri)
                .size(120)
                .crossfade(true)
                .memoryCacheKey("row_${track.id}_$uri")
                .diskCacheKey("row_${track.id}_$uri")
                .allowHardware(true)
                .build()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RadiusMd)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onTrackClick()
            }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RadiusSm)
                .background(palette.surfaceElevated)
                .border(1.dp, if (isCurrent) palette.accent else palette.borderSubtle, RadiusSm),
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
                    tint = palette.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isCurrent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x77000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Equalizer else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isCurrent) palette.accent else palette.textPrimary,
                fontSize = 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist.ifBlank { "Unknown Artist" },
                color = palette.textSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Favorite Heart Button
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) palette.accent else palette.textMuted,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onFavoriteClick()
                }
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Options More Button
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Options",
            tint = palette.textMuted,
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .clickable {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onOptionsClick()
                }
        )
    }
}
