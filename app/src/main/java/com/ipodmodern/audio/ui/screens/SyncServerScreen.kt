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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipodmodern.audio.core.sync.SyncServerState
import com.ipodmodern.audio.ui.components.NeoBadge
import com.ipodmodern.audio.ui.components.NeoCard
import com.ipodmodern.audio.ui.components.NeoIconButton
import com.ipodmodern.audio.ui.components.neoShadow
import com.ipodmodern.audio.ui.theme.NeoBgDark
import com.ipodmodern.audio.ui.theme.NeoBlack
import com.ipodmodern.audio.ui.theme.NeoBlue
import com.ipodmodern.audio.ui.theme.NeoBorderThick
import com.ipodmodern.audio.ui.theme.NeoBorderWidth
import com.ipodmodern.audio.ui.theme.NeoGreen
import com.ipodmodern.audio.ui.theme.NeoMuted
import com.ipodmodern.audio.ui.theme.NeoPink
import com.ipodmodern.audio.ui.theme.NeoPurple
import com.ipodmodern.audio.ui.theme.NeoRadiusLg
import com.ipodmodern.audio.ui.theme.NeoRadiusMd
import com.ipodmodern.audio.ui.theme.NeoRadiusSm
import com.ipodmodern.audio.ui.theme.NeoWhite
import com.ipodmodern.audio.ui.theme.NeoYellow

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
            .background(NeoBgDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 140.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
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
                    fontFamily = FontFamily.Monospace,
                    color = NeoWhite,
                    letterSpacing = 1.0.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "SYNC HUB",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoYellow,
                    letterSpacing = 1.sp
                )
            }

            NeoBadge(
                text = if (serverState.isRunning) "SERVER ONLINE" else "SERVER OFFLINE",
                backgroundColor = if (serverState.isRunning) NeoGreen else NeoPink,
                textColor = NeoWhite
            )
        }

        // Main Wi-Fi Instructions Card
        NeoCard(
            backgroundColor = NeoWhite,
            borderColor = NeoBlack,
            borderWidth = NeoBorderThick,
            shadowOffset = 5.dp,
            cornerRadius = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Wi-Fi Icon Badge
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(if (serverState.isRunning) NeoYellow else NeoWhite)
                        .border(NeoBorderWidth, NeoBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = NeoBlack,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "WIRELESS AUDIO TRANSFER",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = NeoBlack,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Open this address in any browser on your computer connected to the same Wi-Fi network to transfer audio files directly:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoBlack.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Highlighted IP Container (High-Contrast Bold Black Text)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(NeoRadiusMd)
                        .background(NeoYellow)
                        .border(NeoBorderWidth, NeoBlack, NeoRadiusMd)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (serverState.isRunning) "http://${serverState.hostAddress}:${serverState.port}" else "Connecting to Wi-Fi...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Stats & Actions Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Uploaded Files Card
            NeoCard(
                backgroundColor = NeoWhite,
                cornerRadius = 14.dp,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = NeoBlack,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${serverState.totalUploadedFiles}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack
                    )
                    Text(
                        text = "FILES SYNCED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoMuted
                    )
                }
            }

            // Rescan Button Card
            NeoCard(
                backgroundColor = NeoYellow,
                cornerRadius = 14.dp,
                modifier = Modifier.weight(1f),
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onRescanClick()
                }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = NeoBlack,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "RESCAN",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoBlack
                    )
                    Text(
                        text = "Refresh Library",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoBlack.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Supported Codecs Footer
        NeoCard(
            backgroundColor = NeoPurple,
            cornerRadius = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Supports bit-perfect FLAC, DSD (.dsf/.dff), WAV, ALAC, and .CUE sheets",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = NeoBlack,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
    }
}
