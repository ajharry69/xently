package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import co.ke.xently.data.Brand
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandDao {
    @Insert(onConflict = REPLACE)
    suspend fun add(brands: List<Brand>)

    @Query("SELECT * FROM brands WHERE name LIKE :query ORDER BY name")
    fun get(query: String): Flow<List<Brand>>
}