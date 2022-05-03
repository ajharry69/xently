package co.ke.xently.source.local.daos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import co.ke.xently.data.Product
import co.ke.xently.source.local.Database
import co.ke.xently.source.local.RoomDatabaseRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ProductDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val databaseRule = RoomDatabaseRule(Database::class.java)

    @Test
    fun testGetProductsByProductName() = runTest {
        with(databaseRule.database) {
            val productToMatch = Product.default().copy(name = "bread", id = 1)
            productDao.save(
                productToMatch,
                Product.default().copy(name = "milk", id = 2),
            )

            productDao.getProducts("bread").test {
                assertThat(
                    awaitItem().map { it.product.name },
                    Matchers.equalTo(listOf(productToMatch.name))
                )
            }
        }
    }

    @Test
    fun testGetProductsByBrandName() = runTest {
        with(databaseRule.database) {
            val productToMatch = Product.default().copy(name = "milk", id = 1)
            productDao.save(
                productToMatch,
                Product.default().copy(name = "bread", id = 2),
            )

            brandDao.add(
                listOf(
                    Product.Brand.default()
                        .copy(name = "brookside", relatedId = productToMatch.id)
                )
            )

            productDao.getProducts("brookside").test {
                val products = awaitItem()
                assertThat(
                    products.map { it.product.name },
                    Matchers.equalTo(listOf(productToMatch.name)),
                )
                assertThat(
                    products[0].product.brands.map { it.name },
                    Matchers.equalTo(listOf("brookside")),
                )
            }
        }
    }

    @Test
    fun testGetProductsByMatchingProductBrandAndAttributes() = runTest {
        with(databaseRule.database) {
            val milk = Product.default().copy(name = "milk", id = 1)
            val bread = Product.default().copy(name = "bread", id = 2)
            productDao.save(milk, bread, Product.default().copy(name = "salt", id = 3))

            brandDao.add(
                listOf(
                    Product.Brand.default()
                        .copy(name = "brookside", relatedId = milk.id)
                )
            )

            attributeDao.add(
                listOf(
                    Product.Attribute.default()
                        .copy(name = "kind", value = "whole", relatedId = milk.id)
                )
            )
            attributeDao.add(
                listOf(
                    Product.Attribute.default()
                        .copy(name = "kind", value = "brown", relatedId = bread.id)
                )
            )

            productDao.getProducts("%br%").test {
                val products = awaitItem()
                // There shouldn't be duplicates
                assertThat(
                    products.map { it.product.name },
                    Matchers.equalTo(listOf(bread.name, milk.name)),
                )
            }
        }
    }

    @Test
    fun testGetProductsByAttributeName() = runTest {
        with(databaseRule.database) {
            val milk = Product.default().copy(name = "milk", id = 1)
            val bread = Product.default().copy(name = "bread", id = 2)
            productDao.save(milk, bread)

            attributeDao.add(
                listOf(
                    Product.Attribute.default()
                        .copy(name = "kind", value = "whole", relatedId = milk.id)
                )
            )
            attributeDao.add(
                listOf(
                    Product.Attribute.default()
                        .copy(name = "kind", value = "brown", relatedId = bread.id)
                )
            )

            productDao.getProducts("kind").test {
                val products = awaitItem()
                assertThat(
                    products.map { it.product.name },
                    Matchers.equalTo(listOf("bread", "milk")),
                )
                assertThat(
                    products[0].product.attributes.map { it.value },
                    Matchers.equalTo(listOf("brown")),
                )
                assertThat(
                    products[1].product.attributes.map { it.value },
                    Matchers.equalTo(listOf("whole")),
                )
            }
        }
    }

    @Test
    fun testGetProductsByAttributeValue() = runTest {
        with(databaseRule.database) {
            val milk = Product.default().copy(name = "milk", id = 1)
            val bread = Product.default().copy(name = "bread", id = 2)
            productDao.save(milk, bread)

            attributeDao.add(
                listOf(
                    Product.Attribute.default()
                        .copy(name = "kind", value = "whole", relatedId = milk.id)
                )
            )
            attributeDao.add(
                listOf(
                    Product.Attribute.default()
                        .copy(name = "kind", value = "brown", relatedId = bread.id)
                )
            )

            productDao.getProducts("brown").test {
                val products = awaitItem()
                assertThat(
                    products.map { it.product.name },
                    Matchers.equalTo(listOf("bread")),
                )
                assertThat(
                    products[0].product.attributes.map { it.value },
                    Matchers.equalTo(listOf("brown")),
                )
            }
        }
    }
}