package co.ke.xently.products.shared.repository

import co.ke.xently.common.Retry
import co.ke.xently.data.TaskResult
import co.ke.xently.data.mapCatching
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.feature.utils.SEARCH_DELAY
import co.ke.xently.products.shared.AttributeQuery
import co.ke.xently.source.remote.retryCatch
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
open class SearchableRepository(private val dependencies: Dependencies) : ISearchableRepository {
    override fun getShops(query: String) = flow {
        val shopsResult = sendRequest { dependencies.service.shop.get(query, size = 30) }
            .mapCatching { data ->
                data.results.also {
                    dependencies.database.shopDao.add(it)
                }
            }
        emit(shopsResult)
    }.flowOn(dependencies.dispatcher.io)

    override fun getProducts(query: String) = flow {
        val productsResult = sendRequest { dependencies.service.product.get(query, size = 5) }
            .mapCatching { data ->
                data.results.also {
                    dependencies.database.productDao.save(it)
                }.take(5)
            }
        emit(productsResult)
    }.flowOn(dependencies.dispatcher.io)

    override fun getBrands(query: String) = Retry().run {
        dependencies.database.brandDao.get("%${query}%").mapLatest { brands ->
            if (brands.isEmpty()) {
                delay(SEARCH_DELAY)
                sendRequest { dependencies.service.brand.get(query, size = 30) }
                    .mapCatching { data ->
                        data.results.also {
                            dependencies.database.brandDao.add(it)
                        }
                    }
            }
            TaskResult.Success(brands.take(5))
        }.cancellable().retryCatch(this).flowOn(dependencies.dispatcher.io)
    }

    override fun getAttributes(query: AttributeQuery) = Retry().run {
        var (q, filters) = Pair("", mapOf<String, String>())
        when (query.type) {
            AttributeQuery.Type.NAME -> {
                q = query.nameQuery
                dependencies.database.attributeDao.getByName("%${query.nameQuery}%")
            }
            AttributeQuery.Type.VALUE -> {
                q = query.valueQuery
                dependencies.database.attributeDao.getByValue("%${query.valueQuery}%")
            }
            AttributeQuery.Type.BOTH -> {
                q = query.valueQuery
                filters = mapOf("name" to query.nameQuery)
                dependencies.database.attributeDao.get(query.nameQuery, "%${query.valueQuery}%")
            }
            AttributeQuery.Type.NONE -> TODO()
        }.cancellable().mapLatest { attributes ->
            if (attributes.isEmpty()) {
                delay(SEARCH_DELAY)
                sendRequest {
                    dependencies.service.attribute.get(query = q, filters = filters, size = 30)
                }.mapCatching { data ->
                    data.results.flatMap { attribute ->
                        attribute.values.map {
                            attribute.copy(value = it)
                        }
                    }.also { dependencies.database.attributeDao.add(it) }
                }
            }
            TaskResult.Success(attributes.take(5))
        }.cancellable().retryCatch(this).flowOn(dependencies.dispatcher.io)
    }

    override fun getMeasurementUnits(query: String) = Retry().run {
        dependencies.database.measurementUnitDao.get("%${query}%").mapLatest { units ->
            if (units.isEmpty()) {
                delay(SEARCH_DELAY)
                sendRequest { dependencies.service.measurementUnit.get(query) }
                    .mapCatching { data ->
                        data.also {
                            dependencies.database.measurementUnitDao.save(it)
                        }.take(5)
                    }
            }
            TaskResult.Success(units.take(5))
        }.cancellable().retryCatch(this).flowOn(dependencies.dispatcher.io)
    }
}