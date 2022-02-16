package co.ke.xently.source.remote

import android.location.Location
import android.net.Uri
import co.ke.xently.common.DEFAULT_LOCATION
import co.ke.xently.common.DEFAULT_SERVER_DATE_TIME_PATTERN
import co.ke.xently.common.Exclude
import co.ke.xently.common.Exclude.During.*
import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

private fun getExclusionStrategy(during: Exclude.During = BOTH): ExclusionStrategy {
    return object : ExclusionStrategy {
        override fun shouldSkipClass(clazz: Class<*>?): Boolean = false

        override fun shouldSkipField(f: FieldAttributes?): Boolean {
            return if (f == null) true else {
                val annotation = f.getAnnotation(Exclude::class.java)
                if (annotation == null) {
                    false
                } else {
                    annotation.during == during
                }
            }
        }
    }
}

val JSON_CONVERTER: Gson = GsonBuilder()
    .enableComplexMapKeySerialization()
    .addSerializationExclusionStrategy(getExclusionStrategy(SERIALIZATION))
    .addDeserializationExclusionStrategy(getExclusionStrategy(DESERIALIZATION))
    .setExclusionStrategies(getExclusionStrategy())
    .serializeNulls()
    .setDateFormat(DEFAULT_SERVER_DATE_TIME_PATTERN)
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    // https://www.javadoc.io/doc/com.google.code.gson/gson/2.8.0/com/google/gson/TypeAdapter.html
    .registerTypeAdapter(Uri::class.java, object : TypeAdapter<Uri>() {
        override fun write(out: JsonWriter?, value: Uri?) {
            out?.value(value?.toString())
        }

        override fun read(`in`: JsonReader?): Uri? {
            val uri = `in`?.nextString() ?: return null
            return Uri.parse(uri)
        }
    }.nullSafe())
    .registerTypeAdapter(Location::class.java, object : TypeAdapter<Location>() {
        override fun write(out: JsonWriter?, location: Location) {
            out?.apply {
                beginArray()
                value(location.latitude)
                value(location.longitude)
                endArray()
            }
        }

        override fun read(`in`: JsonReader?): Location {
            if (`in` == null) return DEFAULT_LOCATION
            `in`.beginArray()
            val lat = `in`.nextDouble()
            val lon = `in`.nextDouble()
            `in`.endArray()
            return DEFAULT_LOCATION.apply {
                latitude = lat
                longitude = lon
            }
        }
    }.nullSafe())
    /*.setPrettyPrinting()
    .setVersion(1.0)*/
    .create()