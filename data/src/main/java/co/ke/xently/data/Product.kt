package co.ke.xently.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = false) val id: Long = -1L,
    val name: String = "",
    val unit: String = "",
    val unitQuantity: Float = 0f,
//    val shop: Long,
    val unitPrice: Float = 0f,
    val datePurchased: Date = Date(),
    val dateAdded: Date = Date(),
)
