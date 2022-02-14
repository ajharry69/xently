package co.ke.xently.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import co.ke.xently.common.Exclude

@Entity(
    tableName = "brands",
    indices = [
        Index("productId"),
    ],
)
data class Brand(
    @PrimaryKey(autoGenerate = false)
    val name: String = "",
    @Exclude(Exclude.During.SERIALIZATION)
    val productId: Long = Product.default().id,
) {
    val isDefault: Boolean
        get() = name == ""

    override fun toString() = name

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Brand

        if (name != other.name) return false

        return true
    }

    companion object {
        fun default() = Brand()
    }
}