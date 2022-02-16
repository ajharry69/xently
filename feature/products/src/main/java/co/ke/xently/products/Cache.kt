package co.ke.xently.products

import co.ke.xently.data.Product
import co.ke.xently.feature.repository.Dependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


internal suspend fun List<Product>.saveLocallyWithAttributes(
    dependencies: Dependencies,
    scope: CoroutineScope = CoroutineScope(dependencies.dispatcher.io),
) {
    scope.launch(dependencies.dispatcher.io) {
        dependencies.database.productDao.save(this@saveLocallyWithAttributes)
    }
    scope.launch(dependencies.dispatcher.io) {
        dependencies.database.brandDao.add(
            withContext(dependencies.dispatcher.computation) {
                flatMap { product ->
                    product.brands.map {
                        it.copy(relatedId = product.id)
                    }
                }
            },
        )
    }
    scope.launch(dependencies.dispatcher.io) {
        dependencies.database.attributeDao.add(
            withContext(dependencies.dispatcher.computation) {
                flatMap { product ->
                    product.attributes.flatMap { attr ->
                        attr.values.mapTo(mutableListOf(attr)) {
                            attr.copy(value = it)
                        }
                    }.map {
                        it.copy(relatedId = product.id)
                    }
                }
            },
        )
    }
}

internal suspend fun Product.saveLocallyWithAttributes(
    dependencies: Dependencies,
    scope: CoroutineScope = CoroutineScope(dependencies.dispatcher.io),
) {
    listOf(this).saveLocallyWithAttributes(dependencies, scope)
}