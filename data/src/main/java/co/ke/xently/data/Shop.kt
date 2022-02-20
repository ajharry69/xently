package co.ke.xently.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import co.ke.xently.common.Exclude

@Entity(tableName = "shops")
data class Shop(
    @PrimaryKey(autoGenerate = false)
    var id: Long = -1,
    var name: String = "",
    var taxPin: String = "",
    @Exclude(Exclude.During.SERIALIZATION)
    var productsCount: Int = 0,
    @Exclude(Exclude.During.SERIALIZATION)
    var addressesCount: Int = 0,
    @Ignore
    val addresses: List<Address> = emptyList(),
    @Ignore
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
