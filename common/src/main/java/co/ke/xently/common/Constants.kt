package co.ke.xently.common

import java.text.SimpleDateFormat
import java.util.*

const val TAG = "Xently"
const val ENCRYPTED_SHARED_PREFERENCE_KEY = "co.ke.xently.ENCRYPTED_SHARED_PREFERENCE_KEY"
const val UNENCRYPTED_SHARED_PREFERENCE_KEY = "co.ke.xently.UNENCRYPTED_SHARED_PREFERENCE_KEY"
const val TOKEN_VALUE_SHARED_PREFERENCE_KEY = "co.ke.xently.TOKEN_VALUE_SHARED_PREFERENCE_KEY"
const val MY_LOCATION_LATITUDE_SHARED_PREFERENCE_KEY =
    "co.ke.xently.MY_LOCATION_LATITUDE_SHARED_PREFERENCE_KEY"
const val MY_LOCATION_LONGITUDE_SHARED_PREFERENCE_KEY =
    "co.ke.xently.MY_LOCATION_LONGITUDE_SHARED_PREFERENCE_KEY"
const val ENABLE_LOCATION_TRACKING_PREFERENCE_KEY =
    "co.ke.xently.ENABLE_LOCATION_TRACKING_PREFERENCE_KEY"

fun isReleaseBuild() = BuildConfig.BUILD_TYPE.lowercase().contains(Regex("^release$"))

val KENYA: Locale = Locale("en", "KE")

val DEFAULT_SERVER_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", KENYA)

/**
 * Format to use when formatting date shown to users
 */
val DEFAULT_LOCAL_DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy", KENYA)

/**
 * Format to use when formatting time shown to users
 */
val DEFAULT_LOCAL_TIME_FORMAT = SimpleDateFormat("h:mm a", KENYA)

/**
 * Format to use when formatting date and time shown to users
 */
val DEFAULT_LOCAL_DATE_TIME_FORMAT = SimpleDateFormat("dd/MM/yyyy h:mm a", KENYA)

fun localDefaultDateFormatToServerDate(date: String): Date? {
    return DEFAULT_SERVER_DATE_FORMAT.parse(DEFAULT_LOCAL_DATE_FORMAT.format(date))
}

fun String.replaceAt(index: Int, replacement: CharSequence): String {
    return replaceRange(index, index + 1, replacement)
}

/*
fun <T: Service> isServiceRunning(context: Context, clazz: Class<T>): Boolean {
    val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (clazz.name == service.service.className) return true
    }
    return false
}*/
