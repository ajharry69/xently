package co.ke.xently.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import co.ke.xently.common.utils.Exclude
import co.ke.xently.common.utils.Exclude.During.SERIALIZATION

@Entity(tableName = "shoppinglist", indices = [Index(value = ["localId", "id"], unique = true)])
data class ShoppingListItem(
    @Exclude(SERIALIZATION)
    /**
     * ID from the server
     */
    val id: Long,
    @Exclude
    @PrimaryKey(autoGenerate = true)
    /**
     * ID for local caching before syncing with server
     */
    val localId: Long,
    val name: String,
    val unit: String,
    val unitQuantity: Float,
    val purchaseQuantity: Float,
    /*val dateAdded: Date,*/
)
