package com.ipodmodern.audio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.sync.SyncServerState
import com.ipodmodern.audio.ui.theme.AudiophileGold
import com.ipodmodern.audio.ui.theme.LocalIpodColors
import com.ipodmodern.audio.ui.theme.LosslessGreen

@Composable
fun SyncServerScreen(
    serverState: SyncServerState,
    modifier: Modifier = Modifier
) {
    val colors = LocalIpodColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status Badge
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (serverState.isRunning) LosslessGreen else Color.Gray)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (serverState.isRunning) "SYNC SERVER RUNNING" else "SYNC SERVER OFFLINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (serverState.isRunning) LosslessGreen else Color.Gray,
                fontFamily = FontFamily.Monospace
            )
        }

        // Center Connection Information Box
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                tint = if (serverState.isRunning) AudiophileGold else Color.Gray,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Open on your PC/Mac browser:",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (serverState.isRunning) "http://${serverState.hostAddress}:${serverState.port}" else "Connecting to Wi-Fi...",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (serverState.isRunning) AudiophileGold else Color.Gray,
                fontFamily = FontFamily.Monospace
            )
        }

        // Upload Counter & Specs
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Ingested This Session: ${serverState.totalUploadedFiles} files",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.screenText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Drag and drop FLAC, WAV, DSD or .CUE sheets to sync directly into device storage.",
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
