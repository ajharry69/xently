package co.ke.xently.source.remote.services

import co.ke.xently.data.ShoppingListItem
import co.ke.xently.source.remote.PagedData
import retrofit2.Response
import retrofit2.http.*

interface ShoppingListService {
    @GET("shopping-list/")
    suspend fun getShoppingList(): Response<PagedData<ShoppingListItem>>

    @GET("shopping-list/grouped/")
    suspend fun getShoppingList(
        @Query("by") groupBy: String,
        @Header("Cache-Control") cacheControl: String = "",
    ): Response<Map<String, List<ShoppingListItem>>>

    @POST("shopping-list/")
    suspend fun addShoppingListItem(@Body item: ShoppingListItem): Response<ShoppingListItem>
}