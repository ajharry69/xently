package co.ke.xently.common.data

import retrofit2.Response

@Suppress("UNCHECKED_CAST")
suspend fun <T> sendRequest(request: suspend () -> Response<T>): Result<T> {
    return try {
        val response = request.invoke() // Initiate actual network request call
        val (statusCode, body, _) = Triple(
            response.code(),
            response.body(),
            response.errorBody()
        )
        if (response.isSuccessful) {
            val alt = Any() as T
            Result.success(if (statusCode == 204) alt else body ?: alt)
        } else {
            Result.failure(RuntimeException(response.error().error.toString()))
        }
    } catch (ex: Exception) {
        Result.failure(ex)
    }
}