package co.ke.xently.shoppinglist

import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.getOrThrow
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.feature.utils.getMediatorResultsOrThrow
import co.ke.xently.shoppinglist.repository.ShoppingListGroup
import co.ke.xently.source.remote.CacheControl
import co.ke.xently.source.remote.sendRequest

internal class ShoppingListRemoteMediator(
    private val group: ShoppingListGroup?,
    private val dependencies: Dependencies,
) : RemoteMediator<Int, ShoppingListItem.WithRelated>() {
    private val queries = buildMap {
        if (group != null && group.groupBy == GroupBy.DateAdded) {
            put("date_added", group.group.toString())
        }
    }
    private val remoteKeyEndpoint = if (group == null) {
        "/api/shopping-list/"
    } else {
        "/api/shopping-list/?${queries.map { it.key + "=" + it.value }.joinToString("&")}"
    }

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
                dependencies.database.remoteKeyDao.get(remoteKeyEndpoint)
            }?.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val response = sendRequest {
                dependencies.service.shoppingList.get(
                    page = page,
                    queries = queries,
                    size = state.config.initialLoadSize,
                    cacheControl = cacheControl.toString(),
                )
            }
            dependencies.database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dependencies.database.remoteKeyDao.delete(remoteKeyEndpoint)
                    dependencies.database.shoppingListDao.deleteAll()
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
