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
    @Exclude(SERIALIZATION)
    val dateAdded: Date = Date(),
    @Exclude(SERIALIZATION)
    val naturalInput: String = "",
) {
    override fun toString() = if (naturalInput.isNotBlank()) naturalInput else {
        "${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}, ${unitQuantity}${unit} - $purchaseQuantity"
    }
}