package co.ke.xently.feature.utils

import co.ke.xently.data.TaskResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart

val DEFAULT_SHARING_STARTED = SharingStarted.WhileSubscribed(replayExpirationMillis = 5000)

fun <T> Flow<TaskResult<T>>.flagLoadingOnStart() = onStart {
    emit(TaskResult.Loading)
}
