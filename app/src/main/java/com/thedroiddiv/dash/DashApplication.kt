package com.thedroiddiv.dash

import android.app.Application
import com.thedroiddiv.dash.data.player.DRMVideoPlaybackController
import com.thedroiddiv.dash.data.repository.StarryRepository
import com.thedroiddiv.dash.domain.player.IDRMVideoPlaybackController
import com.thedroiddiv.dash.domain.repository.VideoRepository
import io.ktor.client.HttpClient

class DashApplication : Application() {
    val videoPlaybackController: IDRMVideoPlaybackController by lazy {
        DRMVideoPlaybackController(this)
    }

    val client: HttpClient by lazy { HttpClient() }
    val repository: VideoRepository by lazy {
        StarryRepository(client)
    }
}