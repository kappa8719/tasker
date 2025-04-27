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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kr.entropi.tasker.task.RemoteExecutionTask
import kr.entropi.tasker.task.Task

class MainScreenContext {
    val list = mutableStateListOf<String>()
}

private val LocalMainScreen = compositionLocalOf<MainScreenContext> {
    error("No LocalMainScreen provided")
}

/**
 * @see kr.entropi.tasker.navigation.MainEntry
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val mainScreen = remember { MainScreenContext() }

    CompositionLocalProvider(LocalMainScreen provides mainScreen) {
        Scaffold(
            modifier = Modifier.fillMaxSize(), floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { },
                    icon = { Icon(Icons.Filled.Edit, "Create task icon") },
                    text = { Text(text = "Create Task") },
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
    val mainScreen = LocalMainScreen.current
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(12.dp)

    val isSelected = task.id in mainScreen.list
    fun setSelected(selected: Boolean) {
        if (selected) {
            mainScreen.list += task.id
        } else {
            mainScreen.list -= task.id
        }
    }

    Surface(
        modifier = modifier
            .semantics { role = Role.Button }
            .heightIn(min = 50.dp)
            .clip(shape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = {
                    if (mainScreen.list.isNotEmpty()) {
                        setSelected(!isSelected)
                    }
                },
                onLongClick = {
                    setSelected(!isSelected)
                }), shape = shape, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(8.dp, 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
            AnimatedVisibility(
                mainScreen.list.isEmpty(),
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                Spacer(Modifier.width(16.dp))
            }
            Text(
                text = "asdf"
            )
        }
    }
}