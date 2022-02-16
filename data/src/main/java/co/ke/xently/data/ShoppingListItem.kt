package co.ke.xently.data

import androidx.room.*
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
    data class WithRelated(
        @Embedded
        val i: ShoppingListItem,
        @Relation(parentColumn = "id", entityColumn = "relatedId")
        val brands: List<Brand>,
        @Relation(parentColumn = "id", entityColumn = "relatedId")
        val attributes: List<Attribute>,
    ) {
        @Ignore
        val item = i.copy(
            brands = brands,
            attributes = attributes,
        )
    }

    @Entity(
        tableName = "shoppinglist_attributes",
        indices = [
            Index("name"),
            Index("value"),
            Index("relatedId"),
        ],
        primaryKeys = ["name", "value"],
    )
    data class Attribute(
        override var name: String = "",
        override var value: String = "",
        @Exclude
        var relatedId: Long = ShoppingListItem.default().id,
        @Ignore
        val values: List<String> = emptyList(),
        @Exclude
        @Ignore
        val isDefault: Boolean = false,
    ) : AbstractAttribute() {
        companion object {
            fun default() = Attribute(isDefault = true)
        }
    }

    @Entity(
        tableName = "shoppinglist_brands",
        indices = [
            Index("name"),
            Index("relatedId"),
        ],
        primaryKeys = ["name", "relatedId"],
    )
    data class Brand(
        override var name: String = "",
        @Exclude(SERIALIZATION)
        val relatedId: Long = ShoppingListItem.default().id,
    ) : AbstractBrand() {
        companion object {
            fun default() = Brand()
        }
    }

    override fun toString() =
        "${name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}, ${unitQuantity}${unit} - $purchaseQuantity"

    companion object {
        fun default() = ShoppingListItem(isDefault = true)
    }
}