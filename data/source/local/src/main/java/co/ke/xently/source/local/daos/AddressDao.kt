package co.ke.xently.source.local.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ke.xently.data.Address

@Dao
interface AddressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(addresses: List<Address>)

    @Query("SELECT * FROM addresses WHERE shop = :shopId ORDER BY town")
    fun get(shopId: Long): PagingSource<Int, Address>

    @Query("DELETE FROM addresses")
    suspend fun deleteAll(): Int
}