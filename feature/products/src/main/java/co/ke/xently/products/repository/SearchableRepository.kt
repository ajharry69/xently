package co.ke.xently.products.repository

import co.ke.xently.common.Retry
import co.ke.xently.data.TaskResult
import co.ke.xently.data.mapCatching
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.feature.utils.SEARCH_DELAY
import co.ke.xently.products.AttributeQuery
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
open class SearchableRepository(private val dependencies: Dependencies) : ISearchableRepository {
    override fun getShops(query: String) = Retry().run {
        dependencies.database.shopDao.getShops("%${query}%").mapLatest { shops ->
            if (shops.isEmpty()) {
                delay(SEARCH_DELAY)
                sendRequest(401) { dependencies.service.shop.get(query, size = 30) }
                    .mapCatching { data ->
                        data.results.also {
                            dependencies.database.shopDao.add(it)
                        }
                    }
            }
            TaskResult.Success(shops.take(5))
        }.cancellable().retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }

    override fun getBrands(query: String) = Retry().run {
        dependencies.database.brandDao.get("%${query}%").mapLatest { brands ->
            if (brands.isEmpty()) {
                delay(SEARCH_DELAY)
                sendRequest(401) { dependencies.service.brand.get(query, size = 30) }
                    .mapCatching { data ->
                        data.results.also {
                            dependencies.database.brandDao.add(it)
                        }
                    }
            }
            TaskResult.Success(brands.take(5))
        }.cancellable().retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
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
                sendRequest(401) {
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
        }.cancellable().retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }

    override fun getMeasurementUnits(query: String) = Retry().run {
        dependencies.database.measurementUnitDao.get("%${query}%").mapLatest { units ->
            if (units.isEmpty()) {
                delay(SEARCH_DELAY)
                sendRequest(401) { dependencies.service.measurementUnit.get(query) }
                    .mapCatching { data ->
                        data.also {
                            dependencies.database.measurementUnitDao.save(it)
                        }.take(5)
                    }
            }
            TaskResult.Success(units.take(5))
        }.cancellable().retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }
}