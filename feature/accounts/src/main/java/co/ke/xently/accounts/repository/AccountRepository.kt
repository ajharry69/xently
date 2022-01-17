package co.ke.xently.accounts.repository

import android.util.Log
import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
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
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IAccountRepository {
    override fun signIn(basicAuthData: String) = Retry().run {
        Log.d("okhttp.OkHttpClient", "SignInScreen: <$basicAuthData>")
        flow {
            emit(sendRequest(401) { service.signIn(basicAuthData) })
        }.onEach {
            database.accountsDao.save(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun signUp(user: User) = Retry().run {
        flow {
            emit(sendRequest(401) { service.signUp(user) })
        }.onEach {
            database.accountsDao.save(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }
}