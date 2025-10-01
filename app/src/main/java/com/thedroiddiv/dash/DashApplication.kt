package com.thedroiddiv.dash

import android.app.Application
import com.thedroiddiv.dash.data.player.DRMVideoPlaybackController
import com.thedroiddiv.dash.domain.player.IDRMVideoPlaybackController

class DashApplication : Application() {
    val videoPlayerController: IDRMVideoPlaybackController by lazy {
        DRMVideoPlaybackController(this)
    }
}