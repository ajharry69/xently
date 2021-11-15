package co.ke.xently.shoppinglist.repository

import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.RecommendationReport
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.Recommend
import kotlinx.coroutines.flow.Flow

interface IShoppingListRepository {
    fun addShoppingListItem(item: ShoppingListItem): Flow<Result<ShoppingListItem>>

    fun getShoppingList(remote: Boolean): Flow<Result<List<ShoppingListItem>>>

    fun getGroupedShoppingList(groupBy: GroupBy): Flow<Result<List<GroupedShoppingList>>>

    fun getGroupedShoppingListCount(groupBy: GroupBy): Flow<Map<Any, Int>>

    fun getShoppingListItem(id: Long): Flow<Result<ShoppingListItem>>

    fun getRecommendations(recommend: Recommend): Flow<Result<RecommendationReport>>
}