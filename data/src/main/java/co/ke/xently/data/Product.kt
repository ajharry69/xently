package co.ke.xently.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(@PrimaryKey(autoGenerate = false) val id: Long = -1L, val name: String = "")
