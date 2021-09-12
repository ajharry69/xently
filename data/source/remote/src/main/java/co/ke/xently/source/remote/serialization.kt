package co.ke.xently.source.remote

import co.ke.xently.common.Exclude
import co.ke.xently.common.Exclude.During.*
import com.google.gson.*
import java.text.DateFormat

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
    .setDateFormat(DateFormat.LONG)
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    /*.registerTypeAdapter(Id::class.java, IdTypeAdapter())
    .setPrettyPrinting()
    .setVersion(1.0)*/
    .create()