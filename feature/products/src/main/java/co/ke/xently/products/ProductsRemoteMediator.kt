package co.ke.xently.products

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import co.ke.xently.data.Product
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.CancellationException

internal class ProductsRemoteMediator(
    private val dependencies: Dependencies,
    private val shopId: Long? = null,
    private val query: String = "",
) : RemoteMediator<Int, Product.WithRelated>() {
    private val remoteKeyEndpoint = if (shopId == null) {
        "/api/products/"
    } else {
        "/api/shops/$shopId/products/"
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Product.WithRelated>,
    ): MediatorResult {
        val page: Int = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.APPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.PREPEND -> dependencies.database.withTransaction {
                dependencies.database.remoteKeyDao.get(remoteKeyEndpoint)
            }?.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val response = sendRequest {
                if (shopId == null) {
                    dependencies.service.product.get(query, page, state.config.initialLoadSize)
                } else {
                    dependencies.service.shop.getProducts(
                        shopId = shopId,
                        query = query,
                        page = page,
                        size = state.config.initialLoadSize,
                    )
                }
            }
            dependencies.database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dependencies.database.remoteKeyDao.delete(remoteKeyEndpoint)
                    dependencies.database.productDao.deleteAll()
                }

                response.getOrThrow().run {
                    dependencies.database.remoteKeyDao.save(toRemoteKey(remoteKeyEndpoint))
                    results.run {
                        saveLocallyWithAttributes(dependencies)
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