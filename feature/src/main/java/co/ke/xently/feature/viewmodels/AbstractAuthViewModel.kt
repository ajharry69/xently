package co.ke.xently.feature.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.repository.IAuthRepository
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

abstract class AbstractAuthViewModel(private val repository: IAuthRepository) : ViewModel() {
    val currentlyActiveUser = repository.currentlyActiveUser.shareIn(
        replay = 1,
        scope = viewModelScope,
        started = DEFAULT_SHARING_STARTED,
    )
    private val _signOutResult = MutableSharedFlow<TaskResult<Unit>>()
    val signOutResult = _signOutResult.asSharedFlow()

    fun signOut() {
        viewModelScope.launch {
            repository.signOut().flagLoadingOnStart().collectLatest {
                _signOutResult.emit(it)
            }
        }
    }
}