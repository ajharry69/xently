package co.ke.xently.common.data.source.remote

import co.ke.xently.common.data.TaskResult
import co.ke.xently.common.data.models.error
import co.ke.xently.common.data.source.AbstractDataSource
import co.ke.xently.common.utils.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Response

abstract class AbstractRemoteDataSource<M>(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) :
    AbstractDataSource<M>() {
    @Suppress("UNCHECKED_CAST", "BlockingMethodInNonBlockingContext")
    protected suspend fun <T> sendRequest(request: suspend () -> Response<T>): TaskResult<T> {
        return try {
            val response = request.invoke() // Initiate actual network request call
            val (statusCode, body, _) = Triple(
                response.code(),
                response.body(),
                response.errorBody()
            )
            if (response.isSuccessful) {
                val alt = Any() as T
                TaskResult.Success(if (statusCode == 204) alt else body ?: alt)
            } else {
                /*withContext<TaskResult<T>>(ioDispatcher) {
                    TaskResult.Error(response.error().error.toString())
                }*/
                TaskResult.Error(response.error().error.toString())
            }
        } catch (ex: Exception) {
            Log.show(TAG, ex.message, ex, Log.Type.ERROR)
            TaskResult.Error(ex)
        }
    }
}