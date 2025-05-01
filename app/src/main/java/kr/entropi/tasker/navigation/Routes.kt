package kr.entropi.tasker.navigation

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import kr.entropi.tasker.R

@Serializable
data object TaskListEntry

@Serializable
data object SelectTaskTypeEntry

@Serializable
data class CreateTaskEntry(val type: CreateTaskType)

enum class CreateTaskType(@StringRes val displayNameId: Int) {
    RemoteExecution(R.string.tasks_type_remote_execution)
}

@Serializable
data object MachineListEntry

@Serializable
data object AddMachineEntry