package kr.entropi.tasker.machine

import androidx.compose.animation.core.FastOutSlowInEasing
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import kotlinx.serialization.Serializable
import kr.entropi.tasker.util.Snowflake
import kr.entropi.tasker.util.Snowflakes
import java.net.SocketException
import kotlin.time.Duration

@Serializable
data class ShellMachine(
    override val id: Snowflake = Snowflakes.nextId(),
    override val alias: String,
    val host: String,
    val port: UShort,
    val user: String,
    val password: String
) : Machine {
    fun executeCommandOnce(
        command: String,
        sudo: Boolean = false,
        timeout: Duration = Duration.ZERO
    ): CommandExecuteResult {
        try {
            val jsch = JSch()
            val session = jsch.getSession(user, host, port.toInt())
            session.setPassword(password)
            session.setConfig("StrictHostKeyChecking", "no")

            session.connect(timeout.inWholeMilliseconds.toInt())
            val channel = session.openChannel("exec") as ChannelExec

            if (sudo) {
                channel.setCommand("sudo -S -p '' $command")
            } else {
                channel.setCommand(command)
            }

            val inputStream = channel.inputStream
            val outputStream = channel.outputStream
            val errStream = channel.errStream
            channel.connect()

            FastOutSlowInEasing

            if (sudo) {
                outputStream.write("${password}\n".toByteArray())
                outputStream.flush()
            }

            val tmp = ByteArray(1024)
            val stdoutBuffer = StringBuffer()
            val stderrBuffer = StringBuffer()
            while (true) {
                while (inputStream.available() > 0) {
                    val i = inputStream.read(tmp, 0, 1024)
                    if (i < 0) break
                    stdoutBuffer.append(String(tmp, 0, i))
                }
                while (errStream.available() > 0) {
                    val i = errStream.read(tmp, 0, 1024)
                    if (i < 0) break
                    stderrBuffer.append(String(tmp, 0, i))
                }

                if (channel.isClosed) {
                    if (inputStream.available() > 0) continue
                    break
                }
            }

            channel.disconnect()
            session.disconnect()

            return CommandExecuteResult.Exited(
                stdout = stdoutBuffer.toString(),
                stderr = stderrBuffer.toString(),
                code = channel.exitStatus
            )
        } catch (e: Throwable) {
            return when (e) {
                is SocketException -> CommandExecuteResult.SocketFailed(e)
                is JSchException -> CommandExecuteResult.ShellFailed(e)
                else -> CommandExecuteResult.Unknown
            }
        }
    }

    sealed interface CommandExecuteResult {
        data class SocketFailed(val exception: SocketException) : CommandExecuteResult
        data class ShellFailed(val exception: JSchException) : CommandExecuteResult
        data object Unknown : CommandExecuteResult
        data class Exited(val stdout: String, val stderr: String, val code: Int) :
            CommandExecuteResult
    }
}