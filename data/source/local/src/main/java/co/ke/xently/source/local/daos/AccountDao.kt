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

    @Query("UPDATE accounts SET isActive = 0 WHERE id != :userId")
    suspend fun makeInactiveExcept(userId: Long)

    @Query("SELECT * FROM accounts WHERE isActive = 1")
    fun getCurrentlyActiveUser(): Flow<User?>

    @Query("SELECT id FROM accounts WHERE isActive = 1")
    suspend fun getCurrentlyActiveUserID(): Long

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: Long)
}