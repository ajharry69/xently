package co.ke.xently.data

import androidx.room.*
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
    data class WithAddresses(
        @Embedded
        val s: Shop,
        @Relation(entityColumn = "shopId", parentColumn = "id")
        val addresses: List<Address>,
    ) {
        @Ignore
        val shop = s.copy(addresses = addresses)
    }

    override fun toString(): String {
        val s = StringBuilder()
        if (name.isNotBlank()) {
            s.append("${name}, ")
        }
        return s.append(taxPin).replace(Regex(",\\s+$"), "")
    }

    companion object {
        fun default() = Shop(
            name = "Xently electronic store",
            taxPin = "P000111222Z",
            isDefault = true,
        )
    }
}
