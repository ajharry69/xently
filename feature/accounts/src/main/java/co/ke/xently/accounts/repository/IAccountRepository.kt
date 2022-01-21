package co.ke.xently.accounts.repository

import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import kotlinx.coroutines.flow.Flow

interface IAccountRepository {
    fun signIn(authHeaderData: String): Flow<TaskResult<User>>
    fun signUp(user: User): Flow<TaskResult<User>>
}