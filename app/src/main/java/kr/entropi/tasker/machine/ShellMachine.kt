package kr.entropi.tasker.machine

import kotlinx.serialization.Serializable

@Serializable
class ShellMachine(
    val host: String,
    val user: String,
    val password: String
)