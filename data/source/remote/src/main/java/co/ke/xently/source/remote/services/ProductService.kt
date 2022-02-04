package co.ke.xently.source.remote.services

import co.ke.xently.data.MeasurementUnit
import co.ke.xently.data.Product
import co.ke.xently.source.remote.PagedData
import retrofit2.Response
import retrofit2.http.*

interface ProductService {
    @GET("products/")
    suspend fun get(
        @Query("page")
        page: Int = 1,
        @Query("size")
        size: Int? = null,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<PagedData<Product>>

    @GET("products/{id}/")
    suspend fun get(
        @Path("id")
        id: Long,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<Product>

    @POST("products/")
    suspend fun add(@Body shop: Product): Response<Product>

    @PUT("products/{id}/")
    suspend fun update(@Path("id") id: Long, @Body shop: Product): Response<Product>

    @GET("measurement-units/")
    suspend fun getMeasurementUnits(
        @Query("q")
        query: String = "",
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<List<MeasurementUnit>>
}