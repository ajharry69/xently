package co.ke.xently.shoppinglist.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.RecommendationReport
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.products.shared.repository.ISearchableRepository
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.Recommend
import kotlinx.coroutines.flow.Flow

interface IShoppingListRepository : ISearchableRepository {
    fun add(item: ShoppingListItem): Flow<TaskResult<ShoppingListItem>>

    fun get(groupBy: GroupBy): Flow<TaskResult<List<GroupedShoppingList>>>

    fun getCount(groupBy: GroupBy): Flow<Map<Any, Int>>

    fun get(id: Long): Flow<TaskResult<ShoppingListItem>>

    fun get(recommend: Recommend): Flow<TaskResult<RecommendationReport>>

    fun get(config: PagingConfig): Flow<PagingData<ShoppingListItem>>
}