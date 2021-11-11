package co.ke.xently.source.remote.services

import co.ke.xently.data.Shop
import retrofit2.Response
import retrofit2.http.*

interface ShopService {
    @GET("shops/")
    suspend fun getShopList(
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<List<Shop>>

    @GET("shops/{id}/")
    suspend fun getShop(
        @Path("id")
        id: Long,
        @Header("Cache-Control")
        cacheControl: String = "only-if-cached",
    ): Response<Shop>

    @POST("shops/{id}/")
    suspend fun addShop(@Path("id") id: Long, @Body shop: Shop): Response<Shop>

    @PUT("shops/{id}/")
    suspend fun updateShop(@Path("id") id: Long, @Body shop: Shop): Response<Shop>
}