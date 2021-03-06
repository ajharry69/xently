package co.ke.xently.products

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import co.ke.xently.data.Product
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.feature.utils.getMediatorResultsOrThrow
import co.ke.xently.source.remote.CacheControl
import co.ke.xently.source.remote.sendRequest

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
                if (shopId == null) {
                    dependencies.service.product.get(
                        query = query,
                        page = page,
                        size = state.config.initialLoadSize,
                        cacheControl = cacheControl.toString(),
                    )
                } else {
                    dependencies.service.shop.getProducts(
                        shopId = shopId,
                        query = query,
                        page = page,
                        size = state.config.initialLoadSize,
                        cacheControl = cacheControl.toString(),
                    )
                }
            }
            dependencies.database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dependencies.database.remoteKeyDao.delete(remoteKeyEndpoint)
                    dependencies.database.productDao.run {
                        if (shopId == null) {
                            deleteAll()
                        } else {
                            deleteAll(shopId)
                        }
                    }
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
            getMediatorResultsOrThrow(ex)
        }
    }
}