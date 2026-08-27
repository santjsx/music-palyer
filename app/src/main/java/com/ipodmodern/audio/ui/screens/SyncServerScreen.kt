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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.sync.SyncServerState
import com.ipodmodern.audio.ui.components.RaycastCard
import com.ipodmodern.audio.ui.components.RaycastKeycapBadge
import com.ipodmodern.audio.ui.theme.RaycastAccentGreen
import com.ipodmodern.audio.ui.theme.RaycastAsh
import com.ipodmodern.audio.ui.theme.RaycastBody
import com.ipodmodern.audio.ui.theme.RaycastCanvas
import com.ipodmodern.audio.ui.theme.RaycastHairline
import com.ipodmodern.audio.ui.theme.RaycastHairlineStrong
import com.ipodmodern.audio.ui.theme.RaycastInk
import com.ipodmodern.audio.ui.theme.RaycastMute
import com.ipodmodern.audio.ui.theme.RaycastPrimaryWhite
import com.ipodmodern.audio.ui.theme.RaycastRadiusLg
import com.ipodmodern.audio.ui.theme.RaycastRadiusMd
import com.ipodmodern.audio.ui.theme.RaycastSurface
import com.ipodmodern.audio.ui.theme.RaycastSurfaceElevated

@Composable
fun SyncServerScreen(
    serverState: SyncServerState,
    onRescanClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(RaycastCanvas)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // MARK: - Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WI-FI SYNC & INGESTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaycastMute,
                    letterSpacing = 1.0.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Sync Hub",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaycastInk,
                    letterSpacing = 0.2.sp
                )
            }

            // Server Status Keycap Badge
            RaycastKeycapBadge(
                text = if (serverState.isRunning) "SERVER ONLINE" else "SERVER OFFLINE",
                textColor = if (serverState.isRunning) RaycastAccentGreen else RaycastAsh,
                accentColor = if (serverState.isRunning) RaycastAccentGreen else RaycastAsh
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MARK: - Connection Card (Raycast Command-Palette Frame)
        RaycastCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RaycastRadiusLg,
            backgroundColor = RaycastSurface
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(RaycastSurfaceElevated)
                        .border(1.dp, RaycastHairline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = if (serverState.isRunning) RaycastPrimaryWhite else RaycastMute,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Wireless Audio Transfer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RaycastInk,
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Open this address in any browser on your computer connected to the same Wi-Fi network:",
                    fontSize = 13.sp,
                    color = RaycastBody,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Highlighted IP Pill Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RaycastRadiusMd)
                        .background(RaycastSurfaceElevated)
                        .border(1.dp, RaycastHairlineStrong, RaycastRadiusMd)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (serverState.isRunning) "http://${serverState.hostAddress}:${serverState.port}" else "Connecting to Wi-Fi...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaycastPrimaryWhite,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MARK: - Session Statistics Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RaycastCard(
                modifier = Modifier.weight(1f),
                shape = RaycastRadiusMd
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = RaycastBody,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${serverState.totalUploadedFiles}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaycastInk
                    )
                    Text(
                        text = "Files Synced",
                        fontSize = 11.sp,
                        color = RaycastMute
                    )
                }
            }

            RaycastCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onRescanClick()
                    },
                shape = RaycastRadiusMd,
                backgroundColor = RaycastSurfaceElevated
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = RaycastPrimaryWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rescan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RaycastPrimaryWhite
                    )
                    Text(
                        text = "Refresh Library",
                        fontSize = 11.sp,
                        color = RaycastBody
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Supports bit-perfect FLAC, DSD (.dsf/.dff), WAV, ALAC, and .CUE sheets",
            fontSize = 11.sp,
            color = RaycastAsh,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 120.dp)
        )
    }
}
