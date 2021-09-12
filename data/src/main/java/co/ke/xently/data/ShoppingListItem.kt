package co.ke.xently.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.ke.xently.common.Exclude
import co.ke.xently.common.Exclude.During.SERIALIZATION
import java.util.*

@Entity(tableName = "shoppinglist")
data class ShoppingListItem(
    @Exclude(SERIALIZATION)
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val name: String,
    val unit: String,
    val unitQuantity: Float,
    val purchaseQuantity: Float,
    val dateAdded: Date,
)

open class GroupedShoppingListCount(
    open val group: String,
    open val numberOfItems: Int,
) {
    override fun equals(other: Any?) = other is GroupedShoppingList && group == other.group

    override fun hashCode(): Int {
        return group.hashCode()
    }
}

data class GroupedShoppingList(
    override val group: String,
    val shoppingList: List<ShoppingListItem>,
    override val numberOfItems: Int = shoppingList.size,
) : GroupedShoppingListCount(group, numberOfItems)
