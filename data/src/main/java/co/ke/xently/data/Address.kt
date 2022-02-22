package co.ke.xently.data

import android.location.Location
import androidx.room.*
import co.ke.xently.common.DEFAULT_LOCATION
import co.ke.xently.common.Exclude
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "addresses",
    indices = [
        Index("shopId"),
        Index("location"),
        Index("shopId", "town", "location", unique = true),
    ],
)
// TODO: Consider incorporating `android.location.Address`
data class Address(
    @PrimaryKey(autoGenerate = false)
    var id: Long = -1L,
    var town: String = "",
    @SerializedName("shop")
    @Exclude(Exclude.During.SERIALIZATION)
    var shopId: Long = -1L,
    @Ignore
    @Exclude
    val shop: Shop = Shop.default(),
    @SerializedName("coordinates")
    var location: Location = DEFAULT_LOCATION,
) {
    override fun toString(): String {
        val s = StringBuilder()
        if (town.isNotBlank()) {
            s.append("$town, ")
        }
        return s.append(String.format("%.4f", location.longitude)).append(",")
            .append(String.format("%.4f", location.latitude)).replace(Regex(",\\s+$"), "")
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + town.hashCode()
        result = 31 * result + location.latitude.hashCode()
        result = 31 * result + location.longitude.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Address

        if (id != other.id) return false
        if (town != other.town) return false
        if (location.latitude != other.location.latitude) return false
        if (location.longitude != other.location.longitude) return false

        return true
    }

    data class WithShop(
        @Embedded
        val a: Address,
        @Relation(parentColumn = "shopId", entityColumn = "id")
        val shop: Shop,
    ) {
        @Ignore
        val address = a.copy(shop = shop)
    }
}