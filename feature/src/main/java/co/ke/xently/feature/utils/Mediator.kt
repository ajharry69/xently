package co.ke.xently.feature.utils

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator
import co.ke.xently.common.TAG
import co.ke.xently.source.remote.getCleansedError

@OptIn(ExperimentalPagingApi::class)
fun getMediatorResultsOrThrow(
    ex: Exception,
    logTag: String = TAG,
): RemoteMediator.MediatorResult.Error {
    Log.e(logTag, "retryCatchConnectionException: ${ex.message}", ex)

    return RemoteMediator.MediatorResult.Error(ex.getCleansedError())
}