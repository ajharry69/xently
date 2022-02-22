package co.ke.xently.shops.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import co.ke.xently.common.Retry
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.shops.mediators.AddressesRemoteMediator
import co.ke.xently.shops.mediators.ShopsRemoteMediator
import co.ke.xently.source.remote.retryCatch
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ShopsRepository @Inject constructor(private val dependencies: Dependencies) :
    IShopsRepository {
    private fun Flow<TaskResult<Shop>>.doTaskWhileSavingEachLocally(retry: Retry) =
        onEach { result ->
            result.getOrNull()?.also {
                dependencies.database.shopDao.add(it)
            }
        }.retryCatch(retry).flowOn(dependencies.dispatcher.io)

    override fun add(shop: Shop) = Retry().run {
        flow {
            emit(sendRequest { dependencies.service.shop.add(shop) })
        }.doTaskWhileSavingEachLocally(this)
    }

    override fun update(shop: Shop) = Retry().run {
        flow {
            emit(sendRequest { dependencies.service.shop.update(shop.id, shop) })
        }.doTaskWhileSavingEachLocally(this)
    }

    override fun get(id: Long) = Retry().run {
        dependencies.database.shopDao.get(id).map { shop ->
            if (shop == null) {
                sendRequest { dependencies.service.shop.get(id) }.apply {
                    getOrNull()?.also {
                        dependencies.database.shopDao.add(it)
                    }
                }
            } else {
                TaskResult.Success(shop.shop)
            }
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }

    override fun get(config: PagingConfig, query: String) = Pager(
        config = config,
        remoteMediator = ShopsRemoteMediator(dependencies, query),
    ) {
        dependencies.database.shopDao.run {
            if (query.isBlank()) {
                get()
            } else {
                get("%${query}%")
            }
        }
    }.flow

    override fun getAddresses(shopId: Long, config: PagingConfig, query: String) = Pager(
        config = config,
        remoteMediator = AddressesRemoteMediator(shopId, dependencies, query),
    ) {
        dependencies.database.addressDao.get(shopId)
    }.flow.map { data ->
        data.map { it.address }
    }

    override fun getShopName(shopId: Long) = dependencies.database.shopDao.getShopName(shopId)
}