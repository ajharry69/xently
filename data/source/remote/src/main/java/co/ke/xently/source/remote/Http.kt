package co.ke.xently.source.remote

import android.util.Log
import co.ke.xently.common.Retry
import co.ke.xently.common.TAG
import co.ke.xently.data.TaskResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import retrofit2.Response
import java.net.ConnectException

open class HttpException(
    val detail: Any? = null,
    @Suppress("unused") val errorCode: String? = null,
    @Suppress("unused") var statusCode: Int? = null,
) : RuntimeException() {
    // TODO: Override this class in every parent class...
    open fun hasFieldErrors(): Boolean {
        return false
    }

    override val message: String?
        get() = when (detail) {
            null -> {
                super.message
            }
            is String -> {
                detail
            }
            is List<*> -> {
                detail.joinToString("\n")
            }
            else -> {
                throw IllegalStateException("'detail' can only be a (nullable) String or List")
            }
        }
}

val RETRY_ABLE_ERROR_CLASSES = arrayOf(ConnectException::class)

const val DEFAULT_CONNECT_EXCEPTION_MESSAGE = "Failed to connect to server."

fun <T> Flow<TaskResult<T>>.retryCatch(retry: Retry, logTag: String = TAG) = retry {
    it::class in RETRY_ABLE_ERROR_CLASSES && retry.canRetry()
}.catch {
    Log.e(logTag, "retryCatchConnectionException: ${it.message}", it)

    val error = when (it) {
        is CancellationException -> {
            throw it
        }
        is ConnectException -> {
            HttpException(DEFAULT_CONNECT_EXCEPTION_MESSAGE, "connection_failed").apply {
                initCause(it)
            }
        }
        else -> {
            it
        }
    }
    emit(TaskResult.Error(error))
}

@Suppress("UNCHECKED_CAST", "BlockingMethodInNonBlockingContext")
suspend fun <T, E : HttpException> sendRequest(
    errorClass: Class<E>,
    request: suspend () -> Response<T>,
): TaskResult<T> {
    val response = request.invoke() // Initiate actual network request call
    val (statusCode, body, errorBody) = Triple(
        response.code(),
        response.body(),
        response.errorBody()
    )
    return if (response.isSuccessful) {
        if (statusCode == 204) {
            throw HttpException("No results", "empty_response", 204)
        } else {
            TaskResult.Success(body ?: Any() as T)
        }
    } else {
        throw try {
            JSON_CONVERTER.fromJson(
                // The following is potentially blocking! Assume the consumer will call the
                // suspend function from IO dispatcher.
                errorBody!!.string(),
                errorClass
            )
        } catch (ex: IllegalStateException) {
            Log.e(TAG, "sendRequest: ${ex.message}", ex)
            HttpException(response.message())
        }.apply {
            if (this.statusCode == null) {
                this.statusCode = statusCode
            }
        }
    }
}

suspend fun <T> sendRequest(request: suspend () -> Response<T>) =
    sendRequest(errorClass = HttpException::class.java, request = request)