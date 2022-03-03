package co.ke.xently.feature.repository

import androidx.core.content.edit
import co.ke.xently.common.Retry
import co.ke.xently.common.TOKEN_VALUE_SHARED_PREFERENCE_KEY
import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import co.ke.xently.source.remote.HttpException
import co.ke.xently.source.remote.retryCatch
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AuthRepository @Inject constructor(
    private val dependencies: Dependencies,
) : IAuthRepository {
    override val currentlyActiveUser: Flow<User?>
        get() = dependencies.database.accountDao.getCurrentlyActiveUser()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUser(id: Long?) = Retry().run {
        if (id == null) {
            currentlyActiveUser.flatMapLatest {
                if (it == null) {
                    throw HttpException("User not found", statusCode = 404)
                } else {
                    flowOf(TaskResult.Success(it))
                }
            }
        } else {
            flow {
                emit(sendRequest { dependencies.service.account.get(userId = id) })
            }.doTaskWhileSavingEachLocally(this, dependencies)
        }
    }

    override fun update(user: User) = Retry().run {
        flow {
            emit(
                sendRequest(AccountHttpException::class.java) {
                    dependencies.service.account.update(user.id, user)
                },
            )
        }.doTaskWhileSavingEachLocally(this, dependencies)
    }

    override fun signOut() = Retry().run {
        flow {
            emit(dependencies.database.accountDao.getCurrentlyActiveUserID())
        }.map {
            dependencies.database.accountDao.delete(it)
            dependencies.preference.encrypted.edit {
                remove(TOKEN_VALUE_SHARED_PREFERENCE_KEY)
            }
            @Suppress("BlockingMethodInNonBlockingContext")
            withContext(dependencies.dispatcher.io) {
                dependencies.cache.evictAll()
            }
            sendRequest {
                dependencies.service.account.signout(it)
            }
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }
}