package co.ke.xently.shoppinglist.repository

import co.ke.xently.common.Retry
import co.ke.xently.data.ShoppingListItem
import kotlinx.coroutines.flow.Flow

interface IShoppingListRepository {
    fun addShoppingListItem(item: ShoppingListItem): Flow<Result<ShoppingListItem>>

    fun getShoppingList(
        groupBy: String?,
        remote: Boolean,
        retry: Retry = Retry(),
    ): Flow<Result<List<ShoppingListItem>>>
}