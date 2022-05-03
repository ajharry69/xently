package co.ke.xently.source.local.daos

import androidx.paging.PagingSource
import androidx.room.*
import co.ke.xently.data.Shop
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg shops: Shop)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(shops: List<Shop>)

    @Query("SELECT name FROM shops WHERE id = :id")
    fun getShopName(id: Long): Flow<String?>

    @Query("SELECT * FROM shops ORDER BY name")
    fun get(): PagingSource<Int, Shop>

    @Query("SELECT * FROM shops WHERE name LIKE :query OR taxPin LIKE :query ORDER BY name")
    fun get(query: String): PagingSource<Int, Shop>

    @Transaction
    @Query("SELECT * FROM shops WHERE id = :id")
    fun get(id: Long): Flow<Shop?>

    @Query("DELETE FROM shops")
    suspend fun deleteAll(): Int
}