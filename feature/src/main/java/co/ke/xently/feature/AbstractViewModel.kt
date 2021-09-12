package co.ke.xently.feature

import androidx.lifecycle.ViewModel
import co.ke.xently.common.Retry
import kotlinx.coroutines.flow.MutableStateFlow

abstract class AbstractViewModel : ViewModel() {

    protected val remote = MutableStateFlow(false)

    fun shouldLoadRemote(remote: Boolean) {
        this.remote.value = remote
    }

    protected suspend fun Retry.ddddd(): Retry {
        if (canRetry()) {
            shouldLoadRemote(false)
        } else if (!isDefaultState) {
            return copy()
        }
        return this
    }
}