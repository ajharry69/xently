package co.ke.xently.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.source.local.RoomTypeConverters.StringListConverter
import co.ke.xently.source.local.daos.ShoppingListDao

@Database(
    entities = [
        ShoppingListItem::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class AssistantDatabase : RoomDatabase() {
    abstract val shoppingListDao: ShoppingListDao
}