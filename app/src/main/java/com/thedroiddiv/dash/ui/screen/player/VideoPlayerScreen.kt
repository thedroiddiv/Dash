package com.thedroiddiv.dash.ui.screen.player

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.SHOW_BUFFERING_WHEN_PLAYING
import com.thedroiddiv.dash.domain.models.ResolutionInfo

@Composable
fun VideoPlayerScreen(
    viewModel: VideoPlayerScreenVM
) {
    val uiState by viewModel.uiState.collectAsState()
    uiState?.let { state ->
        VideoPlayerScreen(
            exoPlayer = viewModel.exoPlayer,
            uiState = state,
            onUiEvent = viewModel::onUiEvent
        )
    } ?: run {
        Text("Loading...")
    }
}


@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    exoPlayer: ExoPlayer?,
    uiState: UiState,
    onUiEvent: (UiEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        exoPlayer?.let {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        setShowBuffering(SHOW_BUFFERING_WHEN_PLAYING)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (uiState.availableResolutions.isNotEmpty() && !uiState.isLoading && uiState.errorMessage == null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clickable { onUiEvent(UiEvent.ChangeResolutionClicked) },
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Resolution",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.currentResolution?.displayName ?: "Auto",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Playback Error",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onUiEvent(UiEvent.RetryClicked) }) {
                        Text("Retry")
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.7f)
            )
        ) {
            Text(
                text = "Status: ${uiState.playbackState}",
                modifier = Modifier.padding(8.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (uiState.resolutionDialogVisible) {
        ResolutionSelectionDialog(
            resolutions = uiState.availableResolutions,
            currentResolution = uiState.currentResolution,
            onResolutionSelected = { resolutionInfo ->
                onUiEvent(UiEvent.ResolutionSelected(resolutionInfo))
            },
            onDismiss = {
                onUiEvent(UiEvent.ResolutionDialogDismissed)
            }
        )
    }
}


@Composable
fun ResolutionSelectionDialog(
    resolutions: List<ResolutionInfo>,
    currentResolution: ResolutionInfo?,
    onResolutionSelected: (ResolutionInfo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Video Resolution")
        },
        text = {
            LazyColumn {
                items(resolutions) { resolutionInfo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onResolutionSelected(resolutionInfo) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentResolution?.trackIndex == resolutionInfo.trackIndex,
                            onClick = { onResolutionSelected(resolutionInfo) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = resolutionInfo.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (resolutionInfo.bitrate > 0) {
                                Text(
                                    text = "${resolutionInfo.bitrate / 1000} kbps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}