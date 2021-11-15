package co.ke.xently.feature.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import co.ke.xently.common.ENABLE_LOCATION_TRACKING_PREFERENCE_KEY
import co.ke.xently.common.MY_LOCATION_LATITUDE_SHARED_PREFERENCE_KEY
import co.ke.xently.common.MY_LOCATION_LONGITUDE_SHARED_PREFERENCE_KEY
import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.EncryptedSharedPreference
import co.ke.xently.common.di.qualifiers.UnencryptedSharedPreference
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.LocationUpdateService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LocationServiceRepository @Inject constructor(
    private val service: LocationUpdateService,
    @EncryptedSharedPreference
    private val sharedPreference: SharedPreferences,
    @UnencryptedSharedPreference
    private val unencryptedPreference: SharedPreferences,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ILocationServiceRepository {
    override fun saveLocationTrackingPref(requestLocationUpdates: Boolean) {
        unencryptedPreference.edit {
            putBoolean(ENABLE_LOCATION_TRACKING_PREFERENCE_KEY, requestLocationUpdates)
        }
    }

    override fun getLocationTrackingPref() =
        unencryptedPreference.getBoolean(ENABLE_LOCATION_TRACKING_PREFERENCE_KEY, false)

    override fun updateLocation(location: Array<Double>) = Retry().run {
        flow {
            emit(sendRequest(401) { service.updateLocation(location = location) })
        }.onEach {
            sharedPreference.edit(commit = true) {
                putString(MY_LOCATION_LATITUDE_SHARED_PREFERENCE_KEY, location.component1().toString())
                putString(MY_LOCATION_LONGITUDE_SHARED_PREFERENCE_KEY, location.component2().toString())
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }


}