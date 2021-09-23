package co.ke.xently.source.remote.services

import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.RecommendationReport
import co.ke.xently.source.remote.PagedData
import retrofit2.Response
import retrofit2.http.*

interface ShoppingListService {
    @POST("shopping-list/")
    suspend fun addShoppingListItem(@Body item: ShoppingListItem): Response<ShoppingListItem>

    @GET("shopping-list/{id}/")
    suspend fun getShoppingListItem(
        @Path("id") id: Long,
        @Header("Cache-Control") cacheControl: String = "only-if-cached",
    ): Response<ShoppingListItem>

    @GET("shopping-list/")
    suspend fun getShoppingList(
        @Header("Cache-Control") cacheControl: String = "only-if-cached",
    ): Response<PagedData<ShoppingListItem>>

    @GET("shopping-list/grouped/")
    suspend fun getShoppingList(
        @Query("by") groupBy: String,
        @Header("Cache-Control") cacheControl: String = "only-if-cached",
    ): Response<Map<String, List<ShoppingListItem>>>

    @GET("shopping-list/recommendations/")
    suspend fun getRecommendations(
        @Query("group") group: String,
        @Query("group_by") groupBy: String,
        @Header("Cache-Control") cacheControl: String = "only-if-cached",
    ): Response<RecommendationReport>
}