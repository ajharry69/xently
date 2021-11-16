package co.ke.xently.products.repository

import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.*
import co.ke.xently.source.local.daos.ProductsDao
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.ProductService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ProductsRepository @Inject constructor(
    private val service: ProductService,
    private val dao: ProductsDao,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IProductsRepository {
    override fun addProduct(product: Product) = Retry().run {
        flow {
            emit(sendRequest(401) { service.addProduct(product) })
        }.onEach {
            dao.addProducts(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun updateProduct(product: Product) = Retry().run {
        flow {
            emit(sendRequest(401) { service.updateProduct(product.id, product) })
        }.onEach {
            dao.addProducts(it.getOrThrow())
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun getProduct(id: Long) = Retry().run {
        dao.getProduct(id).map { product ->
            if (product == null) {
                sendRequest(401) { service.getProduct(id) }.apply {
                    getOrNull()?.also {
                        dao.addProducts(it)
                    }
                }
            } else {
                TaskResult.Success(product)
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun getProductList(remote: Boolean): Flow<TaskResult<List<Product>>> = Retry().run {
        if (remote) {
            flow { emit(sendRequest(401) { service.getProductList() }) }
                .map { result ->
                    result.mapCatching {
                        it.results.apply {
                            dao.addProducts(*toTypedArray())
                        }
                    }
                }
                .retryCatchIfNecessary(this)
                .flowOn(ioDispatcher)
                .onCompletion {
                    // Return cached records. Caveats:
                    //  1. No error propagation
                    if (it == null) emitAll(getProductList(false))
                }
        } else {
            dao.getProductList().map { products ->
                TaskResult.Success(products)
            }
        }
    }
}