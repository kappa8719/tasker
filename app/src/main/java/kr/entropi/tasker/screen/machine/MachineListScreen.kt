package kr.entropi.tasker.screen.machine

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToQueue
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kr.entropi.tasker.machine.Machine
import kr.entropi.tasker.machine.MachinesViewModel
import kr.entropi.tasker.navigation.AddMachineRoute
import kr.entropi.tasker.navigation.LocalNavController

@Composable
fun MachineListScreen() {
    val navController = LocalNavController.current
    val machinesViewModel = hiltViewModel<MachinesViewModel>()
    val machines by machinesViewModel.machines.collectAsState()

    Scaffold(floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = {
                navController.navigate(AddMachineRoute)
            },
            icon = { Icon(Icons.Default.AddToQueue, null) },
            text = { Text("머신 추가") })
    }) { padding ->
        Box(Modifier
            .padding(padding)
            .padding(8.dp)) {
            LazyColumn {
                items(machines.toList()) { machine ->
                    MachineItem(machine, modifier = Modifier
                        .fillMaxWidth()
                        .animateItem())
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MachineItem(machine: Machine, modifier: Modifier = Modifier) {
    val machinesViewModel = hiltViewModel<MachinesViewModel>()
    var menuVisible by remember { mutableStateOf(false) }

    ElevatedButton(
        onClick = {},
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(8.dp),
        modifier = modifier
            .height(64.dp)
    ) {
        Text(
            text = machine.alias,
            style = TextStyle(fontSize = 20.sp)
        )

        // spacer
        Spacer(Modifier.weight(1f))

        // actions
        Row {
            IconButton({ menuVisible = !menuVisible }) {
                Icon(Icons.Default.MoreVert, contentDescription = "")
            }

            DropdownMenu(menuVisible, onDismissRequest = { menuVisible = false }) {
                DropdownMenuItem(
                    text = { Text("정보") },
                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = "") },
                    onClick = {}
                )
                DropdownMenuItem(
                    text = { Text("머신 삭제") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "",
                            tint = Color.Red
                        )
                    },
                    onClick = {
                        machinesViewModel.machines -= machine
                    }
                )
            }
        }
    }
}