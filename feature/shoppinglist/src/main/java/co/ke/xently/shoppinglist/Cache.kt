package co.ke.xently.shoppinglist

import co.ke.xently.data.Product
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.feature.repository.Dependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


internal suspend fun List<ShoppingListItem>.saveLocallyWithAttributes(
    dependencies: Dependencies,
    scope: CoroutineScope = CoroutineScope(dependencies.dispatcher.io),
) {
    scope.launch(dependencies.dispatcher.io) {
        dependencies.database.shoppingListDao.save(this@saveLocallyWithAttributes)
    }
    val brands = withContext(dependencies.dispatcher.computation) {
        flatMap { item ->
            item.brands.map {
                it.copy(relatedId = item.id)
            }
        }
    }
    scope.launch(dependencies.dispatcher.io) {
        dependencies.database.brandDao.save(brands)
    }
    scope.launch(dependencies.dispatcher.io) {
        dependencies.database.brandDao.add(
            withContext(dependencies.dispatcher.computation) {
                brands.map {
                    Product.Brand(name = it.name)
                }
            },
        )
    }
    val attributes = withContext(dependencies.dispatcher.computation) {
        flatMap { item ->
            item.attributes.flatMap { attr ->
                attr.values.mapTo(mutableListOf(attr)) {
                    attr.copy(value = it)
                }
            }.map {
                it.copy(relatedId = item.id)
            }
        }
    }
    scope.launch(dependencies.dispatcher.io) {
        dependencies.database.attributeDao.save(attributes)
    }
    scope.launch(dependencies.dispatcher.io) {
        dependencies.database.attributeDao.add(
            withContext(dependencies.dispatcher.computation) {
                attributes.map {
                    Product.Attribute(name = it.name, value = it.value, values = it.values)
                }
            },
        )
    }
}

internal suspend fun ShoppingListItem.saveLocallyWithAttributes(
    dependencies: Dependencies,
    scope: CoroutineScope = CoroutineScope(dependencies.dispatcher.io),
) {
    listOf(this).saveLocallyWithAttributes(dependencies, scope)
}