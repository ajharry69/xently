package co.ke.xently.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.ke.xently.common.Exclude

@Entity(tableName = "shops")
data class Shop(
    @Exclude(Exclude.During.SERIALIZATION)
    @PrimaryKey(autoGenerate = false)
    val id: Long = -1,
    val name: String = "",
    val taxPin: String = "",
    val productsCount: Int = 0,
    val addressesCount: Int = 0,
    val isDefault: Boolean = false,
) {
    override fun toString(): String {
        return "${name}, $taxPin".replace(Regex("(^\\s*,\\s+)|(\\s*,\\s+$)"), "")
    }

    companion object {
        fun default() = Shop(isDefault = true)
    }
}
