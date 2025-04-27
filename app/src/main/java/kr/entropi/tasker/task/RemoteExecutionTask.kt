package kr.entropi.tasker.task

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.serialization.Serializable
import kr.entropi.tasker.proto.RemoteExecutionTaskData
import java.io.InputStream
import java.io.OutputStream

@Serializable
class RemoteExecutionTask(
    val script: String,
    override val id: String
) : Task() {
    companion object {
        fun fromData(data: RemoteExecutionTaskData): RemoteExecutionTask {
            return RemoteExecutionTask(
                script = data.script,
                id = data.id
            )
        }
    }

    fun toData(): RemoteExecutionTask {
        return RemoteExecutionTask(script = this.script, id = this.id)
    }
}

object RemoteExecutionTaskDataSerializer: Serializer<RemoteExecutionTaskData> {
    override val defaultValue: RemoteExecutionTaskData = RemoteExecutionTaskData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): RemoteExecutionTaskData {
        try {
            return RemoteExecutionTaskData.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: RemoteExecutionTaskData, output: OutputStream) = t.writeTo(output)
}