package co.ke.xently.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import co.ke.xently.common.Exclude
import com.google.gson.annotations.SerializedName

@Entity(tableName = "shops")
data class Shop(
    @PrimaryKey(autoGenerate = false)
    val id: Long = -1,
    val name: String = "",
    val taxPin: String = "",
    val descriptiveName: String = "",
    @Exclude(Exclude.During.SERIALIZATION)
    val productsCount: Int = 0,
    val town: String = "",
    @Embedded(prefix = "shops_")
    @SerializedName("coordinates")
    val coordinate: Coordinate? = null,
    @Exclude
    val isDefault: Boolean = false,
) {
    data class Coordinate(val lat: Double, val lon: Double) {
        override fun toString(): String {
            return "${lat},${lon}"
        }
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
            descriptiveName = "Xently electronic store, Westlands, Nairobi - P000111222Z",
            name = "Xently electronic store",
            taxPin = "P000111222Z",
            town = "Westlands, Nairobi",
            isDefault = true,
        )
    }
}
