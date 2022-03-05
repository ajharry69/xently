package co.ke.xently.feature.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.feature.repository.IAuthRepository
import co.ke.xently.feature.utils.flagLoadingOnStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

abstract class AbstractAuthViewModel(private val repository: IAuthRepository) : ViewModel() {
    val currentlyActiveUser = repository.currentlyActiveUser.shareIn(
        replay = 1,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
    )
    private val signout = MutableSharedFlow<Boolean>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val signOutResult = signout.flatMapLatest {
        repository.signOut().flagLoadingOnStart()
    }.shareIn(viewModelScope, SharingStarted.Lazily)

    fun signOut() {
        viewModelScope.launch {
            signout.emit(true)
        }
    }
}