package kr.entropi.tasker.machine

import androidx.compose.runtime.mutableStateSetOf
import javax.inject.Singleton

@Singleton
class MachinesRepository {
    val machines = mutableStateSetOf<Machine>()
}