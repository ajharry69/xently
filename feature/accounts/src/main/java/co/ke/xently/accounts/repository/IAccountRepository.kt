package co.ke.xently.accounts.repository

import co.ke.xently.data.TaskResult
import co.ke.xently.data.User
import kotlinx.coroutines.flow.Flow

interface IAccountRepository {
    fun signIn(authHeaderData: String): Flow<TaskResult<User>>
    fun signUp(user: User): Flow<TaskResult<User>>
    fun resetPassword(resetPassword: User.ResetPassword): Flow<TaskResult<User>>
    fun requestTemporaryPassword(email: String): Flow<TaskResult<User>>
    fun requestVerificationCode(): Flow<TaskResult<User>>
    fun verifyAccount(code: String): Flow<TaskResult<User>>
    fun signout(): Flow<TaskResult<Unit>>
}