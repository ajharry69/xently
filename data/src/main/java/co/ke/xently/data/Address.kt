package co.ke.xently.data

import android.location.Location
import androidx.room.*
import co.ke.xently.common.DEFAULT_LOCATION
import co.ke.xently.common.Exclude
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "addresses",
    indices = [
        Index("shopId")
    ],
    primaryKeys = ["shopId", "town", "location"],
)
// TODO: Consider incorporating `android.location.Address`
data class Address(
    var id: Long = -1L,
    var town: String = "",
    @SerializedName("shop")
    var shopId: Long = -1L,
    @Ignore
    @Exclude
    val shop: Shop = Shop.default(),
    @SerializedName("coordinates")
    var location: Location = DEFAULT_LOCATION,
) {
    override fun toString(): String {
        return "${town}, ${String.format("%.4f", location.latitude)},${
            String.format("%.4f",
                location.longitude)
        }".replace(Regex("(^\\s*,\\s+)|(\\s*,\\s+$)"), "")
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