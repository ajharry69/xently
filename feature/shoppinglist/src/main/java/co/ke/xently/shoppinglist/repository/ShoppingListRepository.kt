package co.ke.xently.shoppinglist.repository

import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.source.local.daos.ShoppingListDao
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.ShoppingListService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepository @Inject constructor(
    private val dao: ShoppingListDao,
    private val service: ShoppingListService,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IShoppingListRepository {
    override fun addShoppingListItem(item: ShoppingListItem) = flow {
        emit(sendRequest(401) { service.addShoppingListItem(item) })
    }.onEach {
        dao.addShoppingListItems(item)
    }.flowOn(ioDispatcher)

    override fun getShoppingList(
        groupBy: String?,
        remote: Boolean,
    ): Flow<Result<List<ShoppingListItem>>> {
        val retry = Retry()
        return if (remote) {
            flow { emit(sendRequest(401) { service.getShoppingList() }) }
                .map { result ->
                    result.mapCatching {
                        it.results.apply {
                            dao.addShoppingListItems(*toTypedArray())
                        }
                    }
                }
                .retryCatchIfNecessary(retry)
                .flowOn(ioDispatcher)
                .onCompletion {
                    // Return cached records. Caveats:
                    //  1. No error propagation
                    if (it == null) emitAll(getShoppingList(groupBy, false))
                }
        } else {
            dao.getShoppingList().map { shoppingList ->
                Result.success(shoppingList)
            }
        }
    }

    override fun getGroupedShoppingList(groupBy: String): Flow<Result<List<GroupedShoppingList>>> {
        val retry = Retry()
        return flow { emit(sendRequest(401) { service.getShoppingList(groupBy) }) }
            .map { result ->
                result.mapCatching {
                    it.map { entry ->
                        val shoppingList = entry.value.apply {
                            dao.addShoppingListItems(*toTypedArray())
                        }
                        GroupedShoppingList(group = entry.key, shoppingList = shoppingList)
                    }
                }
            }
            .retryCatchIfNecessary(retry)
            .flowOn(ioDispatcher)
    }

    override fun getGroupedShoppingListCount(groupBy: String) = when (groupBy) {
        "dateadded" -> dao.getGroupCountByDateAdded()
        else -> flow { }
    }.mapLatest {
        mutableMapOf<Any, Int>().apply {
            for (item in it) put(item.group, item.numberOfItems)
        }.toMap()
    }
}