package co.ke.xently.accounts.ui.verification

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
internal class VerificationViewModel @Inject constructor(
    private val repository: IAccountRepository,
) : ViewModel() {
    private val _taskResult = MutableStateFlow<TaskResult<User?>>(TaskResult.Success(null))
    val taskResult: StateFlow<TaskResult<User?>>
        get() = _taskResult

    fun verifyAccount(code: String) {
        viewModelScope.launch {
            repository.verifyAccount(code)
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _taskResult.value = it
                }
        }
    }

    fun resendVerificationCode() {
        viewModelScope.launch {
            repository.requestVerificationCode()
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _taskResult.value = it
                }
        }
    }
}