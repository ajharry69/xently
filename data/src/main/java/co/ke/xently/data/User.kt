package co.ke.xently.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import co.ke.xently.common.Exclude

@Entity(tableName = "accounts", indices = [
    Index("email", unique = true),
])
data class User(
    @PrimaryKey
    @Exclude(Exclude.During.SERIALIZATION)
    var id: Long = -1,
    var email: String = "",
    @Exclude(Exclude.During.SERIALIZATION)
    var isVerified: Boolean = false,
    @Exclude(Exclude.During.SERIALIZATION)
    var photo: Uri? = null,
    @Ignore
    @Exclude(Exclude.During.SERIALIZATION)
    var token: String? = null,
    @Ignore
    @Exclude(Exclude.During.DESERIALIZATION)
    val password: String? = null,
    @Exclude
    var isActive: Boolean = false,
    @Ignore
    @Exclude
    val isDefault: Boolean = false,
) {
    data class ResetPassword(
        val oldPassword: String,
        val newPassword: String,
        val isChange: Boolean = false, // TODO: Rename to `considerAsChange`...
    )

    companion object {
        fun default(): User {
            return User(-1, "", isDefault = true)
        }
    }
}
