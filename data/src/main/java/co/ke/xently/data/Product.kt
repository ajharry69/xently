package co.ke.xently.data

import androidx.room.*
import co.ke.xently.common.Exclude
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(
    tableName = "products",
    indices = [
        Index("shopId"),
    ],
)
data class Product(
    @PrimaryKey(autoGenerate = false) var id: Long = -1L,
    var name: String = "",
    var unit: String = "",
    var unitQuantity: Float = 1f,
    var purchasedQuantity: Float = 1f,
    @Exclude
    @Ignore
    val shop: Shop = Shop.default(),
    @SerializedName("shop")
    var shopId: Long = -1L,
    var unitPrice: Float = 0f,
    var datePurchased: Date = Date(),
    @Exclude(Exclude.During.SERIALIZATION)
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
        val p: Product,
        @Relation(parentColumn = "shopId", entityColumn = "id")
        val shop: Shop?,
        @Relation(parentColumn = "id", entityColumn = "relatedId")
        val brands: List<Brand>,
        @Relation(parentColumn = "id", entityColumn = "relatedId")
        val attributes: List<Attribute>,
    ) {
        @Ignore
        val product = p.copy(
            shop = shop ?: Shop.default(),
            brands = brands,
            attributes = attributes,
        )
    }

    @Entity(
        tableName = "product_attributes",
        indices = [
            Index("name"),
            Index("value"),
            Index("relatedId"),
        ],
        primaryKeys = ["name", "value", "relatedId"],
    )
    data class Attribute(
        override var name: String = "",
        override var value: String = "",
        @Exclude
        var relatedId: Long = Product.default().id,
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
        tableName = "product_brands",
        indices = [
            Index("name"),
            Index("relatedId"),
        ],
        primaryKeys = ["name", "relatedId"],
    )
    data class Brand(
        override var name: String = "",
        @Exclude(Exclude.During.SERIALIZATION)
        val relatedId: Long = Product.default().id,
    ) : AbstractBrand() {
        companion object {
            fun default() = Brand()
        }
    }

    override fun toString(): String {
        return "${name}, $unitQuantity $unit"
    }

    companion object {
        fun default(): Product = Product(isDefault = true)
    }
}
