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
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class VerificationViewModel @Inject constructor(
    private val repository: IAccountRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val currentTimeoutKey = "${VerificationViewModel::class.java.name}.currentTimeout"

    private val _isTimerOnHold = MutableSharedFlow<Boolean>()
    val isTimerOnHold = _isTimerOnHold.asSharedFlow()

    private val countdownSeconds = MutableSharedFlow<Int>()
    val resendCountDownSecond = countdownSeconds.onEach {
        delay(1000)
    }.mapLatest {
        savedStateHandle[currentTimeoutKey] = it
        it
    }.stateIn(
        scope = viewModelScope,
        started = DEFAULT_SHARING_STARTED,
        initialValue = savedStateHandle.get<Int>(currentTimeoutKey) ?: COUNTDOWN_START,
    )

    init {
        viewModelScope.launch {
            startCountdown()
        }
    }

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
        repository.requestVerificationCode().flagLoadingOnStart().onCompletion {
            // Restart counter 10 seconds after a successful or failed resend code request
            savedStateHandle[currentTimeoutKey] = COUNTDOWN_START
            startCountdown()
        }
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun resendVerificationCode() {
        viewModelScope.launch {
            resend.emit(true)
        }
    }

    private suspend fun startCountdown() {
        _isTimerOnHold.emit(true)
        delay(10.seconds)
        _isTimerOnHold.emit(false)
        countdownSeconds.emitAll(
            (0 until (savedStateHandle.get<Int>(currentTimeoutKey)
                ?: COUNTDOWN_START)).reversed().asFlow()
        )
    }

    companion object {
        private const val COUNTDOWN_START = 60
        const val DEFAULT_TIMER_ON_HOLD = true
    }
}