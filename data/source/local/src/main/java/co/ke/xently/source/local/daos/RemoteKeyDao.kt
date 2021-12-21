package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import co.ke.xently.data.RemoteKey

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = REPLACE)
    suspend fun save(key: RemoteKey)

    @Query("SELECT * FROM remote_keys WHERE endpoint = :endpoint")
    suspend fun get(endpoint: String): RemoteKey?

    @Query("DELETE FROM remote_keys WHERE endpoint = :endpoint")
    suspend fun delete(endpoint: String): Int
}