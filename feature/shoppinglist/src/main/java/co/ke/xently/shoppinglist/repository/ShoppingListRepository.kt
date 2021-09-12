package co.ke.xently.shoppinglist.repository

import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.source.local.daos.ShoppingListDao
import co.ke.xently.source.remote.HttpException
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.ShoppingListService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.net.ConnectException
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
        retry: Retry,
    ): Flow<Result<List<ShoppingListItem>>> = if (remote) {
        flow { emit(sendRequest(401) { service.getShoppingList() }) }
            .map { result ->
                result.mapCatching {
                    it.results.apply {
                        dao.addShoppingListItems(*toTypedArray())
                    }
                }
            }
            .retry { cause -> cause is ConnectException && retry.canRetry() }
            .catch {
                // Let the collector handle other exceptions
                if (it is HttpException) throw it
                if (it is ConnectException) emit(Result.failure(it))
            }
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