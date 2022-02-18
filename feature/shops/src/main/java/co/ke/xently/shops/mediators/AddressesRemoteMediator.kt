package co.ke.xently.shops.mediators

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import co.ke.xently.data.Address
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.CancellationException

class AddressesRemoteMediator(
    private val shopId: Long,
    private val dependencies: Dependencies,
    private val query: String? = null,
    private val preLoad: (suspend () -> Unit)? = null,
) : RemoteMediator<Int, Address.WithShop>() {
    private val remoteKeyEndpoint = "/api/shops/${shopId}/addresses/"
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Address.WithShop>,
    ): MediatorResult {
        preLoad?.invoke()
        val page: Int = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.APPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.PREPEND -> dependencies.database.withTransaction {
                dependencies.database.remoteKeyDao.get(remoteKeyEndpoint)
            }?.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val response = sendRequest {
                dependencies.service.shop.getAddresses(shopId, query ?: "", page, state.config.initialLoadSize)
            }
            dependencies.database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dependencies.database.remoteKeyDao.delete(remoteKeyEndpoint)
                    dependencies.database.addressDao.deleteAll()
                }

                response.getOrThrow().run {
                    dependencies.database.remoteKeyDao.save(toRemoteKey(remoteKeyEndpoint))
                    results.run {
                        dependencies.database.addressDao.add(this)
                        MediatorResult.Success(endOfPaginationReached = isEmpty())
                    }
                }
            }
        } catch (ex: Exception) {
            if (ex is CancellationException) throw ex
            MediatorResult.Error(ex)
        }
    }
}