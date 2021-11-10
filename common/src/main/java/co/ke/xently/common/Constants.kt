package co.ke.xently.common

const val ENCRYPTED_SHARED_PREFERENCE_KEY = "ENCRYPTED_SHARED_PREFERENCE_KEY"
const val UNENCRYPTED_SHARED_PREFERENCE_KEY = "UNENCRYPTED_SHARED_PREFERENCE_KEY"
const val TOKEN_VALUE_SHARED_PREFERENCE_KEY = "TOKEN_VALUE_SHARED_PREFERENCE_KEY"
const val LATITUDE_SHARED_PREFERENCE_KEY = "LATITUDE_SHARED_PREFERENCE_KEY"
const val LONGITUDE_SHARED_PREFERENCE_KEY = "LONGITUDE_SHARED_PREFERENCE_KEY"
const val ENABLE_LOCATION_TRACKING_PREFERENCE_KEY = "ENABLE_LOCATION_TRACKING_PREFERENCE_KEY"

fun isReleaseBuild() = BuildConfig.BUILD_TYPE.lowercase().contains(Regex("^release$"))

/*
fun <T: Service> isServiceRunning(context: Context, clazz: Class<T>): Boolean {
    val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (clazz.name == service.service.className) return true
    }
    return false
}*/
