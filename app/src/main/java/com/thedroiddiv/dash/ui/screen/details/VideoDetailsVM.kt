package com.thedroiddiv.dash.ui.screen.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.network.HttpException
import com.thedroiddiv.dash.domain.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoDetailsVM(
    private val repository: VideoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private suspend fun fetchVideoDetails(
        videoId: String,
        userToken: String
    ) {
        repository.fetchVideoDetails(
            videoId = videoId,
            userToken = userToken
        ).onSuccess {
            _uiState.value = _uiState.value.copy(
                video = it
            )
        }.onFailure {
            _uiState.value = _uiState.value.copy(
                error = it.message
            )
        }
    }

    fun onUiEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            is UiEvent.LoadVideoDetailsClicked -> viewModelScope.launch {
                fetchVideoDetails(uiEvent.videoId, uiEvent.userToken)
            }
        }
    }
}