package co.ke.xently.products.repository

import co.ke.xently.data.Product
import co.ke.xently.source.local.Database


internal suspend fun List<Product>.saveLocallyWithAttributes(database: Database) {
    database.productDao.save(this)
    database.brandDao.add(flatMap { product -> product.brands.map { it.copy(productId = product.id) } })
    database.attributeDao.add(flatMap { product -> product.attributes.map { it.copy(productId = product.id) } })
}

internal suspend fun Product.saveLocallyWithAttributes(database: Database) {
    listOf(this).saveLocallyWithAttributes(database)
}