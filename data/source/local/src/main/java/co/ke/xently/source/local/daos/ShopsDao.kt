package co.ke.xently.source.local.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ke.xently.data.Shop
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(vararg shops: Shop)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(shops: List<Shop>)

    @Query("SELECT * FROM shops ORDER BY name")
    fun get(): PagingSource<Int, Shop>

    @Query("SELECT * FROM shops WHERE id = :id")
    fun get(id: Long): Flow<Shop?>

    @Query("DELETE FROM shops")
    suspend fun deleteAll(): Int
}