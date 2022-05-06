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
    @Query(
        """SELECT products.* FROM products
        LEFT JOIN
            product_attributes ON product_attributes.relatedId = products.id
        LEFT JOIN
            product_brands ON product_brands.relatedId = products.id
        WHERE
            products.name LIKE :query OR
            product_attributes.name LIKE :query OR
            product_attributes.value LIKE :query OR
            product_brands.name LIKE :query
        GROUP BY products.id
        ORDER BY products.name
    """
    )
    fun getProducts(query: String): Flow<List<Product.WithRelated>>
}