package co.ke.xently.accounts.ui.signup

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
internal class SignUpViewModel @Inject constructor(
    private val repository: IAccountRepository,
) : ViewModel() {
    private val _signUpResult: MutableStateFlow<TaskResult<User>> =
        MutableStateFlow(TaskResult.Loading)
    val signUpResult: StateFlow<TaskResult<User>>
        get() = _signUpResult

    fun signUp(user: User) {
        viewModelScope.launch {
            repository.signUp(user)
                .flagLoadingOnStartCatchingErrors().collectLatest {
                    _signUpResult.value = it
                }
        }
    }
}