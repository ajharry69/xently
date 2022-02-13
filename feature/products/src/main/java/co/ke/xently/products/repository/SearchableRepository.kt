package co.ke.xently.products.repository

import co.ke.xently.common.Retry
import co.ke.xently.data.TaskResult
import co.ke.xently.data.mapCatching
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.feature.utils.SEARCH_DELAY
import co.ke.xently.products.AttributeQuery
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
open class SearchableRepository(private val dependencies: Dependencies) : ISearchableRepository {
    override fun getShops(query: String) = Retry().run {
        dependencies.database.shopDao.getShops("%${query}%").flatMapConcat { shops ->
            if (shops.isEmpty()) {
                flow {
                    delay(SEARCH_DELAY)
                    emit(
                        sendRequest(401) { dependencies.service.shop.get(query, size = 30) }
                            .mapCatching { data ->
                                data.results.also {
                                    dependencies.database.shopDao.add(it)
                                }.take(5)
                            },
                    )
                }.cancellable()
            } else {
                flowOf(TaskResult.Success(shops.take(5)))
            }
        }.cancellable().retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }

    override fun getBrands(query: String) = Retry().run {
        dependencies.database.brandDao.get("%${query}%").flatMapConcat { brands ->
            if (brands.isEmpty()) {
                flow {
                    delay(SEARCH_DELAY)
                    emit(
                        sendRequest(401) { dependencies.service.brand.get(query, size = 30) }
                            .mapCatching { data ->
                                data.results.also {
                                    dependencies.database.brandDao.add(it)
                                }.take(5)
                            },
                    )
                }.cancellable()
            } else {
                flowOf(TaskResult.Success(brands.take(5)))
            }
        }.cancellable().retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }

    override fun getAttributes(query: AttributeQuery) = Retry().run {
        when (query.type) {
            AttributeQuery.Type.NAME -> {
                dependencies.database.attributeDao.getByName("%${query.nameQuery}%")
            }
            AttributeQuery.Type.VALUE -> {
                dependencies.database.attributeDao.getByValue("%${query.valueQuery}%")
            }
            AttributeQuery.Type.BOTH -> dependencies.database.attributeDao.get("%${query.nameQuery}%",
                "%${query.valueQuery}%")
            AttributeQuery.Type.NONE -> TODO()
        }.flatMapConcat { attributes ->
            if (attributes.isEmpty()) {
                flow {
                    delay(SEARCH_DELAY)
                    emit(
                        sendRequest(401) {
                            dependencies.service.attribute.get(arrayOf(query.nameQuery,
                                query.valueQuery).joinToString("->")
                                .replace(Regex("^(->)|(->)$"), ""), size = 30)
                        }.mapCatching { data ->
                            data.results.flatMap { attribute ->
                                attribute.values.map {
                                    attribute.copy(value = it)
                                }
                            }.also { dependencies.database.attributeDao.add(it) }.take(5)
                        },
                    )
                }.cancellable()
            } else {
                flowOf(TaskResult.Success(attributes.take(5)))
            }
        }.cancellable().retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }

    override fun getMeasurementUnits(query: String) = Retry().run {
        dependencies.database.measurementUnitDao.get("%${query}%").flatMapConcat { units ->
            if (units.isEmpty()) {
                flow {
                    delay(SEARCH_DELAY)
                    emit(
                        sendRequest(401) { dependencies.service.measurementUnit.get(query) }
                            .mapCatching { data ->
                                data.also {
                                    dependencies.database.measurementUnitDao.save(it)
                                }.take(5)
                            },
                    )
                }.cancellable()
            } else {
                flowOf(TaskResult.Success(units.take(5)))
            }
        }.cancellable().retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }
}