package kr.entropi.tasker.task

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kr.entropi.tasker.util.Snowflake
import kr.entropi.tasker.util.flow.MutableSetStateFlow
import java.io.File
import javax.inject.Singleton

@Singleton
class TasksRepository(applicationContext: Context) {
    private val directory = File(applicationContext.filesDir, "tasks")

    private val _markedForRemove = mutableSetOf<Snowflake>()
    private val _tasks = MutableStateFlow(setOf<Task>())
    val tasks = MutableSetStateFlow(
        _tasks,
        onAdd = { _markedForRemove -= it.id },
        onRemove = { _markedForRemove += it.id }
    )

    init {
        directory.mkdirs()
        readFromDisk()
    }

    private fun getFile(id: Snowflake) = File(directory, "${id}.json")

    @OptIn(ExperimentalSerializationApi::class)
    fun readFromDisk(): Boolean {
        try {
            _tasks.update {
                (directory.listFiles { it.extension == "json" } ?: return false).map { file ->
                    Json.decodeFromStream<Task>(file.inputStream())
                }.toSet()
            }
        } catch (e: Throwable) {
            Log.e(this::class.simpleName, "Failed to read tasks from disk", e)
        }

        return true
    }

    fun storeToDisk() {
        try {
            directory.mkdirs()
            _markedForRemove.forEach {
                getFile(it).delete()
            }
            tasks.forEach { task ->
                val file = File(directory, "${task.id}.json")
                val string = Json.encodeToString(task)
                file.writeText(string)
            }
        } catch (e: Throwable) {
            Log.e(this::class.simpleName, "Failed to store tasks to disk", e)
        }
    }
}