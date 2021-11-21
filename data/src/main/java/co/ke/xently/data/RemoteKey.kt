package co.ke.xently.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKey(
    @PrimaryKey(autoGenerate = false)
    val endpoint: String = "",
    val prevPage: Int? = null,
    val nextPage: Int? = null,
    val totalItems: Int = 0,
) {
    init {
        assert(endpoint.isNotBlank()) {
            "Endpoint is an identifier - it must not be blank"
        }
    }
}
