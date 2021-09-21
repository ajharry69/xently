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
    val id: Long = -1,
    val name: String = "",
    val unit: String = "piece",
    val unitQuantity: Float = 1f,
    val purchaseQuantity: Float = 1f,
    val dateAdded: Date = Date(),
) {
    override fun toString() =
        "${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}, ${unitQuantity}${unit} - $purchaseQuantity"
}

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


data class ShoppingListRecommendation(
    val lookupDuration: Float = 0f,
    val count: Count = Count(),
    val recommendations: List<Recommendation> = emptyList(),
    // TODO: Add missed items...
) {
    data class Count(val hitItems: Int = 0, val shopsVisited: Int = 0)

    data class Recommendation(
        val id: Long = -1L,
        val name: String = "",
        val taxPin: String = "",
        val hits: Hits = Hits(),
        val addresses: List<Address> = emptyList(),
    ) {
        data class Address(val id: Long = -1L)

        data class Hits(
            val count: Int = 0,
            val totalPrice: Float = 0f,
            val item: List<ShoppingListItem> = emptyList(),
        )
    }
}
