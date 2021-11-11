package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ke.xently.data.Shop
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addShops(vararg items: Shop)

    @Query("SELECT * FROM shops;")
    fun getShopList(): Flow<List<Shop>>

    @Query("SELECT * FROM shops WHERE id = :id;")
    fun getShop(id: Long): Flow<Shop?>
}