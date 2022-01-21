package co.ke.xently.accounts.repository

import android.content.SharedPreferences
import androidx.core.content.edit
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
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
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
            emit(sendRequest(401) { service.signUp(user) })
        }.onEach {
            if (it is TaskResult.Success) saveLocally(it)
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }
}