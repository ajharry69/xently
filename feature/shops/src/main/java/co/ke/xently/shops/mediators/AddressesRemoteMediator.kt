package co.ke.xently.shops.mediators

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import co.ke.xently.data.Address
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.feature.utils.getMediatorResultsOrThrow
import co.ke.xently.source.remote.CacheControl
import co.ke.xently.source.remote.sendRequest

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
        var cacheControl: CacheControl = CacheControl.OnlyIfCached
        val page: Int = when (loadType) {
            LoadType.REFRESH -> {
                cacheControl = CacheControl.NoCache
                1
            }
            LoadType.APPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.PREPEND -> dependencies.database.withTransaction {
                dependencies.database.remoteKeyDao.get(remoteKeyEndpoint)
            }?.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val response = sendRequest {
                dependencies.service.shop.getAddresses(
                    shopId = shopId,
                    query = query ?: "",
                    page = page,
                    size = state.config.initialLoadSize,
                    cacheControl = cacheControl.toString(),
                )
            }
            dependencies.database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dependencies.database.remoteKeyDao.delete(remoteKeyEndpoint)
                    dependencies.database.addressDao.deleteAll(shopId)
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
            getMediatorResultsOrThrow(ex)
        }
    }
}