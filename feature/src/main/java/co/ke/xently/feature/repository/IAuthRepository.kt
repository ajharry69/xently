package co.ke.xently.feature.repository

import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    val historicallyFirstUser: Flow<User?>
    fun signOut(): Flow<TaskResult<Unit>>
}