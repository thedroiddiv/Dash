package com.thedroiddiv.dash.ui.screen.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.thedroiddiv.dash.domain.player.IDRMVideoPlaybackController

class VideoPlayerScreenVMFactory(
    private val videoPlaybackController: IDRMVideoPlaybackController
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoPlayerScreenVM::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VideoPlayerScreenVM(videoPlaybackController) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
