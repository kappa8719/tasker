package kr.entropi.tasker.machine

import kotlinx.serialization.Serializable

@Serializable
class ShellMachine(
    override val alias: String,
    val host: String,
    val port: UShort,
    val user: String,
    val password: String
) : Machine