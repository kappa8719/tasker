package kr.entropi.tasker.machine

import kotlinx.serialization.Serializable

@Serializable
sealed interface Machine {
    val alias: String
}