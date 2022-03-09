package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import co.ke.xently.data.Product
import co.ke.xently.data.ShoppingListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandDao {
    @Insert(onConflict = REPLACE)
    suspend fun add(brands: List<Product.Brand>)

    @Insert(onConflict = REPLACE)
    suspend fun save(brands: List<ShoppingListItem.Brand>)

    @Query("SELECT * FROM product_brands WHERE name LIKE :query GROUP BY name ORDER BY name")
    fun get(query: String): Flow<List<Product.Brand>>
}