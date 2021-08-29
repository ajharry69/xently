package co.ke.xently.shoppinglist.source

import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.source.remote.services.ShoppingListService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRemoteDataSource @Inject constructor(
    private val service: ShoppingListService,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IShoppingListDataSource {

}