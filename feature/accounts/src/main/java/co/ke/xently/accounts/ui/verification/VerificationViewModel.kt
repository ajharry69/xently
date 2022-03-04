package co.ke.xently.accounts.ui.verification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.accounts.repository.IAccountRepository
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class VerificationViewModel @Inject constructor(
    private val repository: IAccountRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val code = MutableSharedFlow<String>()
    val verifyResult = code.flatMapLatest {
        repository.verifyAccount(it).flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun verifyAccount(code: String) {
        viewModelScope.launch {
            this@VerificationViewModel.code.emit(code)
        }
    }

    private val resend = MutableSharedFlow<Boolean>()
    val resendResult = resend.flatMapLatest {
        repository.requestVerificationCode().flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun resendVerificationCode() {
        viewModelScope.launch {
            resend.emit(true)
        }
    }

    private val currentTimeoutKey = "${VerificationViewModel::class.java.name}.currentTimeout"

    // TODO: Restart counter 10 seconds after a successful or failed resend code request
    val resendCountDownSecond = ((0 until (savedStateHandle.get<Int>(currentTimeoutKey)
        ?: COUNTDOWN_START)).reversed()).asFlow()
        .onEach {
            delay(1000)
        }.mapLatest {
            savedStateHandle[currentTimeoutKey] = it
            it
        }.stateIn(
            scope = viewModelScope,
            started = DEFAULT_SHARING_STARTED,
            initialValue = savedStateHandle.get<Int>(currentTimeoutKey) ?: COUNTDOWN_START,
        )

    companion object {
        private const val COUNTDOWN_START = 60
    }
}