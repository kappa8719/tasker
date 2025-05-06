package kr.entropi.tasker.machine

import kotlinx.serialization.Serializable
import kr.entropi.tasker.util.Snowflake

@Serializable
sealed interface Machine {
    val id: Snowflake
    val alias: String
}