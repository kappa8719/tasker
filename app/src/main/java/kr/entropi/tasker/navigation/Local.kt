package kr.entropi.tasker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kr.entropi.tasker.screen.MainScreen

val LocalNavController =
    compositionLocalOf<NavHostController> { error("No LocalNavController provided") }

@Composable
fun LocalNavHost() {
    val navController = rememberNavController()

    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(navController = navController, startDestination = MainEntry) {
            composable<MainEntry> { MainScreen() }
        }
    }
}