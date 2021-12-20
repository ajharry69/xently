package co.ke.xently.common

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class Retry(
    private val number: Int = 3,
    private val backoffMultiplier: Int = 1,
    private val timeout: Duration = 3.seconds,
) {
    var currentAttemptCount = 1
        private set
    var currentWaitTimeSeconds = timeout.inWholeSeconds.toInt()
        private set

    val isDefaultState: Boolean
        get() = currentAttemptCount == 1 && currentWaitTimeSeconds == timeout.inWholeSeconds.toInt()

    suspend fun canRetry() = (currentAttemptCount <= number).also {
        if (!it) return@also // Avoid unnecessary thread-blocking
        delay(currentWaitTimeSeconds.seconds)
        currentWaitTimeSeconds += (currentWaitTimeSeconds * backoffMultiplier)
        currentAttemptCount++
    }
}
