package kr.entropi.tasker.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kr.entropi.tasker.util.flow.throttle
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val tasksRepository: TasksRepository
) : ViewModel() {
    val tasks = tasksRepository.tasks

    init {
        viewModelScope.launch {
            tasksRepository.tasks.throttle(5.seconds)
                .collect { tasksRepository.storeToDisk() }
        }
    }
}