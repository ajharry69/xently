package co.ke.xently.accounts.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import co.ke.xently.accounts.ui.password_reset.PasswordResetHttpException
import co.ke.xently.accounts.ui.password_reset.request.PasswordResetRequestHttpException
import co.ke.xently.accounts.ui.signin.SignUpHttpException
import co.ke.xently.common.Retry
import co.ke.xently.common.TOKEN_VALUE_SHARED_PREFERENCE_KEY
import co.ke.xently.common.di.qualifiers.EncryptedSharedPreference
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.data.getOrThrow
import co.ke.xently.source.local.Database
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.AccountService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AccountRepository @Inject constructor(
    private val service: AccountService,
    private val database: Database,
    @EncryptedSharedPreference
    private val preferences: SharedPreferences,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IAccountRepository {
    private suspend fun saveLocally(it: TaskResult<User>) {
        val user = it.getOrThrow()
        database.accountsDao.save(user)
        preferences.edit(commit = true) {
            putString(TOKEN_VALUE_SHARED_PREFERENCE_KEY, user.token)
        }
    }

    override fun signIn(authHeaderData: String) = Retry().run {
        flow {
            emit(sendRequest(401) { service.signIn(authHeaderData) })
        }.onEach {
            if (it is TaskResult.Success) saveLocally(it)
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun signUp(user: User) = Retry().run {
        flow {
            emit(sendRequest(401, errorClass = SignUpHttpException::class.java) {
                service.signUp(user)
            })
        }.onEach {
            if (it is TaskResult.Success) saveLocally(it)
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun resetPassword(resetPassword: User.ResetPassword) = Retry().run {
        flow {
            emit(sendRequest(401, errorClass = PasswordResetHttpException::class.java) {
                service.resetPassword(
                    database.accountsDao.getHistoricallyFirstUserId(),
                    resetPassword,
                )
            })
        }.onEach {
            if (it is TaskResult.Success) saveLocally(it)
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun requestTemporaryPassword(email: String) = Retry().run {
        flow {
            emit(sendRequest(401, errorClass = PasswordResetRequestHttpException::class.java) {
                service.requestTemporaryPassword(mapOf("email" to email))
            })
        }.onEach {
            if (it is TaskResult.Success) saveLocally(it)
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun requestVerificationCode() = Retry().run {
        flow {
            emit(sendRequest(401) {
                service.requestVerificationCode(database.accountsDao.getHistoricallyFirstUserId())
            })
        }.onEach {
            if (it is TaskResult.Success) saveLocally(it)
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun verifyAccount(code: String) = Retry().run {
        flow {
            emit(sendRequest(401) {
                service.verify(
                    database.accountsDao.getHistoricallyFirstUserId(),
                    mapOf("code" to code),
                )
            })
        }.onEach {
            if (it is TaskResult.Success) saveLocally(it)
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun signout() = Retry().run {
        flow {
            emit(database.accountsDao.getHistoricallyFirstUserId())
        }.map {
            database.accountsDao.delete(it)
            preferences.edit {
                remove(TOKEN_VALUE_SHARED_PREFERENCE_KEY)
            }
            sendRequest(401) {
                service.signout(it)
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }
}