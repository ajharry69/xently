package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ke.xently.data.MeasurementUnit
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementUnitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(units: List<MeasurementUnit>)

    @Query("SELECT * FROM measurement_units WHERE name LIKE :query ORDER BY name")
    fun get(query: String): Flow<List<MeasurementUnit>>
}