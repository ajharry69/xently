package co.ke.xently.accounts.repository

import androidx.core.content.edit
import co.ke.xently.accounts.ui.password_reset.PasswordResetHttpException
import co.ke.xently.accounts.ui.password_reset.request.PasswordResetRequestHttpException
import co.ke.xently.accounts.ui.signin.SignUpHttpException
import co.ke.xently.accounts.ui.verification.VerificationHttpException
import co.ke.xently.common.Retry
import co.ke.xently.common.TOKEN_VALUE_SHARED_PREFERENCE_KEY
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.source.remote.retryCatch
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AccountRepository @Inject constructor(private val dependencies: Dependencies) :
    IAccountRepository {
    private fun Flow<TaskResult<User>>.doTaskWhileSavingEachLocally(retry: Retry) =
        onEach { result ->
            result.getOrNull()?.also {
                dependencies.database.accountDao.save(it)
                dependencies.preference.encrypted.edit(commit = true) {
                    putString(TOKEN_VALUE_SHARED_PREFERENCE_KEY, it.token)
                }
            }
        }.retryCatch(retry).flowOn(dependencies.dispatcher.io)

    override fun signIn(authHeaderData: String) = Retry().run {
        flow {
            emit(sendRequest { dependencies.service.account.signIn(authHeaderData) })
        }.doTaskWhileSavingEachLocally(this)
    }

    override fun signUp(user: User) = Retry().run {
        flow {
            emit(sendRequest(errorClass = SignUpHttpException::class.java) {
                dependencies.service.account.signUp(user)
            })
        }.doTaskWhileSavingEachLocally(this)
    }

    override fun resetPassword(resetPassword: User.ResetPassword) = Retry().run {
        flow {
            emit(sendRequest(errorClass = PasswordResetHttpException::class.java) {
                dependencies.service.account.resetPassword(
                    dependencies.database.accountDao.getHistoricallyFirstUserId(),
                    resetPassword,
                )
            })
        }.doTaskWhileSavingEachLocally(this)
    }

    override fun requestTemporaryPassword(email: String) = Retry().run {
        flow {
            emit(sendRequest(errorClass = PasswordResetRequestHttpException::class.java) {
                dependencies.service.account.requestTemporaryPassword(mapOf("email" to email))
            })
        }.doTaskWhileSavingEachLocally(this)
    }

    override fun requestVerificationCode() = Retry().run {
        flow {
            emit(sendRequest {
                dependencies.service.account.requestVerificationCode(dependencies.database.accountDao.getHistoricallyFirstUserId())
            })
        }.doTaskWhileSavingEachLocally(this)
    }

    override fun verifyAccount(code: String) = Retry().run {
        flow {
            emit(sendRequest(errorClass = VerificationHttpException::class.java) {
                dependencies.service.account.verify(
                    dependencies.database.accountDao.getHistoricallyFirstUserId(),
                    mapOf("code" to code),
                )
            })
        }.doTaskWhileSavingEachLocally(this)
    }
}