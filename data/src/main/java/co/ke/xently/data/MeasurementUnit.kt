package co.ke.xently.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "measurement_units",
    indices = [
        Index("name", unique = true),
        Index("name", "synonym", unique = true),
    ],
)
data class MeasurementUnit(
    @PrimaryKey(autoGenerate = false)
    val id: Long = -1,
    val name: String = "",
    val synonym: Long? = null,
    val isDefault: Boolean = false,
) {
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MeasurementUnit

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun toString() = name

    companion object {
        fun default() = MeasurementUnit(isDefault = true)
    }
}
