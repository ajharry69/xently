package co.ke.xently.accounts.ui.password_reset

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
import javax.inject.Inject

@HiltViewModel
internal class PasswordResetViewModel @Inject constructor(
    private val repository: IAccountRepository,
) : ViewModel() {
    private val resetPassword = MutableSharedFlow<User.ResetPassword>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val result = resetPassword.flatMapLatest {
        repository.resetPassword(it).flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun resetPassword(resetPassword: User.ResetPassword) {
        viewModelScope.launch {
            this@PasswordResetViewModel.resetPassword.emit(resetPassword)
        }
    }
}