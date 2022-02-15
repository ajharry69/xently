package co.ke.xently.accounts.ui.signin

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
import okhttp3.Credentials
import javax.inject.Inject

@HiltViewModel
internal class SignInViewModel @Inject constructor(
    private val repository: IAccountRepository,
) : ViewModel() {
    private val _signInResult: MutableStateFlow<TaskResult<User?>> =
        MutableStateFlow(TaskResult.Success(null))
    val signInResult: StateFlow<TaskResult<User?>>
        get() = _signInResult

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            repository.signIn(Credentials.basic(username, password))
                .flagLoadingOnStart()
                .collectLatest {
                    _signInResult.value = it
                }
        }
    }
}