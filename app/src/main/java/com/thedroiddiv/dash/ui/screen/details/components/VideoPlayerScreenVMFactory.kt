package com.thedroiddiv.dash.ui.screen.details.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.thedroiddiv.dash.domain.repository.VideoRepository
import com.thedroiddiv.dash.ui.screen.details.VideoDetailsVM

class VideoDetailsVMFactory(
    private val repository: VideoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoDetailsVM::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VideoDetailsVM(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
