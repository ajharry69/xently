package co.ke.xently.source.local

import androidx.room.TypeConverter
import java.util.*

object RoomTypeConverters {
    class DateConverter {
        @TypeConverter
        fun dateToLong(date: Date): Long = date.time

        @TypeConverter
        fun longToData(date: Long): Date = Date(date)
    }
}