package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import co.ke.xently.data.Attribute
import kotlinx.coroutines.flow.Flow

@Dao
interface AttributeDao {
    @Insert(onConflict = REPLACE)
    suspend fun add(attributes: List<Attribute>)

    @Query("SELECT * FROM attributes WHERE name LIKE :query GROUP BY name ORDER BY name")
    fun getByName(query: String): Flow<List<Attribute>>

    @Query("SELECT * FROM attributes WHERE value LIKE :query GROUP BY value ORDER BY value")
    fun getByValue(query: String): Flow<List<Attribute>>

    @Query("SELECT * FROM attributes WHERE name LIKE :nameQuery AND value LIKE :valueQuery ORDER BY name, value")
    fun get(nameQuery: String, valueQuery: String): Flow<List<Attribute>>
}