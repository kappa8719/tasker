package kr.entropi.tasker.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kr.entropi.tasker.screen.CreateTaskScreen
import kr.entropi.tasker.screen.SelectTaskTypeScreen
import kr.entropi.tasker.screen.MainScreen

val LocalNavController =
    compositionLocalOf<NavHostController> { error("No LocalNavController provided") }

@Composable
fun LocalNavHost() {
    val navController = rememberNavController()

    val tween = tween<IntOffset>(easing = FastOutSlowInEasing, durationMillis = 300)
    val tweenFloat = tween<Float>(easing = FastOutSlowInEasing, durationMillis = 150)

    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = MainEntry,
            enterTransition = {
                fadeIn(tweenFloat) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween,
                    initialOffset = { it / 8 }
                )
            },
            exitTransition = {
                fadeOut(tweenFloat) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween,
                    targetOffset = { it / 8 }
                )
            },
            popEnterTransition = {
                fadeIn(tweenFloat) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween,
                    initialOffset = { it / 8 }
                )
            },
            popExitTransition = {
                fadeOut(tweenFloat) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween,
                    targetOffset = { it / 8 }
                )
            }
        ) {
            composable<MainEntry> { MainScreen() }
            composable<SelectTaskTypeEntry> { SelectTaskTypeScreen() }
            composable<CreateTaskEntry> { entry ->
                val entry = entry.toRoute<CreateTaskEntry>()
                CreateTaskScreen(type = entry.type)
            }
        }
    }
}