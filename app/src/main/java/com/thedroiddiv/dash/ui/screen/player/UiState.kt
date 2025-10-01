package com.thedroiddiv.dash.ui.screen.player

import androidx.media3.exoplayer.ExoPlayer
import com.thedroiddiv.dash.domain.models.PlaybackState
import com.thedroiddiv.dash.domain.models.ResolutionInfo

data class UiState(
    val isLoading: Boolean = false,
    val playbackState: PlaybackState = PlaybackState.UNKNOWN,
    val resolutionDialogVisible: Boolean = false,
    val availableResolutions: List<ResolutionInfo> = listOf(),
    val currentResolution: ResolutionInfo? = null,
    val errorMessage: String? = null
)
