package co.ke.xently.products.repository

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import co.ke.xently.data.Product
import co.ke.xently.data.getOrThrow
import co.ke.xently.source.local.Database
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.ProductService

internal class ProductsRemoteMediator(
    private val database: Database,
    private val service: ProductService,
) : RemoteMediator<Int, Product.WithRelated>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Product.WithRelated>,
    ): MediatorResult {
        val page: Int = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.APPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.PREPEND -> database.withTransaction {
                database.remoteKeyDao.get(REMOTE_KEY_ENDPOINT)
            }?.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val response = sendRequest(401) {
                service.get(page, state.config.initialLoadSize)
            }
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeyDao.delete(REMOTE_KEY_ENDPOINT)
                    database.productDao.deleteAll()
                }

                response.getOrThrow().run {
                    database.remoteKeyDao.save(toRemoteKey(REMOTE_KEY_ENDPOINT))
                    results.run {
                        saveLocallyWithAttributes(database)
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