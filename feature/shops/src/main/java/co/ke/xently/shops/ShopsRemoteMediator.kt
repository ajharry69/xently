package co.ke.xently.shops

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import co.ke.xently.data.Shop
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.source.remote.sendRequest

class ShopsRemoteMediator(
    private val dependencies: Dependencies,
    private val query: String? = null,
    private val preLoad: (suspend () -> Unit)? = null,
) : RemoteMediator<Int, Shop>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, Shop>): MediatorResult {
        preLoad?.invoke()
        val page: Int = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.APPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.PREPEND -> dependencies.database.withTransaction {
                dependencies.database.remoteKeyDao.get(REMOTE_KEY_ENDPOINT)
            }?.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val response = sendRequest(401) {
                dependencies.service.shop.get(query ?: "", page, state.config.initialLoadSize)
            }
            dependencies.database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dependencies.database.remoteKeyDao.delete(REMOTE_KEY_ENDPOINT)
                    dependencies.database.shopDao.deleteAll()
                }

                response.getOrThrow().run {
                    dependencies.database.remoteKeyDao.save(toRemoteKey(REMOTE_KEY_ENDPOINT))
                    results.run {
                        dependencies.database.shopDao.add(this)
                        MediatorResult.Success(endOfPaginationReached = isEmpty())
                    }
                }
            }
        } catch (ex: Exception) {
            MediatorResult.Error(ex)
        }
    }

    private companion object {
        private const val REMOTE_KEY_ENDPOINT = "/api/shops/"
    }
}