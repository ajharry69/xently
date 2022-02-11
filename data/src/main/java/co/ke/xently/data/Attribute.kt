package co.ke.xently.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index

@Entity(
    tableName = "attributes",
    indices = [
        Index("name"),
        Index("value"),
        Index("name", "value", "productId", unique = true),
    ],
    primaryKeys = ["name", "value"],
)
data class Attribute(
    var name: String = "",
    var value: String = "",
    var productId: Long = -1,
    @Ignore
    val values: List<String> = emptyList(),
    var isDefault: Boolean = false,
) {
    override fun toString() = "${name}:${value}"

    companion object {
        fun default() = Attribute(isDefault = true)
    }
}