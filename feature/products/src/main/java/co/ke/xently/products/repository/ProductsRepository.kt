package co.ke.xently.products.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.Product
import co.ke.xently.data.TaskResult
import co.ke.xently.data.getOrNull
import co.ke.xently.data.getOrThrow
import co.ke.xently.source.local.Database
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.ProductService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ProductsRepository @Inject constructor(
    private val service: ProductService,
    private val database: Database,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IProductsRepository {
    override fun add(product: Product) = Retry().run {
        flow {
            emit(sendRequest(401) { service.add(product) })
        }.onEach {
            database.productsDao.save(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun update(product: Product) = Retry().run {
        flow {
            emit(sendRequest(401) { service.update(product.id, product) })
        }.onEach {
            database.productsDao.save(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun get(id: Long) = Retry().run {
        database.productsDao.get(id).map { product ->
            if (product == null) {
                sendRequest(401) { service.get(id) }.apply {
                    getOrNull()?.also {
                        database.productsDao.save(it)
                    }
                }
            } else {
                TaskResult.Success(product)
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun get(config: PagingConfig) = Pager(
        config = config,
        remoteMediator = ProductsRemoteMediator(database, service),
    ) { database.productsDao.get() }
}