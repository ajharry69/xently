package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ke.xently.data.Shop
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(vararg shops: Shop)

    @Query("SELECT * FROM shops")
    fun get(): Flow<List<Shop>>

    @Query("SELECT * FROM shops WHERE id = :id")
    fun get(id: Long): Flow<Shop?>
}