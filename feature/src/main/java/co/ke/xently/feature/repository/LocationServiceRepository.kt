package co.ke.xently.feature.repository

import androidx.core.content.edit
import co.ke.xently.common.ENABLE_LOCATION_TRACKING_PREFERENCE_KEY
import co.ke.xently.common.MY_LOCATION_LATITUDE_SHARED_PREFERENCE_KEY
import co.ke.xently.common.MY_LOCATION_LONGITUDE_SHARED_PREFERENCE_KEY
import co.ke.xently.common.Retry
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LocationServiceRepository @Inject constructor(private val dependencies: Dependencies) :
    ILocationServiceRepository {
    override fun saveLocationTrackingPref(requestLocationUpdates: Boolean) {
        dependencies.preference.unencrypted.edit {
            putBoolean(ENABLE_LOCATION_TRACKING_PREFERENCE_KEY, requestLocationUpdates)
        }
    }

    override fun getLocationTrackingPref() =
        dependencies.preference.unencrypted.getBoolean(ENABLE_LOCATION_TRACKING_PREFERENCE_KEY,
            false)

    override fun updateLocation(location: Array<Double>) = Retry().run {
        flow {
            // TODO: Add user ID to location update...
            emit(sendRequest(401) { dependencies.service.account.update(location = location) })
        }.onEach {
            dependencies.preference.encrypted.edit(commit = true) {
                putString(
                    MY_LOCATION_LATITUDE_SHARED_PREFERENCE_KEY,
                    location.component1().toString()
                )
                putString(
                    MY_LOCATION_LONGITUDE_SHARED_PREFERENCE_KEY,
                    location.component2().toString()
                )
            }
        }.retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }

}