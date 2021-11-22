package co.ke.xently.shoppinglist.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.*
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.GroupBy.DateAdded
import co.ke.xently.shoppinglist.Recommend
import co.ke.xently.source.local.Database
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.ShoppingListService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ShoppingListRepository @Inject constructor(
    private val database: Database,
    private val service: ShoppingListService,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IShoppingListRepository {
    // TODO: Use memoization to retrieve all grouped shopping list items...
    override fun add(item: ShoppingListItem) = Retry().run {
        flow {
            emit(sendRequest(401) { service.add(item) })
        }.onEach {
            database.shoppingListDao.save(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun get(groupBy: GroupBy) = Retry().run {
        flow {
            emit(sendRequest(401) { service.get(groupBy = groupBy.name.lowercase()) })
        }.map { result ->
            result.mapCatching {
                it.map { entry ->
                    val shoppingList = entry.value.apply {
                        database.shoppingListDao.save(*toTypedArray())
                    }
                    GroupedShoppingList(group = entry.key, shoppingList = shoppingList)
                }
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun getCount(groupBy: GroupBy) = when (groupBy) {
        DateAdded -> database.shoppingListDao.getCountGroupedByDateAdded()
    }.mapLatest {
        mutableMapOf<Any, Int>().apply {
            for (item in it) put(item.group, item.numberOfItems)
        }.toMap()
    }

    override fun get(id: Long) = Retry().run {
        database.shoppingListDao.get(id).map { item ->
            if (item == null) {
                sendRequest(401) { service.get(id) }.apply {
                    getOrNull()?.also {
                        database.shoppingListDao.save(it)
                    }
                }
            } else {
                TaskResult.Success(item)
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    @Suppress("UNCHECKED_CAST")
    override fun get(recommend: Recommend) = Retry().run {
        flow {
            emit(sendRequest(401) {
                when (recommend.from) {
                    Recommend.From.Item -> {
                        val item = if (recommend.by !is ShoppingListItem)
                            database.shoppingListDao.get(recommend.by.toString().toLong())
                                .first()!! else recommend.by
                        service.getRecommendations(
                            RecommendationRequest(listOf(item), recommend.saveBy)
                        )
                    }
                    Recommend.From.ItemList -> {
                        service.getRecommendations(
                            RecommendationRequest(
                                recommend.by as List<ShoppingListItem>,
                                recommend.saveBy
                            )
                        )
                    }
                    Recommend.From.GroupedList -> {
                        service.getRecommendations(
                            recommend.by.toString(),
                            recommend.groupBy.name.lowercase()
                        )
                    }
                }
            })
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }
    override fun get(config: PagingConfig) = Pager(
        config=config,
        remoteMediator=ShoppingListRemoteMediator(database, service),
    ){
        database.shoppingListDao.get()
    }
}