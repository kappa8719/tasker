package kr.entropi.tasker.util

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class Snowflake(val value: Long) {
    override fun toString(): String {
        return value.toString()
    }
}