package co.ke.xently.shops.repository

import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.Shop
import co.ke.xently.source.local.daos.ShopsDao
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
    private val dao: ShopsDao,
    private val service: ShopService,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IShopsRepository {
    override fun addShop(shop: Shop) = Retry().run {
        flow {
            emit(sendRequest(401) { service.addShop(shop) })
        }.onEach {
            dao.addShops(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun updateShop(shop: Shop) = Retry().run {
        flow {
            emit(sendRequest(401) { service.updateShop(shop.id, shop) })
        }.onEach {
            dao.addShops(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun getShop(id: Long) = Retry().run {
        dao.getShop(id).map { shop ->
            if (shop == null) {
                sendRequest(401) { service.getShop(id) }.apply {
                    getOrNull()?.also {
                        dao.addShops(it)
                    }
                }
            } else {
                Result.success(shop)
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun getShopList(remote: Boolean): Flow<Result<List<Shop>>> = Retry().run {
        if (remote) {
            flow { emit(sendRequest(401) { service.getShopList() }) }
                .map { result ->
                    result.mapCatching {
                        it.results.apply {
                            dao.addShops(*toTypedArray())
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
            dao.getShopList().map { shops ->
                Result.success(shops)
            }
        }
    }
}