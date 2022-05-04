package co.ke.xently.source.remote.services

import co.ke.xently.data.Product
import co.ke.xently.data.Shop
import co.ke.xently.source.remote.PagedData
import retrofit2.Response
import retrofit2.http.*

interface ShopService {
    @GET("search/shops/")
    suspend fun get(
        @Query("q")
        query: String = "",
        @Query("page")
        page: Int = 1,
        @Query("size")
        size: Int? = null,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<PagedData<Shop>>

    @GET("search/shops/{id}/")
    suspend fun get(
        @Path("id")
        id: Long,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<Shop>

    @GET("shops/{id}/products/")
    @Deprecated(
        "Replace with an ES alternative",
        replaceWith = ReplaceWith("dependencies.service.product.get")
    )
    suspend fun getProducts(
        @Path("id")
        shopId: Long,
        @Query("q")
        query: String = "",
        @Query("page")
        page: Int = 1,
        @Query("size")
        size: Int? = null,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<PagedData<Product>>

    @POST("shops/")
    suspend fun add(@Body shop: Shop): Response<Shop>

    @PUT("shops/{id}/")
    suspend fun update(@Path("id") id: Long, @Body shop: Shop): Response<Shop>
}