package kr.entropi.tasker.screen.machine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToQueue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kr.entropi.tasker.navigation.AddMachineEntry
import kr.entropi.tasker.navigation.LocalNavController

@Composable
fun MachineListScreen() {
    val navController = LocalNavController.current

    Scaffold(floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = {
                navController.navigate(AddMachineEntry)
            },
            icon = { Icon(Icons.Default.AddToQueue, null) },
            text = { Text("머신 추가") })
    }) { padding ->
        Box(Modifier.padding(padding)) {

        }
    }
}