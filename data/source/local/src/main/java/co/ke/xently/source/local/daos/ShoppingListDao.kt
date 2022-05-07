package co.ke.xently.source.local.daos

import androidx.paging.PagingSource
import androidx.room.*
import co.ke.xently.data.GroupedShoppingListCount
import co.ke.xently.data.ShoppingListItem
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ShoppingListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(vararg items: ShoppingListItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(items: List<ShoppingListItem>)

    @Transaction
    @Query("SELECT * FROM shoppinglist WHERE id = :id")
    fun get(id: Long): Flow<ShoppingListItem.WithRelated?>

    @Transaction
    @Query("SELECT * FROM shoppinglist ORDER BY name")
    fun get(): PagingSource<Int, ShoppingListItem.WithRelated>

    @Transaction
    @Query("SELECT * FROM shoppinglist WHERE dateAdded = :group ORDER BY name")
    fun get(group: Date): PagingSource<Int, ShoppingListItem.WithRelated>

    @Transaction
    @Query("SELECT * FROM shoppinglist WHERE dateAdded = :group ORDER BY name")
    fun getList(group: Date): Flow<List<ShoppingListItem.WithRelated>>

    @Transaction
    @Query("SELECT * FROM shoppinglist WHERE id = :id")
    fun getList(id: Long): Flow<List<ShoppingListItem.WithRelated>>

    @Query("SELECT dateAdded AS `group`, COUNT(dateAdded) AS numberOfItems FROM shoppinglist GROUP BY dateAdded")
    fun getCountGroupedByDateAdded(): Flow<List<GroupedShoppingListCount>>

    @Query("DELETE FROM shoppinglist")
    suspend fun deleteAll(): Int
}