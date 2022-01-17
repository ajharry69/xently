package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import co.ke.xently.data.User
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountsDao {
    @Insert(onConflict = REPLACE)
    suspend fun save(user: User)

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun get(id: Long): Flow<User?>

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun delete(id: Long)
}