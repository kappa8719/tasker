package kr.entropi.tasker.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavOptions
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navOptions
import androidx.navigation.ui.navigateUp
import kotlinx.coroutines.launch
import kr.entropi.tasker.navigation.LocalNavController
import kr.entropi.tasker.navigation.MachineListRoute
import kr.entropi.tasker.navigation.TaskListRoute

private class LocalNavigatorContext(val drawerState: DrawerState)

private val LocalNavigator =
    compositionLocalOf<LocalNavigatorContext> { error("no LocalNavigator provided") }

@Composable
fun Navigator(content: @Composable () -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val localNavigatorContext = LocalNavigatorContext(
        drawerState = drawerState
    )

    CompositionLocalProvider(LocalNavigator provides localNavigatorContext) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerState = drawerState) {
                    Text("Tasker", modifier = Modifier.padding(16.dp))
                    HorizontalDivider()
                    Column(
                        Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NavigatorItem(
                            route = TaskListRoute,
                            icon = { Icon(Icons.Default.Checklist, null) },
                            text = { Text(text = "Tasks") }
                        )
                        NavigatorItem(
                            route = MachineListRoute,
                            icon = { Icon(Icons.Default.Dns, null) },
                            text = { Text(text = "Machines") }
                        )
                    }
                }
            }
        ) {
            content()
        }
    }
}

@Composable
private inline fun <reified T : Any> NavigatorItem(
    route: T,
    crossinline icon: @Composable () -> Unit = {},
    crossinline text: @Composable () -> Unit = {}
) {
    val navController = LocalNavController.current
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val localNavigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    NavigationDrawerItem(
        label = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                icon()
                text()
            }
        },
        selected = currentDestination?.hierarchy?.any { it.hasRoute(T::class) } == true,
        onClick = {
            // navigate
            if (currentDestination?.hierarchy?.first()?.hasRoute<T>() != true) {
                navController.navigate(route) {
                    currentDestination?.route?.let {
                        popUpTo(it){
                            inclusive = true
                        }
                    }
                }
            }

            // close drawer
            coroutineScope.launch {
                try {
                    localNavigator.drawerState.close()
                } catch (_: Throwable) {
                    // the animation was interrupted
                }
            }
        }
    )
}