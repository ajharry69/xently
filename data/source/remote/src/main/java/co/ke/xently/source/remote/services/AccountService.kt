package co.ke.xently.source.remote.services

import co.ke.xently.data.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AccountService {
    @POST("accounts/signin/")
    fun signIn(@Header("Authorization") basicAuthData: String): Response<User>

    @POST("accounts/signup/")
    fun signUp(@Body user: User): Response<User>

    @POST("accounts/{id}/verify-account/")
    fun verify(@Path("id") userId: Long, @Body codeValue: Pair<String, String>): Response<User>

    @POST("accounts/{id}/request-temporary-password/")
    fun requestTemporaryPassword(
        @Path("id") userId: Long,
        @Body emailValue: Pair<String, String>,
    ): Response<User>

    @POST("accounts/{id}/reset-password/")
    fun requestPassword(
        @Path("id") userId: Long,
        @Body resetPassword: User.ResetPassword,
    ): Response<User>

    @POST("accounts/{id}/signout/")
    fun signout(@Path("id") userId: Long): Response<Unit>
}