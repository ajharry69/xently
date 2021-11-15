package co.ke.xently.source.remote.services

import co.ke.xently.data.Product
import co.ke.xently.source.remote.PagedData
import retrofit2.Response
import retrofit2.http.*

interface ProductService {
    @GET("products/")
    suspend fun getProductList(
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<PagedData<Product>>

    @GET("products/{id}/")
    suspend fun getProduct(
        @Path("id")
        id: Long,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<Product>

    @POST("products/")
    suspend fun addProduct(@Body shop: Product): Response<Product>

    @PUT("products/{id}/")
    suspend fun updateProduct(@Path("id") id: Long, @Body shop: Product): Response<Product>
}