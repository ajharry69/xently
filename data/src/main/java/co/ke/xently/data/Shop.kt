package co.ke.xently.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.ke.xently.common.Exclude

@Entity(tableName = "shops")
data class Shop(
    @PrimaryKey(autoGenerate = false)
    val id: Long = -1,
    val name: String = "",
    val taxPin: String = "",
    @Exclude(Exclude.During.SERIALIZATION)
    val productsCount: Int = 0,
    @Exclude(Exclude.During.SERIALIZATION)
    val addressesCount: Int = 0,
    @Exclude
    val isDefault: Boolean = false,
) {
    override fun toString(): String {
        return "${name}, $taxPin".replace(Regex("(^\\s*,\\s+)|(\\s*,\\s+$)"), "")
    }

    companion object {
        fun default() = Shop(isDefault = true)
    }
}
