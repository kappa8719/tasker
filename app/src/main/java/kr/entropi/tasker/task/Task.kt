package kr.entropi.tasker.task

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kr.entropi.tasker.util.Snowflake

@Serializable
sealed class Task() {
    abstract val id: Snowflake
    abstract val name: String
    abstract val askBeforeRun: Boolean

    @Transient
    open val dependsOnMachines: Set<Snowflake> = emptySet()

    abstract fun execute(context: Context): Any?
}