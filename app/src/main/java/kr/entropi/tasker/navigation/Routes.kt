package kr.entropi.tasker.navigation

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import kr.entropi.tasker.R

@Serializable
sealed interface Route

@Serializable
data object TaskListRoute : Route

@Serializable
data object SelectTaskTypeRoute : Route

@Serializable
data class CreateTaskRoute(val type: CreateTaskType) : Route

enum class CreateTaskType(@StringRes val displayNameId: Int) {
    RemoteExecution(R.string.tasks_type_remote_execution)
}

@Serializable
data object MachineListRoute : Route

@Serializable
data object AddMachineRoute : Route