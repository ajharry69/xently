package co.ke.xently.source.remote

import android.util.Log
import co.ke.xently.common.Retry
import co.ke.xently.common.TAG
import co.ke.xently.data.TaskResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException

open class HttpException(
    val detail: String? = null,
    val errorCode: String? = null,
    val errors: Map<String, HttpException> = mapOf(),
) : RuntimeException() {
    override val message: String?
        get() = detail ?: super.message
}

fun <T> Flow<TaskResult<T>>.retryCatchIfNecessary(retry: Retry) =
    retry { cause -> cause is ConnectException && retry.canRetry() }.catch {
        // Let the collector handle other exceptions
        if (it is HttpException) throw it
        if (it is ConnectException) emit(TaskResult.Error(it))
    }

@Suppress("UNCHECKED_CAST", "BlockingMethodInNonBlockingContext")
suspend fun <T, E : HttpException> sendRequest(
    vararg throwOnStatusCode: Int,
    errorClass: Class<E>,
    request: suspend () -> Response<T>,
): TaskResult<T> {
    return try {
        val response = request.invoke() // Initiate actual network request call
        val (statusCode, body, errorBody) = Triple(
            response.code(),
            response.body(),
            response.errorBody()
        )
        if (response.isSuccessful) {
            val alt = Any() as T
            TaskResult.Success(if (statusCode == 204) alt else body ?: alt)
        } else {
            val error = try {
                val exception = JSON_CONVERTER.fromJson(
                    // The following is potentially blocking! Assume the consumer will call the
                    // suspend function from IO dispatcher.
                    errorBody!!.string(),
                    errorClass
                )
                if (exception.detail.isNullOrBlank()) HttpException(detail = response.message()) else exception
            } catch (ex: IllegalStateException) {
                HttpException(response.message())
            }
            if (statusCode in throwOnStatusCode) {
                throw error
            }
            TaskResult.Error(error)
        }
    } catch (ex: ConnectException) {
        throw ex
    } catch (ex: SocketTimeoutException) {
        throw ex
    } catch (ex: HttpException) {
        throw ex
    } catch (ex: Exception) {
        Log.e(TAG, "sendRequest: ${ex.message}", ex)
        TaskResult.Error(ex)
    }
}

suspend fun <T> sendRequest(vararg throwOnStatusCode: Int, request: suspend () -> Response<T>) =
    sendRequest(*throwOnStatusCode, errorClass = HttpException::class.java, request = request)