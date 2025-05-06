package kr.entropi.tasker.task

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kr.entropi.tasker.machine.MachinesModule
import kr.entropi.tasker.machine.ShellMachine
import kr.entropi.tasker.util.Snowflake
import kr.entropi.tasker.util.Snowflakes

@Serializable
data class RemoteExecutionTask(
    override val id: Snowflake = Snowflakes.nextId(),
    override val name: String,
    override val askBeforeRun: Boolean = false,
    val script: String,
    val machine: Snowflake,
    val executeWithSudo: Boolean = false
) : Task() {
    override val dependsOnMachines = setOf(machine)

    @Transient
    val escapedScript = script.trimIndent().replace("\"", "\\\"")

    override fun execute(context: Context): String {
        val machines = MachinesModule.provideMachinesRepository(context).machines
        val machine = machines.find { it.id == machine }

        if (machine !is ShellMachine) {
            return "invalid machine ${this.machine}"
        }

        val result =
            machine.executeCommandOnce("sh -c \"${escapedScript}\"", sudo = executeWithSudo)
        return result.prettier()
    }

    private fun ShellMachine.CommandExecuteResult.prettier(): String {
        var indentLevel = 0
        val indentWidth = 4

        fun padding() = "".padStart(indentLevel * indentWidth)

        val toString = toString()

        val stringBuilder = StringBuilder(toString.length)

        var i = 0
        while (i < toString.length) {
            when (val char = toString[i]) {
                '(', '[', '{' -> {
                    indentLevel++
                    stringBuilder.appendLine(char).append(padding())
                }

                ')', ']', '}' -> {
                    indentLevel--
                    stringBuilder.appendLine().append(padding()).append(char)
                }

                ',' -> {
                    stringBuilder.appendLine(char).append(padding())
                    // ignore space after comma as we have added a newline
                    val nextChar = toString.getOrElse(i + 1) { char }
                    if (nextChar == ' ') i++
                }

                else -> {
                    stringBuilder.append(char)
                }
            }
            i++
        }

        return stringBuilder.toString()
    }
}