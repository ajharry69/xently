package co.ke.xently.feature.repository

import androidx.core.content.edit
import co.ke.xently.common.Retry
import co.ke.xently.common.TOKEN_VALUE_SHARED_PREFERENCE_KEY
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.data.getOrNull
import co.ke.xently.source.remote.retryCatch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach


fun Flow<TaskResult<User>>.doTaskWhileSavingEachLocally(
    retry: Retry,
    dependencies: Dependencies,
) = onEach { result ->
    result.getOrNull()?.also {
        dependencies.database.accountDao.apply {
            save(it.copy(isActive = true))
            makeInactiveExcept(userId = it.id)
        }
        if (it.token != null) {
            dependencies.preference.encrypted.edit(commit = true) {
                putString(TOKEN_VALUE_SHARED_PREFERENCE_KEY, it.token)
            }
        }
    }
}.retryCatch(retry).flowOn(dependencies.dispatcher.io)