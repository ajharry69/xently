package co.ke.xently.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.ke.xently.common.utils.Exclude
import co.ke.xently.common.utils.Exclude.During.SERIALIZATION

@Entity(tableName = "shoppinglist")
data class ShoppingListItem(@Exclude(SERIALIZATION) @PrimaryKey(autoGenerate = false) val id: Long)
