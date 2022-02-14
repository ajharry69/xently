package co.ke.xently.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import co.ke.xently.common.Exclude

@Entity(
    tableName = "attributes",
    indices = [
        Index("name"),
        Index("value"),
        Index("productId"),
    ],
    primaryKeys = ["name", "value"],
)
data class Attribute(
    var name: String = "",
    var value: String = "",
    @Exclude
    var productId: Long = Product.default().id,
    @Ignore
    val values: List<String> = emptyList(),
    @Exclude
    @Ignore
    val isDefault: Boolean = false,
) {
    override fun toString() = "${name}:${value}"

    companion object {
        fun default() = Attribute(isDefault = true)
    }
}