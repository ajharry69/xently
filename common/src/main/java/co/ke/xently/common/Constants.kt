package co.ke.xently.common

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

/*
fun <T: Service> isServiceRunning(context: Context, clazz: Class<T>): Boolean {
    val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (clazz.name == service.service.className) return true
    }
    return false
}*/
