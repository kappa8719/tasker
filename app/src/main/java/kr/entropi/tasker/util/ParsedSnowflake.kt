package kr.entropi.tasker.util

/**
 * Parsed snowflake data
 */
data class ParsedSnowflake(
    val timestamp: Long,
    val nodeId: Int,
    val sequence: Int
)
