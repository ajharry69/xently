package co.ke.xently.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.ke.xently.data.Product
import co.ke.xently.data.RemoteKey
import co.ke.xently.data.Shop
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.source.local.daos.ProductsDao
import co.ke.xently.source.local.daos.RemoteKeyDao
import co.ke.xently.source.local.daos.ShoppingListDao
import co.ke.xently.source.local.daos.ShopsDao

@Database(
    entities = [
        RemoteKey::class,
        Shop::class,
        Product::class,
        ShoppingListItem::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomTypeConverters.DateConverter::class)
abstract class Database : RoomDatabase() {
    abstract val remoteKeyDao: RemoteKeyDao
    abstract val shopsDao: ShopsDao
    abstract val productsDao: ProductsDao
    abstract val shoppingListDao: ShoppingListDao
}