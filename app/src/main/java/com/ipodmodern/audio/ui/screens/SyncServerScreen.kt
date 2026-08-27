package com.ipodmodern.audio.ui.screens

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.sync.SyncServerState
import com.ipodmodern.audio.ui.theme.ModernAccentBlue
import com.ipodmodern.audio.ui.theme.ModernAccentCyan
import com.ipodmodern.audio.ui.theme.ModernAccentEmerald
import com.ipodmodern.audio.ui.theme.ModernAccentPurple
import com.ipodmodern.audio.ui.theme.ModernHeroGradient
import com.ipodmodern.audio.ui.theme.ModernTextMuted
import com.ipodmodern.audio.ui.theme.ModernTextPrimary
import com.ipodmodern.audio.ui.theme.ModernTextSecondary

@Composable
fun SyncServerScreen(
    serverState: SyncServerState,
    onRescanClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color(0xFF07080B))
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // MARK: - Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WI-FI SYNC & INGESTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ModernAccentCyan,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Sync Hub",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ModernTextPrimary
                )
            }

            // Server Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (serverState.isRunning) ModernAccentEmerald.copy(alpha = 0.15f) else Color(0xFF1E232E))
                    .border(
                        1.dp,
                        if (serverState.isRunning) ModernAccentEmerald.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.12f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (serverState.isRunning) ModernAccentEmerald else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (serverState.isRunning) "ONLINE" else "OFFLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (serverState.isRunning) ModernAccentEmerald else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MARK: - Connection Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = ModernAccentCyan.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF141822), Color(0xFF0F1118))
                    )
                )
                .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ModernAccentCyan, ModernAccentBlue)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Wireless Audio Transfer",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ModernTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Open this address in any browser on your computer connected to the same Wi-Fi network:",
                    fontSize = 13.sp,
                    color = ModernTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Highlighted IP Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF090B10))
                        .border(1.dp, ModernAccentCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (serverState.isRunning) "http://${serverState.hostAddress}:${serverState.port}" else "Connecting to Wi-Fi...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ModernAccentCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MARK: - Session Statistics Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF10131B))
                    .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = ModernAccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${serverState.totalUploadedFiles}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ModernTextPrimary
                    )
                    Text(
                        text = "Files Synced",
                        fontSize = 11.sp,
                        color = ModernTextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF10131B))
                    .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(18.dp))
                    .clickable { onRescanClick() }
                    .padding(16.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = ModernAccentEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rescan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ModernTextPrimary
                    )
                    Text(
                        text = "Refresh Library",
                        fontSize = 11.sp,
                        color = ModernTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Supports bit-perfect FLAC, DSD (.dsf/.dff), WAV, ALAC, and .CUE sheet splitting",
            fontSize = 11.sp,
            color = ModernTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 70.dp)
        )
    }
}
