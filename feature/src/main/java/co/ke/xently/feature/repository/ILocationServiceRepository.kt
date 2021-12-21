package co.ke.xently.feature.repository

import co.ke.xently.data.TaskResult
import kotlinx.coroutines.flow.Flow

interface ILocationServiceRepository {
    fun getLocationTrackingPref(): Boolean

    fun saveLocationTrackingPref(requestLocationUpdates: Boolean)

    fun updateLocation(location: Array<Double>): Flow<TaskResult<Unit>>
}