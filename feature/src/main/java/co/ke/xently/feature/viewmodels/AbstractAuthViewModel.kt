package co.ke.xently.feature.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.feature.repository.IAuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

abstract class AbstractAuthViewModel(repository: IAuthRepository) : ViewModel() {
    val currentlyActiveUser = repository.currentlyActiveUser.shareIn(
        replay = 1,
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
    )
}