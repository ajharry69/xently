package co.ke.xently.source.remote.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface LocationUpdateService {
    @POST("accounts/{id}/update-location/")
    suspend fun updateLocation(
        @Path("id") userId: Long = 1L,
        @Body location: Array<Double>,
    ): Response<Unit>
}