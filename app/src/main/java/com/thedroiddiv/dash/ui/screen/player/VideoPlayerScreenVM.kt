package com.thedroiddiv.dash.ui.screen.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.thedroiddiv.dash.domain.models.PlaybackState
import com.thedroiddiv.dash.domain.models.ResolutionInfo
import com.thedroiddiv.dash.domain.player.IDRMVideoPlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class VideoPlayerScreenVM(
    val videoPlaybackController: IDRMVideoPlaybackController
) : ViewModel() {

    val manifestUrl = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/mpds/11331.mpd"
    val licenseServerUrl = "https://cwip-shaka-proxy.appspot.com/no_auth"

    var exoPlayer: ExoPlayer? = null

    private val _uiState = MutableStateFlow<UiState?>(null)
    val uiState = _uiState.asStateFlow()

    private val callback = object : IDRMVideoPlaybackController.Callback {
        override fun onError(message: String) {
            _uiState.update { it?.copy(errorMessage = message) }
        }

        override fun onStateChanged(playbackState: PlaybackState) {
            when (playbackState) {
                PlaybackState.IDLE -> {
                    _uiState.update {
                        it?.copy(
                            playbackState = PlaybackState.IDLE
                        )
                    }
                }

                PlaybackState.BUFFERING -> {
                    _uiState.update {
                        it?.copy(
                            isLoading = true,
                            playbackState = PlaybackState.BUFFERING
                        )
                    }
                }

                PlaybackState.READY -> {
                    _uiState.update {
                        it?.copy(
                            isLoading = false,
                            playbackState = PlaybackState.READY
                        )
                    }
                }

                PlaybackState.ENDED -> {
                    _uiState.update {
                        it?.copy(
                            playbackState = PlaybackState.ENDED
                        )
                    }
                }

                PlaybackState.UNKNOWN -> {
                    _uiState.update {
                        it?.copy(
                            playbackState = PlaybackState.UNKNOWN
                        )
                    }
                }
            }
        }

        override fun onTracksChanged(updatedInfo: List<ResolutionInfo>) {
            val state = uiState.value
            if (state == null) return
            _uiState.update {
                var currentResolution = state.currentResolution
                if (currentResolution == null) {
                    currentResolution = updatedInfo.find { i -> i.isAuto }
                }
                it?.copy(
                    availableResolutions = updatedInfo,
                    currentResolution = currentResolution
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            loadVideo(
                manifestUri = manifestUrl,
                licenseServerUrl = licenseServerUrl
            )
        }
    }


    private suspend fun loadVideo(
        manifestUri: String,
        licenseServerUrl: String
    ) {
        exoPlayer = videoPlaybackController.prepare(
            manifestUri = manifestUri,
            licenseServerUrl = licenseServerUrl,
            callback = callback
        )
        _uiState.update { UiState() }
    }

    fun onUiEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            UiEvent.ChangeResolutionClicked -> {
                _uiState.update { it?.copy(resolutionDialogVisible = true) }
            }

            UiEvent.ResolutionDialogDismissed -> {
                _uiState.update { it?.copy(resolutionDialogVisible = false) }
            }

            is UiEvent.ResolutionSelected -> viewModelScope.launch {
                videoPlaybackController.changeResolution(uiEvent.resolution)
            }

            UiEvent.RetryClicked -> viewModelScope.launch {
                videoPlaybackController.release()
                loadVideo(
                    manifestUri = manifestUrl,
                    licenseServerUrl = licenseServerUrl
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        CoroutineScope(Dispatchers.Default).launch {
            videoPlaybackController.release()
        }
    }
}
