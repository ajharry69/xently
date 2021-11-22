package co.ke.xently.source.local.daos

import androidx.paging.PagingSource
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(items: List<ShoppingListItem>)

    @Query("SELECT * FROM shoppinglist WHERE id = :id")
    fun get(id: Long): Flow<ShoppingListItem?>

    @Query("SELECT * FROM shoppinglist ORDER BY name")
    fun get(): PagingSource<Int, ShoppingListItem>

    @Query("SELECT dateAdded AS `group`, COUNT(dateAdded) AS numberOfItems FROM shoppinglist GROUP BY dateAdded")
    fun getCountGroupedByDateAdded(): Flow<List<GroupedShoppingListCount>>

    @Query("DELETE FROM shoppinglist")
    suspend fun deleteAll(): Int
}