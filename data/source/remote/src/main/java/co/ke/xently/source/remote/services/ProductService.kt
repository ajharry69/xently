package co.ke.xently.source.remote.services

import co.ke.xently.data.Product
import co.ke.xently.source.remote.PagedData
import retrofit2.Response
import retrofit2.http.*

interface ProductService {
    @GET("search/products/")
    suspend fun get(
        @Query("q")
        query: String = "",
        @Query("page")
        page: Int = 1,
        @Query("size")
        size: Int? = null,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<PagedData<Product>>

    @GET("search/products/{id}/")
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
}