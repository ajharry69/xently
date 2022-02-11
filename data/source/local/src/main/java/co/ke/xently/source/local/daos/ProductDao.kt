package co.ke.xently.source.local.daos

import androidx.paging.PagingSource
import androidx.room.*
import co.ke.xently.data.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(vararg products: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(products: List<Product>)

    @Transaction
    @Query("SELECT * FROM products ORDER BY dateAdded DESC")
    fun get(): PagingSource<Int, Product.WithRelated>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    fun get(id: Long): Flow<Product.WithRelated?>

    @Query("DELETE FROM products")
    suspend fun deleteAll(): Int
}