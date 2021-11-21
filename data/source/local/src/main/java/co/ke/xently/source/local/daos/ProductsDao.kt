package co.ke.xently.source.local.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ke.xently.data.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(vararg products: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(products: List<Product>)

    @Query("SELECT * FROM products ORDER BY dateAdded DESC")
    fun pagingSource(): PagingSource<Int, Product>

    @Query("SELECT * FROM products WHERE id = :id")
    fun get(id: Long): Flow<Product?>

    @Query("DELETE FROM products")
    suspend fun deleteAll(): Int
}