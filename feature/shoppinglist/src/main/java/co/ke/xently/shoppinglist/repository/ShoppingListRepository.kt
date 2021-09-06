package co.ke.xently.shoppinglist.repository

import android.util.Log
import co.ke.xently.common.data.sendRequest
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.source.local.daos.ShoppingListDao
import co.ke.xently.source.remote.services.ShoppingListService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
        emit(sendRequest { service.addShoppingListItem(item) })
    }.onEach {
        dao.addShoppingListItems(item)
    }.flowOn(ioDispatcher)

    override fun getShoppingList(groupBy: String?) = flow {
        emit(sendRequest { service.getShoppingList() })
    }.map { result ->
        result.mapCatching {
            it.results.apply {
                dao.addShoppingListItems(*toTypedArray())
            }
        }
    }.flowOn(ioDispatcher)
}