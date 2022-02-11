package co.ke.xently.products.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import co.ke.xently.common.Retry
import co.ke.xently.common.di.qualifiers.coroutines.IODispatcher
import co.ke.xently.data.*
import co.ke.xently.products.ui.detail.ProductHttpException
import co.ke.xently.source.local.Database
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import co.ke.xently.source.remote.services.AttributeService
import co.ke.xently.source.remote.services.BrandService
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
    private val database: Database,
    private val service: ProductService,
    private val shopService: ShopService,
    private val brandService: BrandService,
    private val attributeService: AttributeService,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IProductsRepository {
    private fun Flow<TaskResult<Product>>.saveLocallyIfApplicable(retry: Retry) = onEach { result ->
        if (result is TaskResult.Success) {
            result.getOrThrow().also { product ->
                database.productDao.save(product)
                database.brandDao.add(product.brands.map { it.copy(productId = product.id) })
                database.attributeDao.add(product.attributes.map { it.copy(productId = product.id) })
            }
        }
    }.retryCatchIfNecessary(retry).flowOn(ioDispatcher)

    override fun add(product: Product) = Retry().run {
        flow {
            emit(sendRequest(401, errorClass = ProductHttpException::class.java) {
                service.add(product)
            })
        }.saveLocallyIfApplicable(this)
    }

    override fun update(product: Product) = Retry().run {
        flow {
            emit(sendRequest(401, errorClass = ProductHttpException::class.java) {
                service.update(product.id, product)
            })
        }.saveLocallyIfApplicable(this)
    }

    override fun get(id: Long) = Retry().run {
        database.productDao.get(id).map { productWithRelated ->
            if (productWithRelated == null) {
                sendRequest(401) {
                    service.get(id)
                }.also { result ->
                    result.getOrNull()?.also {
                        database.productDao.save(it)
                    }
                }
            } else {
                TaskResult.Success(productWithRelated.product.copy(
                    shop = productWithRelated.shop ?: Shop.default(),
                    brands = productWithRelated.brands,
                    attributes = productWithRelated.attributes,
                ))
            }
        }.retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    override fun get(config: PagingConfig) = Pager(
        config = config,
        remoteMediator = ProductsRemoteMediator(database, service),
        pagingSourceFactory = database.productDao::get,
    ).flow.map { data ->
        data.map {
            it.product.copy(shop = it.shop ?: Shop.default())
        }
    }

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
    override fun getShops(query: String) = Retry().run {
        database.shopDao.getShops("%${query}%").flatMapConcat { shops ->
            if (shops.isEmpty()) {
                flow {
                    delay(100.milliseconds)
                    emit(
                        sendRequest(401) { shopService.get(query, size = 30) }
                            .mapCatching { data ->
                                data.results.also {
                                    database.shopDao.add(it)
                                }.take(5)
                            },
                    )
                }.cancellable()
            } else {
                flowOf(TaskResult.Success(shops.take(5)))
            }
        }.cancellable().retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    @OptIn(FlowPreview::class, ExperimentalTime::class)
    override fun getBrands(query: String) = Retry().run {
        database.brandDao.get("%${query}%").flatMapConcat { brands ->
            if (brands.isEmpty()) {
                flow {
                    delay(100.milliseconds)
                    emit(
                        sendRequest(401) { brandService.get(query, size = 30) }
                            .mapCatching { data ->
                                data.results.also {
                                    database.brandDao.add(it)
                                }.take(5)
                            },
                    )
                }.cancellable()
            } else {
                flowOf(TaskResult.Success(brands.take(5)))
            }
        }.cancellable().retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }

    @OptIn(FlowPreview::class, ExperimentalTime::class)
    override fun getAttributes(query: AttributeQuery) = Retry().run {
        when (query.type) {
            AttributeQuery.Type.NAME -> {
                database.attributeDao.getByName("%${query.nameQuery}%")
            }
            AttributeQuery.Type.VALUE -> {
                database.attributeDao.getByValue("%${query.valueQuery}%")
            }
            AttributeQuery.Type.BOTH -> database.attributeDao.get("%${query.nameQuery}%",
                "%${query.valueQuery}%")
            AttributeQuery.Type.NONE -> TODO()
        }.flatMapConcat { attributes ->
            if (attributes.isEmpty()) {
                flow {
                    delay(100.milliseconds)
                    emit(
                        sendRequest(401) {
                            attributeService.get(arrayOf(query.nameQuery,
                                query.valueQuery).joinToString("->"), size = 30)
                        }.mapCatching { data ->
                            data.results.flatMap { attribute ->
                                attribute.values.map {
                                    attribute.copy(value = it)
                                }
                            }.also { database.attributeDao.add(it) }.take(5)
                        },
                    )
                }.cancellable()
            } else {
                flowOf(TaskResult.Success(attributes.take(5)))
            }
        }.cancellable().retryCatchIfNecessary(this).flowOn(ioDispatcher)
    }
}