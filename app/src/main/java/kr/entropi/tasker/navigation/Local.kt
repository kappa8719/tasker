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
import kr.entropi.tasker.screen.task.UpdateTaskScreen
import kr.entropi.tasker.screen.machine.MachineListScreen
import kr.entropi.tasker.screen.task.SelectTaskTypeScreen
import kr.entropi.tasker.screen.task.TaskListScreen
import kr.entropi.tasker.screen.machine.AddMachineScreen

val LocalNavController =
    compositionLocalOf<NavHostController> { error("No LocalNavController provided") }

@Composable
fun LocalNavHost(block: @Composable (content: @Composable () -> Unit) -> Unit) {
    val navController = rememberNavController()

    val tween = tween<IntOffset>(easing = FastOutSlowInEasing, durationMillis = 300)
    val tweenFloat = tween<Float>(easing = FastOutSlowInEasing, durationMillis = 150)

    CompositionLocalProvider(LocalNavController provides navController) {
        block {
            NavHost(
                navController = navController,
                startDestination = TaskListRoute,
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
                composable<TaskListRoute> { TaskListScreen() }
                composable<SelectTaskTypeRoute> { SelectTaskTypeScreen() }
                composable<UpdateTaskRoute> { entry ->
                    val entry = entry.toRoute<UpdateTaskRoute>()
                    UpdateTaskScreen(route = entry)
                }
                composable<MachineListRoute> { MachineListScreen() }
                composable<AddMachineRoute> { AddMachineScreen() }
            }
        }
    }
}