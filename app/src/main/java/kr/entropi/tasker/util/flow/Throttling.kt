package kr.entropi.tasker.util.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration


/**
 * Throttle flow for given duration
 */
fun <T> Flow<T>.throttleFirst(windowDuration: Duration): Flow<T> = flow {
    var lastEmissionTime = Instant.fromEpochMilliseconds(0)
    collect { value ->
        val currentTime = Clock.System.now()
        if (currentTime - lastEmissionTime >= windowDuration) {
            emit(value)
            lastEmissionTime = currentTime
        }
    }
}

fun <T> Flow<T>.throttle(duration: Duration): Flow<T> = channelFlow {
    val buffer = mutableListOf<T>()
    var isCooldown = false

    collect { value ->
        if (!isCooldown) {
            // Process first item immediately
            send(value)
            isCooldown = true
            launch {
                delay(duration) // cooldown
                // Process buffered items after delay
                buffer.forEach { send(it) }
                buffer.clear()
                isCooldown = false
            }
        } else {
            // Buffer subsequent items during cooldown
            buffer.add(value)
        }
    }
}