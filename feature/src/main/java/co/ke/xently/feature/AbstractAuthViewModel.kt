package co.ke.xently.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.repository.IAuthRepository
import co.ke.xently.feature.utils.flagLoadingOnStart
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class AbstractAuthViewModel(private val repository: IAuthRepository) : ViewModel() {
    val historicallyFirstUser = repository.historicallyFirstUser.shareIn(viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000))
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