package kr.entropi.tasker.navigation

import kotlinx.serialization.Serializable

@Serializable
object MainEntry

@Serializable
object SelectTaskTypeEntry

@Serializable
data class CreateTaskEntry(val type: CreateTaskType)

enum class CreateTaskType {
    RemoteExecution
}