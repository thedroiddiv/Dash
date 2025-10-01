package com.thedroiddiv.dash.domain.player

import androidx.media3.exoplayer.ExoPlayer
import com.thedroiddiv.dash.domain.models.PlaybackState
import com.thedroiddiv.dash.domain.models.ResolutionInfo

interface IDRMVideoPlaybackController {
    suspend fun prepare(
        manifestUri: String,
        licenseServerUrl: String,
        callback: Callback
    ): ExoPlayer?

    suspend fun changeResolution(newResolution: ResolutionInfo) : Boolean

    suspend fun release()

    interface Callback {
        fun onError(message: String)
        fun onStateChanged(playbackState: PlaybackState)
        fun onTracksChanged(updatedInfo: List<ResolutionInfo>)
    }
}