package com.thedroiddiv.dash.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thedroiddiv.dash.domain.player.IDRMVideoPlaybackController
import com.thedroiddiv.dash.domain.repository.VideoRepository
import com.thedroiddiv.dash.ui.screen.details.VideoDetailsScreen
import com.thedroiddiv.dash.ui.screen.details.VideoDetailsVM
import com.thedroiddiv.dash.ui.screen.details.components.VideoDetailsVMFactory
import com.thedroiddiv.dash.ui.screen.home.HomeScreen
import com.thedroiddiv.dash.ui.screen.player.VideoPlayerScreen
import com.thedroiddiv.dash.ui.screen.player.VideoPlayerScreenVM
import com.thedroiddiv.dash.ui.screen.player.VideoPlayerScreenVMFactory

@Composable
fun DashAppNavigation(
    modifier: Modifier = Modifier,
    videoPlaybackController: IDRMVideoPlaybackController,
    repository: VideoRepository
) {
    val navController = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "home_screen"
    ) {
        composable("home_screen") {
            HomeScreen(
                navigateToPlayer = {
                    navController.navigate("video_player")
                },
                navigateToDetails = {
                    navController.navigate("video_details")
                }
            )
        }

        composable("video_player") {
            // ideally the dependencies would come a DI framework such as hilt or koin
            val viewModel = viewModel<VideoPlayerScreenVM>(
                factory = VideoPlayerScreenVMFactory(videoPlaybackController)
            )
            VideoPlayerScreen(viewModel)
        }

        composable("video_details") {
            // ideally the dependencies would come a DI framework such as hilt or koin
            val viewModel = viewModel<VideoDetailsVM>(
                factory = VideoDetailsVMFactory(repository)
            )
            VideoDetailsScreen(viewModel = viewModel)
        }
    }
}