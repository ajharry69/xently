package co.ke.xently.shoppinglist.repository

import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.RecommendationReport
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.Recommend
import kotlinx.coroutines.flow.Flow

interface IShoppingListRepository {
    fun addShoppingListItem(item: ShoppingListItem): Flow<TaskResult<ShoppingListItem>>

    fun getShoppingList(remote: Boolean): Flow<TaskResult<List<ShoppingListItem>>>

    fun getGroupedShoppingList(groupBy: GroupBy): Flow<TaskResult<List<GroupedShoppingList>>>

    fun getGroupedShoppingListCount(groupBy: GroupBy): Flow<Map<Any, Int>>

    fun getShoppingListItem(id: Long): Flow<TaskResult<ShoppingListItem>>

    fun getRecommendations(recommend: Recommend): Flow<TaskResult<RecommendationReport>>
}