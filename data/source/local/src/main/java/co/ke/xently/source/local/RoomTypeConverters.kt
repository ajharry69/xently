package co.ke.xently.source.local

import androidx.room.TypeConverter
import co.ke.xently.common.utils.JSON_CONVERTER
import com.google.gson.reflect.TypeToken

object RoomTypeConverters {
    class StringListConverter {
        @TypeConverter
        fun stringSetToJsonArray(strs: Set<String>): String = JSON_CONVERTER.toJson(strs)

        @TypeConverter
        fun jsonArrayToStringSet(jsonArray: String): Set<String> =
            JSON_CONVERTER.fromJson(jsonArray, object : TypeToken<Set<String>>() {}.type)
    }
}