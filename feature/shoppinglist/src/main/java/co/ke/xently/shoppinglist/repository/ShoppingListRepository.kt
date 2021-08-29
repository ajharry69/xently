package co.ke.xently.shoppinglist.repository

import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.shoppinglist.di.qualifiers.LocalShoppingListDataSource
import co.ke.xently.shoppinglist.di.qualifiers.RemoteShoppingListDataSource
import co.ke.xently.shoppinglist.source.IShoppingListDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepository @Inject constructor(
    @LocalShoppingListDataSource
    private val local: IShoppingListDataSource,
    @RemoteShoppingListDataSource
    private val remote: IShoppingListDataSource,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractShoppingListRepository() {

}