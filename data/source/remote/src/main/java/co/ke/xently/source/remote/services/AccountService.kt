package co.ke.xently.source.remote.services

import co.ke.xently.data.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AccountService {
    @POST("accounts/signin/")
    suspend fun signIn(@Header("Authorization") basicAuthData: String): Response<User>

    @POST("accounts/signup/")
    suspend fun signUp(@Body user: User): Response<User>

    @POST("accounts/{id}/verify-account/")
    suspend fun verify(
        @Path("id") userId: Long,
        @Body codeValue: Pair<String, String>,
    ): Response<User>

    @POST("accounts/{id}/request-verification-code/")
    suspend fun requestVerificationCode(@Path("id") userId: Long): Response<User>

    @POST("accounts/{id}/request-temporary-password/")
    suspend fun requestTemporaryPassword(
        @Path("id") userId: Long,
        @Body emailValue: Pair<String, String>,
    ): Response<User>

    @POST("accounts/{id}/reset-password/")
    suspend fun resetPassword(
        @Path("id") userId: Long,
        @Body resetPassword: User.ResetPassword,
    ): Response<User>

    @POST("accounts/{id}/signout/")
    suspend fun signout(@Path("id") userId: Long): Response<Unit>
}