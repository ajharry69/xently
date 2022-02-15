package co.ke.xently.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import co.ke.xently.common.Exclude
import co.ke.xently.common.Exclude.During.SERIALIZATION
import java.util.*

@Entity(tableName = "shoppinglist")
data class ShoppingListItem(
    @PrimaryKey(autoGenerate = false)
    var id: Long = -1L,
    var name: String = "",
    var unit: String = "",
    var unitQuantity: Float = 1f,
    var purchaseQuantity: Float = 1f,
    @Exclude(SERIALIZATION)
    var dateAdded: Date = Date(),
    @Ignore
    val brands: List<Brand> = emptyList(),
    @Ignore
    val attributes: List<Attribute> = emptyList(),
    @Ignore
    @Exclude
    val isDefault: Boolean = false,
) {
    override fun toString() =
        "${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}, ${unitQuantity}${unit} - $purchaseQuantity"

    companion object {
        fun default() = ShoppingListItem(isDefault = true)
    }
}