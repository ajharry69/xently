package co.ke.xently.shoppinglist.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.ke.xently.common.Retry
import co.ke.xently.data.*
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.GroupBy.DateAdded
import co.ke.xently.shoppinglist.Recommend
import co.ke.xently.shoppinglist.ShoppingListRemoteMediator
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ShoppingListRepository @Inject constructor(private val dependencies: Dependencies) :
    IShoppingListRepository {
    // TODO: Use memoization to retrieve all grouped shopping list items...
    override fun add(item: ShoppingListItem) = Retry().run {
        flow {
            emit(sendRequest(401) { dependencies.service.shoppingList.add(item) })
        }.onEach { result ->
            result.getOrNull()?.also {
                dependencies.database.shoppingListDao.save(it)
            }
        }.retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }

    override fun get(groupBy: GroupBy) = Retry().run {
        flow {
            emit(sendRequest(401) { dependencies.service.shoppingList.get(groupBy = groupBy.name.lowercase()) })
        }.map { result ->
            result.mapCatching {
                it.map { entry ->
                    val shoppingList = entry.value.apply {
                        dependencies.database.shoppingListDao.save(*toTypedArray())
                    }
                    GroupedShoppingList(group = entry.key, shoppingList = shoppingList)
                }
            }
        }.retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }

    override fun getCount(groupBy: GroupBy) = when (groupBy) {
        DateAdded -> dependencies.database.shoppingListDao.getCountGroupedByDateAdded()
    }.mapLatest {
        mutableMapOf<Any, Int>().apply {
            for (item in it) put(item.group, item.numberOfItems)
        }.toMap()
    }

    override fun get(id: Long) = Retry().run {
        dependencies.database.shoppingListDao.get(id).map { item ->
            if (item == null) {
                sendRequest(401) { dependencies.service.shoppingList.get(id) }.apply {
                    getOrNull()?.also {
                        dependencies.database.shoppingListDao.save(it)
                    }
                }
            } else {
                TaskResult.Success(item)
            }
        }.retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(recommend: Recommend) = Retry().run {
        flow {
            emit(sendRequest(401) {
                when (recommend.from) {
                    Recommend.From.Item -> {
                        val item =
                            if (recommend.by !is ShoppingListItem) dependencies.database.shoppingListDao.get(
                                recommend.by.toString().toLong())
                                .first()!! else recommend.by
                        dependencies.service.shoppingList.getRecommendations(
                            RecommendationRequest(listOf(item), recommend.saveBy)
                        )
                    }
                    Recommend.From.ItemList -> {
                        dependencies.service.shoppingList.getRecommendations(
                            RecommendationRequest(
                                recommend.by as List<ShoppingListItem>,
                                recommend.saveBy
                            )
                        )
                    }
                    Recommend.From.GroupedList -> {
                        dependencies.service.shoppingList.getRecommendations(
                            recommend.by.toString(),
                            recommend.groupBy.name.lowercase()
                        )
                    }
                }
            })
        }.retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }

    override fun get(config: PagingConfig) = Pager(
        config = config,
        remoteMediator = ShoppingListRemoteMediator(dependencies),
        pagingSourceFactory = dependencies.database.shoppingListDao::get,
    ).flow
}