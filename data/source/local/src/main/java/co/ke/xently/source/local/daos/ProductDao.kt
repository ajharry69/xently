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
    @Query("SELECT * FROM products WHERE shopId = :shopId ORDER BY dateAdded DESC")
    fun getForShop(shopId: Long): PagingSource<Int, Product.WithRelated>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    fun get(id: Long): Flow<Product.WithRelated?>

    @Query("DELETE FROM products WHERE shopId = :shopId")
    suspend fun deleteAll(shopId: Long): Int

    @Query("DELETE FROM products")
    suspend fun deleteAll(): Int

    @Transaction
    @Query("""SELECT p.* FROM products AS p 
        LEFT JOIN
            product_attributes AS pa ON pa.relatedId = p.id
        LEFT JOIN
            product_brands AS pb ON pb.relatedId = p.id
        WHERE
            p.name LIKE :query OR
            pa.name LIKE :query OR
            pa.value LIKE :query OR
            pb.name LIKE :query
        GROUP BY p.id
        ORDER BY p.name
    """)
    fun getProducts(query: String): Flow<List<Product.WithRelated>>
}