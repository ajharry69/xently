package co.ke.xently.shoppinglist

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.feature.utils.getMediatorResultsOrThrow
import co.ke.xently.source.remote.CacheControl
import co.ke.xently.source.remote.sendRequest

internal class ShoppingListRemoteMediator(
    private val dependencies: Dependencies,
) : RemoteMediator<Int, ShoppingListItem.WithRelated>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ShoppingListItem.WithRelated>,
    ): MediatorResult {
        var cacheControl: CacheControl = CacheControl.OnlyIfCached
        val page: Int = when (loadType) {
            LoadType.REFRESH -> {
                cacheControl = CacheControl.NoCache
                1
            }
            LoadType.APPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.PREPEND -> dependencies.database.withTransaction {
                dependencies.database.remoteKeyDao.get(REMOTE_KEY_ENDPOINT)
            }?.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val response = sendRequest {
                dependencies.service.shoppingList.get(
                    page = page,
                    size = state.config.initialLoadSize,
                    cacheControl = cacheControl.toString(),
                )
            }
            dependencies.database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dependencies.database.remoteKeyDao.delete(REMOTE_KEY_ENDPOINT)
                    dependencies.database.shoppingListDao.deleteAll()
                }

                response.getOrThrow().run {
                    dependencies.database.remoteKeyDao.save(toRemoteKey(REMOTE_KEY_ENDPOINT))
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

    private companion object {
        private const val REMOTE_KEY_ENDPOINT = "/api/shopping-list/"
    }
}
