package co.ke.xently.accounts.repository

import co.ke.xently.accounts.ui.password_reset.PasswordResetHttpException
import co.ke.xently.accounts.ui.password_reset.request.PasswordResetRequestHttpException
import co.ke.xently.accounts.ui.signin.SignUpHttpException
import co.ke.xently.accounts.ui.verification.VerificationHttpException
import co.ke.xently.common.Retry
import co.ke.xently.data.User
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.feature.repository.doTaskWhileSavingEachLocally
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AccountRepository @Inject constructor(private val dependencies: Dependencies) :
    IAccountRepository {
    override fun signIn(authHeaderData: String) = Retry().run {
        flow {
            emit(sendRequest { dependencies.service.account.signIn(authHeaderData) })
        }.doTaskWhileSavingEachLocally(this, dependencies)
    }

    override fun signUp(user: User) = Retry().run {
        flow {
            emit(sendRequest(errorClass = SignUpHttpException::class.java) {
                dependencies.service.account.signUp(user)
            })
        }.doTaskWhileSavingEachLocally(this, dependencies)
    }

    override fun resetPassword(resetPassword: User.ResetPassword) = Retry().run {
        flow {
            emit(sendRequest(errorClass = PasswordResetHttpException::class.java) {
                dependencies.service.account.resetPassword(
                    dependencies.database.accountDao.getCurrentlyActiveUserID(),
                    resetPassword,
                )
            })
        }.doTaskWhileSavingEachLocally(this, dependencies)
    }

    override fun requestTemporaryPassword(email: String) = Retry().run {
        flow {
            emit(sendRequest(errorClass = PasswordResetRequestHttpException::class.java) {
                dependencies.service.account.requestTemporaryPassword(mapOf("email" to email))
            })
        }.doTaskWhileSavingEachLocally(this, dependencies)
    }

    override fun requestVerificationCode() = Retry().run {
        flow {
            emit(sendRequest {
                dependencies.service.account.requestVerificationCode(dependencies.database.accountDao.getCurrentlyActiveUserID())
            })
        }.doTaskWhileSavingEachLocally(this, dependencies)
    }

    override fun verifyAccount(code: String) = Retry().run {
        flow {
            emit(sendRequest(errorClass = VerificationHttpException::class.java) {
                dependencies.service.account.verify(
                    dependencies.database.accountDao.getCurrentlyActiveUserID(),
                    mapOf("code" to code),
                )
            })
        }.doTaskWhileSavingEachLocally(this, dependencies)
    }
}