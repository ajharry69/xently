package co.ke.xently.source.remote

import retrofit2.Response
import java.net.ConnectException

data class HttpException(
    val detail: String?,
    val errorCode: String? = null,
    val errors: Map<String, HttpException> = mapOf(),
) : RuntimeException() {
    override val message: String?
        get() = detail ?: super.message
}

@Suppress("UNCHECKED_CAST")
suspend fun <T> sendRequest(
    vararg throwOnStatusCode: Int,
    request: suspend () -> Response<T>,
): Result<T> {
    return try {
        val response = request.invoke() // Initiate actual network request call
        val (statusCode, body, errorBody) = Triple(
            response.code(),
            response.body(),
            response.errorBody()
        )
        if (response.isSuccessful) {
            val alt = Any() as T
            Result.success(if (statusCode == 204) alt else body ?: alt)
        } else {
            val error = try {
                val exception = JSON_CONVERTER.fromJson(
                    // The following is potentially blocking! Assume the consumer will call the
                    // suspend function from IO dispatcher.
                    errorBody!!.string(),
                    HttpException::class.java
                )
                if (exception.detail.isNullOrBlank()) exception.copy(detail = response.message()) else exception
            } catch (ex: IllegalStateException) {
                HttpException(response.message())
            }
            if (statusCode in throwOnStatusCode) {
                throw error
            }
            Result.failure(error)
        }
    } catch (ex: ConnectException) {
        throw ex
    } catch (ex: HttpException) {
        throw ex
    } catch (ex: Exception) {
        Result.failure(ex)
    }
}