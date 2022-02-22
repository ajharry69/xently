package co.ke.xently.source.local.daos

import androidx.paging.PagingSource
import androidx.room.*
import co.ke.xently.data.Address

@Dao
interface AddressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(addresses: List<Address>)

    @Transaction
    @Query("SELECT * FROM addresses WHERE shopId = :shopId AND town != '' ORDER BY town")
    fun get(shopId: Long): PagingSource<Int, Address.WithShop>

    @Query("DELETE FROM addresses WHERE shopId = :shopId")
    suspend fun deleteAll(shopId: Long): Int
}