package co.ke.xently.shoppinglist.source

import co.ke.xently.source.local.daos.ShoppingListDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListLocalDataSource @Inject constructor(private val dao: ShoppingListDao) : IShoppingListDataSource {

}