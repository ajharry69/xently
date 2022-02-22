package co.ke.xently.source.local

import android.location.Location
import android.net.Uri
import androidx.room.TypeConverter
import co.ke.xently.common.DEFAULT_LOCATION
import java.util.*

object RoomTypeConverters {
    class DateConverter {
        @TypeConverter
        fun dateToLong(date: Date): Long = date.time

        @TypeConverter
        fun longToDate(date: Long): Date = Date(date)
    }

    class UriConverter {
        @TypeConverter
        fun uriToString(uri: Uri?): String? = uri?.toString()

        @TypeConverter
        fun stringToUri(uri: String?): Uri? = uri?.run { Uri.parse(this) }
    }

    class LocationConverter {
        @TypeConverter
        fun locationToString(location: Location): String =
            "${location.longitude},${location.latitude}"

        @TypeConverter
        fun stringToLocation(location: String): Location = DEFAULT_LOCATION.apply {
            location.split(",").also {
                longitude = it[0].toDouble()
                latitude = it[1].toDouble()
            }
        }
    }
}