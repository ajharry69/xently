package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import co.ke.xently.data.User
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert(onConflict = REPLACE)
    suspend fun save(user: User)

    @Query("SELECT * FROM accounts ORDER BY timeRecorded LIMIT 1")
    fun getHistoricallyFirstUser(): Flow<User?>

    @Query("SELECT id FROM accounts ORDER BY timeRecorded LIMIT 1")
    suspend fun getHistoricallyFirstUserId(): Long

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: Long)
}