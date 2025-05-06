package kr.entropi.tasker.screen.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kr.entropi.tasker.R
import kr.entropi.tasker.machine.MachinesViewModel
import kr.entropi.tasker.navigation.AddMachineRoute
import kr.entropi.tasker.navigation.LocalNavController
import kr.entropi.tasker.navigation.SelectTaskTypeRoute
import kr.entropi.tasker.navigation.UpdateTaskRoute
import kr.entropi.tasker.task.RemoteExecutionTask
import kr.entropi.tasker.task.TasksViewModel
import kr.entropi.tasker.ui.foundation.LabeledCheckbox
import kr.entropi.tasker.ui.navigation.BackButton
import kr.entropi.tasker.util.Snowflake

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTaskScreen(route: UpdateTaskRoute) {
    val navController = LocalNavController.current
    val machinesViewModel = hiltViewModel<MachinesViewModel>()
    val tasksViewModel = hiltViewModel<TasksViewModel>()

    val updateTarget = tasksViewModel.tasks.find { it.id == route.updateTarget }
    val type = route.type

    var askBeforeRun by remember { mutableStateOf(updateTarget?.askBeforeRun == true) }
    var executeWithSudo by remember { mutableStateOf((updateTarget as? RemoteExecutionTask)?.executeWithSudo == true) }
    var machine by remember { mutableStateOf<Snowflake?>((updateTarget as? RemoteExecutionTask)?.machine) }
    var scriptFieldState =
        rememberTextFieldState((updateTarget as? RemoteExecutionTask)?.script ?: "")
    var nameFieldState = rememberTextFieldState(updateTarget?.name ?: "")

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(1.dp),
                title = {
                    Text(
                        stringResource(
                            R.string.tasks_create_type,
                            stringResource(type.displayNameId)
                        )
                    )
                },
                navigationIcon = {
                    BackButton()
                }
            )
        },
        bottomBar = {
            HorizontalDivider()
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 8.dp, 8.dp, 20.dp), horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = {
                        if (updateTarget == null) {
                            machine?.let { machine ->
                                tasksViewModel.tasks += RemoteExecutionTask(
                                    name = nameFieldState.text.toString(),
                                    askBeforeRun = askBeforeRun,
                                    script = scriptFieldState.text.toString(),
                                    machine = machine,
                                    executeWithSudo = executeWithSudo,
                                )

                                navController.popBackStack(SelectTaskTypeRoute, true)
                            }
                        } else {
                            machine?.let { machine ->
                                tasksViewModel.tasks -= updateTarget
                                tasksViewModel.tasks += RemoteExecutionTask(
                                    id = updateTarget.id,
                                    name = nameFieldState.text.toString(),
                                    askBeforeRun = askBeforeRun,
                                    script = scriptFieldState.text.toString(),
                                    machine = machine,
                                    executeWithSudo = executeWithSudo,
                                )

                                navController.popBackStack()
                            }
                        }
                    },
                    enabled = nameFieldState.text.isNotBlank() && machine != null
                ) {
                    Text(if (route.isCreate) "Create task" else "Update task")

                    Spacer(Modifier.width(8.dp))

                    Icon(
                        if (route.isCreate) Icons.Default.AddCircle else Icons.Default.Edit,
                        null
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                state = nameFieldState,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") })
            LabeledCheckbox(
                checked = askBeforeRun,
                onCheckChanged = { askBeforeRun = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ask before run") }
            )

            HorizontalDivider()

            MachineSelectField(machine) { machine = it }
            LabeledCheckbox(
                checked = executeWithSudo,
                onCheckChanged = { executeWithSudo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Execute with sudo") }
            )
            OutlinedTextField(
                state = scriptFieldState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = { Text("Script") })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineSelectField(
    selected: Snowflake?,
    onSelectChange: (Snowflake) -> Unit
) {
    val navController = LocalNavController.current
    val machinesViewModel = hiltViewModel<MachinesViewModel>()
    val focusManager = LocalFocusManager.current

    var isOpen by remember { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState("")

    LaunchedEffect(selected) {
        val text = machinesViewModel.machines.find { it.id == selected }?.alias ?: ""
        textFieldState.setTextAndPlaceCursorAtEnd(text)
    }

    LaunchedEffect(isOpen) {
        if (!isOpen) {
            focusManager.clearFocus()
        }
    }

    ExposedDropdownMenuBox(
        expanded = isOpen,
        onExpandedChange = { isOpen = it },
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            state = textFieldState,
            readOnly = true,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text("Target Machine") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isOpen) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )

        ExposedDropdownMenu(
            expanded = isOpen,
            onDismissRequest = { isOpen = false },
        ) {
            DropdownMenuItem(
                onClick = {
                    isOpen = false
                    navController.navigate(AddMachineRoute)
                },
                text = { Text("Add machine") },
                leadingIcon = { Icon(Icons.Default.AddCircleOutline, null) },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
            )
            machinesViewModel.machines.sortedByDescending { it.id.value }
                .forEach { machine ->
                    DropdownMenuItem(
                        text = {
                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    machine.alias,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(Modifier.weight(1f))

                                Text(
                                    machine.id.toString(),
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.5f
                                        )
                                    )
                                )
                            }
                        },
                        onClick = {
                            onSelectChange(machine.id)
                            isOpen = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
        }
    }
}