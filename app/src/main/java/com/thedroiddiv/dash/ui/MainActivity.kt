package com.thedroiddiv.dash.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.thedroiddiv.dash.DashApplication
import com.thedroiddiv.dash.ui.nav.DashAppNavigation
import com.thedroiddiv.dash.ui.theme.DashTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // VideoPlayerController is created using ApplicationContext so that it doesn't cause activity leaks
        val videoPlaybackController = (this.applicationContext as DashApplication)
            .videoPlaybackController

        val repository = (this.applicationContext as DashApplication)
            .repository

        setContent {
            DashTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DashAppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        videoPlaybackController = videoPlaybackController,
                        repository = repository
                    )
                }
            }
        }
    }
}

