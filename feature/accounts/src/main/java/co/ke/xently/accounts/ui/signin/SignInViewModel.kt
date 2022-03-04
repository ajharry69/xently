package co.ke.xently.accounts.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.accounts.repository.IAccountRepository
import co.ke.xently.data.User
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import okhttp3.Credentials
import javax.inject.Inject

@HiltViewModel
internal class SignInViewModel @Inject constructor(
    private val repository: IAccountRepository,
) : ViewModel() {
    private val auth = MutableSharedFlow<User.BasicAuth>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val result = auth.flatMapLatest {
        repository.signIn(Credentials.basic(it.username, it.password)).flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun signIn(auth: User.BasicAuth) {
        viewModelScope.launch {
            this@SignInViewModel.auth.emit(auth)
        }
    }
}