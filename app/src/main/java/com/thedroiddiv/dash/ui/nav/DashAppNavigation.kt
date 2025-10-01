package com.thedroiddiv.dash.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thedroiddiv.dash.ui.screen.details.VideoDetailsScreen
import com.thedroiddiv.dash.ui.screen.home.HomeScreen
import com.thedroiddiv.dash.ui.screen.player.VideoPlayerScreen

@Composable
fun DashAppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
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
            VideoPlayerScreen()
        }

        composable("video_details") {
            VideoDetailsScreen()
        }
    }
}