package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ke.xently.data.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProducts(vararg items: Product)

    @Query("SELECT * FROM products;")
    fun getProductList(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id;")
    fun getProduct(id: Long): Flow<Product?>
}