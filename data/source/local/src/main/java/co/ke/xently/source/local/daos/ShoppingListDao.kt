package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ke.xently.data.ShoppingListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addShoppingListItems(vararg items: ShoppingListItem)

    @Query("SELECT * FROM shoppinglist;")
    fun getShoppingList(): Flow<List<ShoppingListItem>>
}