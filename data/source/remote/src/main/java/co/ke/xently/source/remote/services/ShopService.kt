package co.ke.xently.source.remote.services

import co.ke.xently.data.Shop
import co.ke.xently.source.remote.PagedData
import retrofit2.Response
import retrofit2.http.*

interface ShopService {
    @GET("shops/")
    suspend fun getShopList(
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<PagedData<Shop>>

    @GET("shops/{id}/")
    suspend fun getShop(
        @Path("id")
        id: Long,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<Shop>

    @POST("shops/")
    suspend fun addShop(@Body shop: Shop): Response<Shop>

    @PUT("shops/{id}/")
    suspend fun updateShop(@Path("id") id: Long, @Body shop: Shop): Response<Shop>
}