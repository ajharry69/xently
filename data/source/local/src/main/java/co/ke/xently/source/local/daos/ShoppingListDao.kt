package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ke.xently.data.GroupedShoppingListCount
import co.ke.xently.data.ShoppingListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(vararg items: ShoppingListItem)

    @Query("SELECT * FROM shoppinglist")
    fun get(): Flow<List<ShoppingListItem>>

    @Query("SELECT * FROM shoppinglist WHERE id = :id")
    fun get(id: Long): Flow<ShoppingListItem?>

    @Query("SELECT dateAdded AS `group`, COUNT(dateAdded) AS numberOfItems FROM shoppinglist GROUP BY dateAdded")
    fun getCountGroupedByDateAdded(): Flow<List<GroupedShoppingListCount>>
}