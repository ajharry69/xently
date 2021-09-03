package co.ke.xently.source.remote.services

import co.ke.xently.common.data.PagedData
import co.ke.xently.data.ShoppingListItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ShoppingListService {
    @GET("shopping-list/")
    suspend fun getShoppingList(): Response<PagedData<ShoppingListItem>>

    @POST("shopping-list/")
    suspend fun addShoppingListItem(@Body item: ShoppingListItem): Response<ShoppingListItem>
}