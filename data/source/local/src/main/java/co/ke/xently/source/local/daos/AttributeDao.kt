package co.ke.xently.source.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import co.ke.xently.data.Product
import co.ke.xently.data.ShoppingListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AttributeDao {
    @Insert(onConflict = REPLACE)
    suspend fun add(attributes: List<Product.Attribute>)

    @Insert(onConflict = REPLACE)
    suspend fun save(attributes: List<ShoppingListItem.Attribute>)

    @Query("SELECT * FROM product_attributes WHERE name LIKE :query GROUP BY name ORDER BY name")
    fun getByName(query: String): Flow<List<Product.Attribute>>

    @Query("SELECT * FROM product_attributes WHERE value LIKE :query GROUP BY value ORDER BY value")
    fun getByValue(query: String): Flow<List<Product.Attribute>>

    @Query("SELECT * FROM product_attributes WHERE name LIKE :nameQuery AND value LIKE :valueQuery GROUP BY name, value ORDER BY name, value")
    fun get(nameQuery: String, valueQuery: String): Flow<List<Product.Attribute>>
}