package co.ke.xently.shoppinglist.repository

import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.GroupedShoppingListCount
import co.ke.xently.data.ShoppingListItem
import kotlinx.coroutines.flow.Flow

interface IShoppingListRepository {
    fun addShoppingListItem(item: ShoppingListItem): Flow<Result<ShoppingListItem>>

    fun getShoppingList(groupBy: String?, remote: Boolean): Flow<Result<List<ShoppingListItem>>>

    fun getGroupedShoppingList(groupBy: String): Flow<Result<List<GroupedShoppingList>>>

    fun getGroupedShoppingListCount(groupBy: String): Flow<Map<Any, Int>>
}