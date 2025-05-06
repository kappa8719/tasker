package kr.entropi.tasker.util

object Snowflakes {
    private val generator = SnowflakeIdGenerator()
    val nodeId get() = generator.nodeId
    val epoch get() = generator.epoch

    fun nextId() = generator.nextId()
    fun parse(id: Snowflake) = generator.parse(id)
}