package co.ke.xently.products

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import co.ke.xently.data.Product
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.source.remote.sendRequest

internal class ProductsRemoteMediator(private val dependencies: Dependencies) :
    RemoteMediator<Int, Product.WithRelated>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Product.WithRelated>,
    ): MediatorResult {
        val page: Int = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.APPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.PREPEND -> dependencies.database.withTransaction {
                dependencies.database.remoteKeyDao.get(REMOTE_KEY_ENDPOINT)
            }?.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val response = sendRequest(401) {
                dependencies.service.product.get(page, state.config.initialLoadSize)
            }
            dependencies.database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dependencies.database.remoteKeyDao.delete(REMOTE_KEY_ENDPOINT)
                    dependencies.database.productDao.deleteAll()
                }

                response.getOrThrow().run {
                    dependencies.database.remoteKeyDao.save(toRemoteKey(REMOTE_KEY_ENDPOINT))
                    results.run {
                        saveLocallyWithAttributes(dependencies.database)
                        MediatorResult.Success(endOfPaginationReached = isEmpty())
                    }
                }
            }
        } catch (ex: Exception) {
            MediatorResult.Error(ex)
        }
    }

    private companion object {
        private const val REMOTE_KEY_ENDPOINT = "/api/products/"
    }
}