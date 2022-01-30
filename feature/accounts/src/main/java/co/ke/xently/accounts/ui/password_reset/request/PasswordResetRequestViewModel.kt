package co.ke.xently.accounts.ui.password_reset.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.accounts.repository.IAccountRepository
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.feature.utils.flagLoadingOnStartCatchingErrors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PasswordResetRequestViewModel @Inject constructor(
    private val repository: IAccountRepository,
) : ViewModel() {
    private val _taskResult = MutableStateFlow<TaskResult<User?>>(TaskResult.Success(null))
    val taskResult: StateFlow<TaskResult<User?>>
        get() = _taskResult

    fun requestTemporaryPassword(email: String) {
        viewModelScope.launch {
            repository.requestTemporaryPassword(email)
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _taskResult.value = it
                }
        }
    }
}