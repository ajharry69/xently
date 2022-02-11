package co.ke.xently.feature.utils

import android.util.Log
import co.ke.xently.common.TAG
import co.ke.xently.data.TaskResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart

fun <T> Flow<TaskResult<T>>.flagLoadingOnStartCatchingErrors(logTag: String = TAG): Flow<TaskResult<T>> {
    return onStart {
        emit(TaskResult.Loading)
    }.catch {
        Log.e(logTag, "flagLoadingOnStartCatchingErrors: ${it.message}", it)
        emit(TaskResult.Error(it))
    }
}

fun MutableStateFlow<String>.setCleansedQuery(query: String) {
    query.trim().also {
        if (it.isNotBlank()) {
            this.value = query
        }
    }
}
