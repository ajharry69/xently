package co.ke.xently.source.remote.services

import co.ke.xently.data.MeasurementUnit
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface MeasurementUnitService {
    @GET("measurement-units/")
    suspend fun get(
        @Query("q")
        query: String = "",
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<List<MeasurementUnit>>
}