package co.ke.xently.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "brands",
    indices = [
        Index("name"),
        Index("name", "productId", unique = true),
    ],
)
data class Brand(
    @PrimaryKey(autoGenerate = false)
    val id: Long = -1,
    val name: String = "",
    val productId: Long = -1,
    val isDefault: Boolean = false,
) {
    override fun toString() = name

    companion object {
        fun default() = Brand(isDefault = true)
    }
}