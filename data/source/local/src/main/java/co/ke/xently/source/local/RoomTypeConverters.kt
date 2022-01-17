package co.ke.xently.source.local

import android.net.Uri
import androidx.room.TypeConverter
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
        fun uriToString(uri: Uri): String = uri.toString()

        @TypeConverter
        fun stringToUri(uri: String): Uri = Uri.parse(uri)
    }
}