package co.ke.xently.data

import android.location.Location
import androidx.room.Entity
import androidx.room.Index
import co.ke.xently.common.DEFAULT_LOCATION
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "addresses",
    indices = [
        Index("shop")
    ],
    primaryKeys = ["shop", "town", "location"],
)
// TODO: Consider incorporating `android.location.Address`
data class Address(
    val id: Long = -1L,
    val shop: Long = -1L,
    val town: String = "",
    @SerializedName("coordinates")
    val location: Location = DEFAULT_LOCATION,
)