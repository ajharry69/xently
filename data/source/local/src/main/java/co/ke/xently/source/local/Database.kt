package co.ke.xently.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.ke.xently.data.*
import co.ke.xently.source.local.daos.*

@Database(
    entities = [
        RemoteKey::class,
        Shop::class,
        User::class,
        Product::class,
        ShoppingListItem::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomTypeConverters.DateConverter::class, RoomTypeConverters.UriConverter::class)
abstract class Database : RoomDatabase() {
    abstract val remoteKeyDao: RemoteKeyDao
    abstract val accountsDao: AccountsDao
    abstract val shopsDao: ShopsDao
    abstract val productsDao: ProductsDao
    abstract val shoppingListDao: ShoppingListDao
}