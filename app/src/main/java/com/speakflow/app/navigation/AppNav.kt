package com.speakflow.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.speakflow.feature.cloud.CloudScreen
import com.speakflow.feature.library.LibraryScreen
import com.speakflow.feature.player.PlayerScreen

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "library") {
        composable("library") {
            LibraryScreen(
                onOpenPlayer = { id -> nav.navigate("player/$id") },
                onOpenCloud = { nav.navigate("cloud") }
            )
        }
        composable(
            route = "player/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.StringType })
        ) { back ->
            val id = back.arguments?.getString("mediaId").orEmpty()
            PlayerScreen(mediaId = id, onBack = { nav.popBackStack() })
        }
        composable("cloud") {
            CloudScreen(onFinished = { nav.popBackStack() })
        }
    }
}
