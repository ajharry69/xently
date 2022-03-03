package co.ke.xently.source.remote.services

import co.ke.xently.data.User
import retrofit2.Response
import retrofit2.http.*

interface AccountService {
    @GET("accounts/{id}/")
    suspend fun get(@Path("id") userId: Long): Response<User>

    @POST("accounts/signin/")
    suspend fun signIn(@Header("Authorization") basicAuthData: String): Response<User>

    @POST("accounts/signup/")
    suspend fun signUp(@Body user: User): Response<User>

    @POST("accounts/{id}/verify-account/")
    suspend fun verify(
        @Path("id") userId: Long,
        @Body codeValue: Map<String, String>,
    ): Response<User>

    @GET("accounts/{id}/request-verification-code/")
    suspend fun requestVerificationCode(@Path("id") userId: Long): Response<User>

    @POST("accounts/request-temporary-password/")
    suspend fun requestTemporaryPassword(@Body emailValue: Map<String, String>): Response<User>

    @POST("accounts/{id}/reset-password/")
    suspend fun resetPassword(
        @Path("id") userId: Long,
        @Body resetPassword: User.ResetPassword,
    ): Response<User>

    @POST("accounts/{id}/signout/")
    suspend fun signout(@Path("id") userId: Long): Response<Unit>

    @PUT("accounts/{id}/")
    suspend fun update(@Path("id") userId: Long = 1L, @Body user: User): Response<User>

    @POST("accounts/{id}/update-location/")
    suspend fun update(@Path("id") userId: Long = 1L, @Body location: Array<Double>): Response<Unit>
}