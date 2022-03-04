package co.ke.xently.accounts.ui.password_reset.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.accounts.repository.IAccountRepository
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
internal class PasswordResetRequestViewModel @Inject constructor(
    private val repository: IAccountRepository,
) : ViewModel() {
    private val email = MutableSharedFlow<String>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val result = email.flatMapLatest {
        repository.requestTemporaryPassword(it).flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun requestTemporaryPassword(email: String) {
        viewModelScope.launch {
            this@PasswordResetRequestViewModel.email.emit(email)
        }
    }
}