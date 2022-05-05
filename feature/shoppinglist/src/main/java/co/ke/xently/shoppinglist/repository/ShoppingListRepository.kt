package co.ke.xently.shoppinglist.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import co.ke.xently.common.DEFAULT_SERVER_DATE_FORMAT
import co.ke.xently.common.Retry
import co.ke.xently.data.*
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.products.shared.repository.SearchableRepository
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.GroupBy.DateAdded
import co.ke.xently.shoppinglist.ShoppingListRemoteMediator
import co.ke.xently.shoppinglist.saveLocallyWithAttributes
import co.ke.xently.source.remote.CacheControl
import co.ke.xently.source.remote.retryCatch
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ShoppingListRepository @Inject constructor(private val dependencies: Dependencies) :
    SearchableRepository(dependencies), IShoppingListRepository {
    // TODO: Use memoization to retrieve all grouped shopping list items...
    override fun add(item: ShoppingListItem) = Retry().run {
        flow {
            emit(sendRequest { dependencies.service.shoppingList.add(item) })
        }.onEach { result ->
            result.getOrNull()?.also {
                it.saveLocallyWithAttributes(dependencies)
            }
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }

    override fun get(groupBy: GroupBy, cacheControl: CacheControl) = Retry().run {
        flow {
            emit(
                sendRequest {
                    dependencies.service.shoppingList.get(
                        groupBy = groupBy.name.lowercase(),
                        cacheControl = cacheControl.toString(),
                    )
                },
            )
        }.map { result ->
            result.mapCatching {
                if (cacheControl is CacheControl.NoCache) {
                    // Signifies refresh
                    withContext(dependencies.dispatcher.io) {
                        dependencies.database.shoppingListDao.deleteAll()
                    }
                }
                it.map { entry ->
                    withContext(dependencies.dispatcher.io) {
                        launch {
                            entry.value.saveLocallyWithAttributes(dependencies, scope = this)
                        }
                    }
                    GroupedShoppingList(group = entry.key, shoppingList = entry.value)
                }
            }
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }

    override fun getCount(groupBy: GroupBy) = when (groupBy) {
        DateAdded -> dependencies.database.shoppingListDao.getCountGroupedByDateAdded()
    }.mapLatest {
        mutableMapOf<Any, Int>().apply {
            for (item in it) {
                put(item.group, item.numberOfItems)
            }
        }.toMap()
    }

    override fun get(id: Long) = Retry().run {
        dependencies.database.shoppingListDao.get(id).mapLatest { item ->
            if (item == null) {
                sendRequest { dependencies.service.shoppingList.get(id) }.apply {
                    getOrNull()?.also {
                        dependencies.database.shoppingListDao.save(it)
                    }
                }
            } else {
                TaskResult.Success(item.item)
            }
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }

    override fun get(config: PagingConfig, group: ShoppingListGroup?) = Pager(
        config = config,
        remoteMediator = ShoppingListRemoteMediator(group, dependencies),
        pagingSourceFactory = {
            if (group != null && group.groupBy == DateAdded) {
                dependencies.database.shoppingListDao.get(
                    DEFAULT_SERVER_DATE_FORMAT.parse(group.group.toString())!!,
                )
            } else {
                dependencies.database.shoppingListDao.get()
            }
        },
    ).flow.map { data ->
        data.map { it.item }
    }
}