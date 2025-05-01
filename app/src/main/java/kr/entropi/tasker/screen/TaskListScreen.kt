package kr.entropi.tasker.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.entropi.tasker.R
import kr.entropi.tasker.navigation.LocalNavController
import kr.entropi.tasker.navigation.SelectTaskTypeEntry
import kr.entropi.tasker.task.RemoteExecutionTask
import kr.entropi.tasker.task.Task

class TaskListContext {
    val list = mutableStateListOf<String>()
}

private val LocalTaskListScreen = compositionLocalOf<TaskListContext> {
    error("No LocalMainScreen provided")
}

/**
 * @see kr.entropi.tasker.navigation.TaskListEntry
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen() {
    val navController = LocalNavController.current
    val mainScreen = remember { TaskListContext() }

    CompositionLocalProvider(LocalTaskListScreen provides mainScreen) {
        Scaffold(
            modifier = Modifier.fillMaxSize(), floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        navController.navigate(SelectTaskTypeEntry)
                    },
                    icon = { Icon(Icons.Filled.Edit, "Create task icon") },
                    text = { Text(stringResource(R.string.tasks_create_fab)) },
                )
            }) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(0.dp)
            ) {
                AnimatedVisibility(
                    visible = mainScreen.list.isEmpty(),
                    enter = slideInVertically() + expandVertically() + fadeIn(),
                    exit = slideOutVertically() + shrinkVertically() + fadeOut()
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(padding.calculateTopPadding())
                    )
                }

                AnimatedVisibility(
                    visible = mainScreen.list.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    TopAppBar(title = { Text("선택") }, navigationIcon = {
                        Checkbox(true, {})
                    })
                }

                TaskList()
            }
        }
    }
}


@Composable
fun TaskList(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TaskElement(Modifier.fillMaxWidth(), task = RemoteExecutionTask(script = "", id = "task1"))
        TaskElement(Modifier.fillMaxWidth(), task = RemoteExecutionTask(script = "", id = "task2"))
        TaskElement(Modifier.fillMaxWidth(), task = RemoteExecutionTask(script = "", id = "task3"))
        TaskElement(Modifier.fillMaxWidth(), task = RemoteExecutionTask(script = "", id = "task4"))
        TaskElement(Modifier.fillMaxWidth(), task = RemoteExecutionTask(script = "", id = "task5"))
        TaskElement(Modifier.fillMaxWidth(), task = RemoteExecutionTask(script = "", id = "task1"))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskElement(modifier: Modifier = Modifier, task: Task) {
    val mainScreen = LocalTaskListScreen.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(12.dp)

    var menuVisible by remember { mutableStateOf(false) }

    val isSelected = task.id in mainScreen.list
    fun setSelected(selected: Boolean) {
        if (selected) {
            mainScreen.list += task.id
        } else {
            mainScreen.list -= task.id
        }
    }

    ElevatedButton(
        onClick = {
            if(mainScreen.list.isNotEmpty()) {
                setSelected(!isSelected)
            }
        },
        shape = shape,
        contentPadding = PaddingValues(8.dp),
        modifier = modifier
            .height(64.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        setSelected(!isSelected)
                    }
                )
            }
    ) {
        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // select checkbox
            AnimatedVisibility(
                visible = mainScreen.list.isNotEmpty(),
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                Checkbox(
                    checked = task.id in mainScreen.list,
                    onCheckedChange = {
                        setSelected(!isSelected)
                    },
                )
            }

            // task name
            AnimatedVisibility(
                mainScreen.list.isEmpty(),
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                Spacer(Modifier.width(16.dp))
            }
            Text(
                text = "asdf",
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
                        text = { Text("선택") },
                        leadingIcon = { Icon(Icons.Outlined.CheckCircle, contentDescription = "") },
                        onClick = {
                            setSelected(!isSelected)
                            menuVisible = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("편집") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = "") },
                        onClick = {}
                    )
                    DropdownMenuItem(
                        text = { Text("작업 삭제") },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = "", tint = Color.Red) },
                        onClick = {}
                    )
                }
            }
        }
    }
}