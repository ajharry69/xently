package co.ke.xently.accounts.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.User
import co.ke.xently.feature.repository.IAuthRepository
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class ProfileViewModel @Inject constructor(repository: IAuthRepository) : ViewModel() {
    private val userId = MutableSharedFlow<Long?>()
    val fetchResult = userId.flatMapLatest {
        repository.getUser(it).flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    private val user = MutableSharedFlow<User>()
    val updateResult = user.flatMapLatest {
        repository.update(it).flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun setUserId(id: Long?) {
        viewModelScope.launch {
            userId.emit(id)
        }
    }

    fun update(user: User) {
        viewModelScope.launch {
            this@ProfileViewModel.user.emit(user)
        }
    }
}