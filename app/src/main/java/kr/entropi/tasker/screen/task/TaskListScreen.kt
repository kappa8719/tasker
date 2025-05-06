package kr.entropi.tasker.screen.task

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.entropi.tasker.R
import kr.entropi.tasker.navigation.LocalNavController
import kr.entropi.tasker.navigation.SelectTaskTypeRoute
import kr.entropi.tasker.navigation.UpdateTaskRoute
import kr.entropi.tasker.task.Task
import kr.entropi.tasker.task.TasksViewModel
import kr.entropi.tasker.util.Snowflake
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor() : ViewModel() {
    val selection = mutableStateSetOf<Snowflake>()
}

/**
 * @see kr.entropi.tasker.navigation.TaskListRoute
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen() {
    val navController = LocalNavController.current
    val tasksViewModel = hiltViewModel<TasksViewModel>()
    val viewModel = hiltViewModel<TaskListViewModel>()

    Scaffold(
        modifier = Modifier.fillMaxSize(), floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate(SelectTaskTypeRoute)
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
                visible = viewModel.selection.isEmpty(),
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
                visible = viewModel.selection.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                TopAppBar(title = { Text("선택") }, navigationIcon = {
                    Checkbox(
                        viewModel.selection.containsAll(tasksViewModel.tasks.map { it.id }),
                        {
                            if (viewModel.selection.containsAll(tasksViewModel.tasks.map { it.id }))
                                viewModel.selection.clear()
                            else
                                viewModel.selection.addAll(tasksViewModel.tasks.map { it.id })
                        }
                    )
                })
            }

            TaskList()
        }
    }
}


@Composable
fun TaskList(modifier: Modifier = Modifier) {
    val tasksViewModel = hiltViewModel<TasksViewModel>()
    val tasks by tasksViewModel.tasks.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tasks.toList(), key = { it.id.value }) {
            TaskElement(
                Modifier
                    .fillMaxWidth()
                    .animateItem(),
                task = it
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TaskElement(modifier: Modifier = Modifier, task: Task) {
    val tasksViewModel = hiltViewModel<TasksViewModel>()
    val viewModel = hiltViewModel<TaskListViewModel>()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope { Dispatchers.IO }

    val shape = RoundedCornerShape(12.dp)

    var menuVisible by remember { mutableStateOf(false) }

    val isSelected = task.id in viewModel.selection
    fun setSelected(selected: Boolean) {
        if (selected) {
            viewModel.selection += task.id
        } else {
            viewModel.selection -= task.id
        }
    }

    var isExecuting by remember { mutableStateOf(false) }
    var isAskForRunDialogOpen by remember { mutableStateOf(false) }
    var taskExecuteResult by remember { mutableStateOf<Any?>(null) }

    if (taskExecuteResult != null) {
        AlertDialog(
            onDismissRequest = { taskExecuteResult = null },
            confirmButton = {
                ElevatedButton({ taskExecuteResult = null }) {
                    Text("완료")
                }
            },
            icon = { Icon(Icons.Default.Check, null) },
            title = { Text("작업 실행 완료") },
            text = {
                Box(Modifier.fillMaxWidth()) {
                    Text("결과:\n${taskExecuteResult}")
                }
            }
        )
    }

    if (isAskForRunDialogOpen) {
        AlertDialog(
            onDismissRequest = { isAskForRunDialogOpen = false },
            confirmButton = {
                ElevatedButton({
                    isAskForRunDialogOpen = false
                    isExecuting = true
                    coroutineScope.launch {
                        taskExecuteResult = task.execute(context)
                        isExecuting = false
                    }
                }) { Text("진행") }
            },
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("정말로 실행하시겠습니까?") },
            text = { Text("진행하면 작업을 즉시 실행합니다.") }
        )
    }

    ElevatedButton(
        onClick = {
            if (viewModel.selection.isNotEmpty()) {
                setSelected(!isSelected)
                return@ElevatedButton
            }

            // ask for run
            if (task.askBeforeRun) {
                isAskForRunDialogOpen = true
                return@ElevatedButton
            }

            // execute
            isExecuting = true
            coroutineScope.launch {
                taskExecuteResult = task.execute(context)
                isExecuting = false
            }
        },
        enabled = !isExecuting,
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
                visible = viewModel.selection.isNotEmpty(),
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                Checkbox(
                    checked = task.id in viewModel.selection,
                    onCheckedChange = {
                        setSelected(!isSelected)
                    },
                )
            }

            AnimatedVisibility(isExecuting) {
                CircularProgressIndicator()
            }

            // task name
            AnimatedVisibility(
                viewModel.selection.isEmpty(),
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                Spacer(Modifier.width(16.dp))
            }
            Text(
                text = task.name,
                style = TextStyle(fontSize = 20.sp)
            )

            // spacer
            Spacer(Modifier.weight(1f))

            Row {
                Text(
                    "${task.id}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
                )
            }

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
                        onClick = {
                            menuVisible = false
                            navController.navigate(
                                UpdateTaskRoute(
                                    type = UpdateTaskRoute.UpdateTaskType.fromTask(
                                        task
                                    ),
                                    updateTargetRawId = task.id.value
                                )
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("작업 삭제") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "",
                                tint = Color.Red
                            )
                        },
                        onClick = {
                            tasksViewModel.tasks -= task
                        }
                    )
                }
            }
        }
    }
}