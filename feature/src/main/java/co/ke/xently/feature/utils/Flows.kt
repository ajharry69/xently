package co.ke.xently.feature.utils

import co.ke.xently.data.TaskResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart

fun <T> Flow<TaskResult<T>>.flagLoadingOnStartCatchingErrors(): Flow<TaskResult<T>> {
    return onStart {
        emit(TaskResult.Loading)
    }.catch {
        emit(TaskResult.Error(it))
    }
}