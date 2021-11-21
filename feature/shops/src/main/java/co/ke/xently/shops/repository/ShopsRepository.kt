package co.ke.xently.shops.repository

import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.*
import co.ke.xently.source.local.Database
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.ShopService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ShopsRepository @Inject constructor(
    private val database: Database,
    private val service: ShopService,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IShopsRepository {
    override fun addShop(shop: Shop) = Retry().run {
        flow {
            emit(sendRequest(401) { service.add(shop) })
        }.onEach {
            database.shopsDao.save(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun updateShop(shop: Shop) = Retry().run {
        flow {
            emit(sendRequest(401) { service.update(shop.id, shop) })
        }.onEach {
            database.shopsDao.save(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun getShop(id: Long) = Retry().run {
        database.shopsDao.get(id).map { shop ->
            if (shop == null) {
                sendRequest(401) { service.get(id) }.apply {
                    getOrNull()?.also {
                        database.shopsDao.save(it)
                    }
                }
            } else {
                TaskResult.Success(shop)
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun getShopList(remote: Boolean): Flow<TaskResult<List<Shop>>> = Retry().run {
        if (remote) {
            flow { emit(sendRequest(401) { service.get() }) }
                .map { result ->
                    result.mapCatching {
                        it.results.apply {
                            database.shopsDao.save(*toTypedArray())
                        }
                    }
                }
                .retryCatchIfNecessary(this)
                .flowOn(ioDispatcher)
                .onCompletion {
                    // Return cached records. Caveats:
                    //  1. No error propagation
                    if (it == null) emitAll(getShopList(false))
                }
        } else {
            database.shopsDao.get().map { shops ->
                TaskResult.Success(shops)
            }
        }
    }
}