package co.ke.xently.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.ke.xently.data.Shop
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.source.local.daos.ShopDao
import co.ke.xently.source.local.daos.ShoppingListDao

@Database(
    entities = [
        Shop::class,
        ShoppingListItem::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomTypeConverters.DateConverter::class)
abstract class AssistantDatabase : RoomDatabase() {
    abstract val shopDao: ShopDao
    abstract val shoppingListDao: ShoppingListDao
}