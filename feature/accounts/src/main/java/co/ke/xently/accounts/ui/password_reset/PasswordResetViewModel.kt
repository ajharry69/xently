package co.ke.xently.accounts.ui.password_reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.accounts.repository.IAccountRepository
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.feature.utils.flagLoadingOnStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PasswordResetViewModel @Inject constructor(
    private val repository: IAccountRepository,
) : ViewModel() {
    private val _taskResult = MutableStateFlow<TaskResult<User?>>(TaskResult.Success(null))
    val taskResult: StateFlow<TaskResult<User?>>
        get() = _taskResult

    fun resetPassword(resetPassword: User.ResetPassword) {
        viewModelScope.launch {
            repository.resetPassword(resetPassword)
                .flagLoadingOnStart()
                .collectLatest {
                    _taskResult.value = it
                }
        }
    }
}