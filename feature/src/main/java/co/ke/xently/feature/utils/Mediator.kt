package co.ke.xently.feature.utils

import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator
import co.ke.xently.source.remote.DEFAULT_CONNECT_EXCEPTION_MESSAGE
import kotlinx.coroutines.CancellationException
import java.net.ConnectException


@OptIn(ExperimentalPagingApi::class)
fun getMediatorResultsOrThrow(ex: Exception): RemoteMediator.MediatorResult.Error {
    val error = when (ex) {
        is CancellationException -> {
            throw ex
        }
        is ConnectException -> {
            ConnectException(DEFAULT_CONNECT_EXCEPTION_MESSAGE).apply {
                initCause(ex)
            }
        }
        else -> {
            ex
        }
    }
    return RemoteMediator.MediatorResult.Error(error)
}