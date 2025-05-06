package kr.entropi.tasker.navigation

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import kr.entropi.tasker.R
import kr.entropi.tasker.task.RemoteExecutionTask
import kr.entropi.tasker.task.Task
import kr.entropi.tasker.util.Snowflake

@Serializable
sealed interface Route

@Serializable
data object TaskListRoute : Route

@Serializable
data object SelectTaskTypeRoute : Route

@Serializable
data class UpdateTaskRoute(val type: UpdateTaskType, private val updateTargetRawId: Long? = null) :
    Route {
    val isCreate get() = updateTargetRawId == null
    val updateTarget get() = updateTargetRawId?.let { Snowflake(it) }

    enum class UpdateTaskType(@StringRes val displayNameId: Int) {
        RemoteExecution(R.string.tasks_type_remote_execution);

        companion object {
            fun fromTask(task: Task): UpdateTaskType {
                return when (task) {
                    is RemoteExecutionTask -> RemoteExecution
                }
            }
        }
    }
}

@Serializable
data object MachineListRoute : Route

@Serializable
data object AddMachineRoute : Route