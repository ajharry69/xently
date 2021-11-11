package co.ke.xently.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.ke.xently.common.Exclude

@Entity(tableName = "shops")
data class Shop(
    @Exclude(Exclude.During.SERIALIZATION)
    @PrimaryKey(autoGenerate = false)
    val id: Long = -1L,
    val name: String = "",
//    @Ignore
//    val addresses: List<Address> = emptyList(),
)
