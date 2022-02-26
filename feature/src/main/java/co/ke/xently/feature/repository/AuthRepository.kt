package co.ke.xently.feature.repository

import androidx.core.content.edit
import co.ke.xently.common.Retry
import co.ke.xently.common.TOKEN_VALUE_SHARED_PREFERENCE_KEY
import co.ke.xently.data.User
import co.ke.xently.source.remote.retryCatch
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AuthRepository @Inject constructor(
    private val dependencies: Dependencies,
) : IAuthRepository {
    override val historicallyFirstUser: Flow<User?>
        get() = dependencies.database.accountDao.getHistoricallyFirstUser()

    override fun signOut() = Retry().run {
        flow {
            emit(dependencies.database.accountDao.getHistoricallyFirstUserId())
        }.map {
            dependencies.database.accountDao.delete(it)
            dependencies.preference.encrypted.edit {
                remove(TOKEN_VALUE_SHARED_PREFERENCE_KEY)
            }
            sendRequest {
                dependencies.service.account.signout(it)
            }
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }
}