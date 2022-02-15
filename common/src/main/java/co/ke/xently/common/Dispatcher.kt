package co.ke.xently.common

import android.os.Looper
import co.ke.xently.common.di.qualifiers.coroutines.ComputationDispatcher
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.common.di.qualifiers.coroutines.UIDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class Dispatcher @Inject constructor(
    val looper: Looper?,
    @IODispatcher
    val io: CoroutineDispatcher,
    @UIDispatcher
    val main: CoroutineDispatcher,
    @ComputationDispatcher
    val computation: CoroutineDispatcher,
)
