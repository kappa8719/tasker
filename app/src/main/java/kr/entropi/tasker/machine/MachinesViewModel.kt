package kr.entropi.tasker.machine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kr.entropi.tasker.util.flow.throttle
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class MachinesViewModel @Inject constructor(
    private val machinesRepository: MachinesRepository
) : ViewModel() {
    val machines = machinesRepository.machines

    init {
        viewModelScope.launch {
            machinesRepository.machines.throttle(5.seconds)
                .collect { machinesRepository.storeToDisk() }
        }
    }
}