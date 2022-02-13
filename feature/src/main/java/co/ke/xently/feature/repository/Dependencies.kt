package co.ke.xently.feature.repository

import co.ke.xently.common.Dispatcher
import co.ke.xently.source.local.Database
import co.ke.xently.source.local.Preference
import co.ke.xently.source.remote.Service
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class Dependencies @Inject constructor(
    val service: Service,
    val database: Database,
    val preference: Preference,
    val dispatcher: Dispatcher,
)
