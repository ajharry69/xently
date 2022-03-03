package co.ke.xently.feature.repository

import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    val currentlyActiveUser: Flow<User?>
    fun getUser(id: Long?): Flow<TaskResult<User>>
    fun update(user: User): Flow<TaskResult<User>>
    fun signOut(): Flow<TaskResult<Unit>>
}