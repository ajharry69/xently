package co.ke.xently.common.data

import co.ke.xently.common.utils.JSON_CONVERTER
import co.ke.xently.common.utils.Log
import retrofit2.Response

data class HttpException(
    val detail: String?,
    val code: String? = null,
    val errors: Map<String, HttpException> = mapOf(),
    @Transient
    val response: Response<*> = Response.success(null),
) : RuntimeException(detail)

fun Response<*>.error(): HttpException = try {
    val exception = JSON_CONVERTER.fromJson(
        errorBody()?.string(),
        HttpException::class.java
    ).copy(response = this)
    if (exception.detail.isNullOrBlank()) exception.copy(detail = message()) else exception
} catch (ex: IllegalStateException) {
    Log.show("HttpError", ex.message, ex, Log.Type.ERROR)
    HttpException(message(), null, response = this)
}