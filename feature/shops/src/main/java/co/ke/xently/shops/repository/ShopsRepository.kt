package co.ke.xently.shops.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.data.getOrNull
import co.ke.xently.data.getOrThrow
import co.ke.xently.source.local.Database
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.ShopService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ShopsRepository @Inject constructor(
    private val database: Database,
    private val service: ShopService,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IShopsRepository {
    override fun add(shop: Shop) = Retry().run {
        flow {
            emit(sendRequest(401) { service.add(shop) })
        }.onEach {
            database.shopDao.add(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun update(shop: Shop) = Retry().run {
        flow {
            emit(sendRequest(401) { service.update(shop.id, shop) })
        }.onEach {
            database.shopDao.add(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun get(id: Long) = Retry().run {
        database.shopDao.get(id).map { shop ->
            if (shop == null) {
                sendRequest(401) { service.get(id) }.apply {
                    getOrNull()?.also {
                        database.shopDao.add(it)
                    }
                }
            } else {
                TaskResult.Success(shop)
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun get(config: PagingConfig, query: String) = Pager(
        config = config,
        remoteMediator = ShopsRemoteMediator(database, service, query),
    ) {
        database.shopDao.run {
            if (query.isBlank()) {
                get()
            } else {
                get("%${query}%")
            }
        }
    }.flow
}