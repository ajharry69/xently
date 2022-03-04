package co.ke.xently.accounts.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.feature.repository.IAuthRepository
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class ProfileViewModel @Inject constructor(repository: IAuthRepository) : ViewModel() {
    private val userId = MutableStateFlow<Long?>(null)
    fun setUserID(id: Long?) {
        userId.value = id
    }

    val result = userId.flatMapLatest(repository::getUser).flagLoadingOnStart().stateIn(
        scope = viewModelScope,
        initialValue = TaskResult.Loading,
        started = DEFAULT_SHARING_STARTED,
    )

    private val user = MutableSharedFlow<User>()

    val updateResult = user.flatMapLatest(repository::update).shareIn(
        scope = viewModelScope,
        started = DEFAULT_SHARING_STARTED,
    )

    fun update(user: User) {
        viewModelScope.launch {
            this@ProfileViewModel.user.emit(user)
        }
    }
}