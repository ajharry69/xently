package co.ke.xently

import androidx.lifecycle.viewModelScope
import co.ke.xently.feature.repository.IAuthRepository
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.feature.viewmodels.AbstractAuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository: IAuthRepository,
) : AbstractAuthViewModel(repository = repository) {
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