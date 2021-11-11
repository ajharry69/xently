package co.ke.xently.feature

import co.ke.xently.common.Retry
import kotlinx.coroutines.flow.MutableStateFlow

interface IRetryViewModel {

    val remote: MutableStateFlow<Boolean>
        get() = MutableStateFlow(false)

    fun shouldLoadRemote(remote: Boolean) {
        this.remote.value = remote
    }

    suspend fun Retry.signalLoadFromCache(): Retry {
        if (canRetry()) {
            shouldLoadRemote(false)
        } else if (!isDefaultState) {
            return copy()
        }
        return this
    }
}