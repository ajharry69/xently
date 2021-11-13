package co.ke.xently.feature.repository

import kotlinx.coroutines.flow.Flow

interface ILocationServiceRepository {
    fun getLocationTrackingPref(): Boolean

    fun saveLocationTrackingPref(requestLocationUpdates: Boolean)

    fun updateLocation(location: Array<Double>): Flow<Result<Unit>>
}