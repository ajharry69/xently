package co.ke.xently.products.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.*
import co.ke.xently.products.ui.detail.ProductHttpException
import co.ke.xently.source.local.Database
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.ProductService
import co.ke.xently.source.remote.services.ShopService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@Singleton
internal class ProductsRepository @Inject constructor(
    private val service: ProductService,
    private val shopService: ShopService,
    private val database: Database,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IProductsRepository {
    override fun add(product: Product) = Retry().run {
        flow {
            emit(sendRequest(401, errorClass = ProductHttpException::class.java) {
                service.add(product)
            })
        }.onEach {
            if (it is TaskResult.Success) {
                database.productsDao.save(it.getOrThrow())
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun update(product: Product) = Retry().run {
        flow {
            emit(sendRequest(401, errorClass = ProductHttpException::class.java) {
                service.update(product.id, product)
            })
        }.onEach {
            if (it is TaskResult.Success) {
                database.productsDao.save(it.getOrThrow())
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun get(id: Long) = Retry().run {
        database.productsDao.get(id).map { product ->
            if (product == null) {
                sendRequest(401) {
                    service.get(id)
                }.also { result ->
                    result.getOrNull()?.also {
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

    @OptIn(FlowPreview::class, ExperimentalTime::class)
    override fun getMeasurementUnits(query: String) = Retry().run {
        database.measurementUnitDao.get("%${query}%").flatMapConcat { units ->
            if (units.isEmpty()) {
                flow {
                    delay(100.milliseconds)
                    emit(
                        sendRequest(401) { service.getMeasurementUnits(query) }
                            .mapCatching { data ->
                                data.also {
                                    database.measurementUnitDao.save(it)
                                }.take(5)
                            },
                    )
                }.cancellable()
            } else {
                flowOf(TaskResult.Success(units.take(5)))
            }
        }.cancellable().retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    @OptIn(FlowPreview::class, ExperimentalTime::class)
    override fun getShops(query: String): Flow<TaskResult<List<Shop>>> = Retry().run {
        database.shopsDao.getShops("%${query}%").flatMapConcat { shops ->
            if (shops.isEmpty()) {
                flow {
                    delay(100.milliseconds)
                    emit(
                        sendRequest(401) { shopService.get(query, size = 30) }
                            .mapCatching { data ->
                                data.results.also {
                                    database.shopsDao.add(it)
                                }.take(5)
                            },
                    )
                }.cancellable()
            } else {
                flowOf(TaskResult.Success(shops.take(5)))
            }
        }.cancellable().retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }
}