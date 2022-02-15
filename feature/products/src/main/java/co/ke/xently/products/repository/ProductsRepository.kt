package co.ke.xently.products.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import co.ke.xently.common.Retry
import co.ke.xently.data.Product
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.products.ProductsRemoteMediator
import co.ke.xently.products.saveLocallyWithAttributes
import co.ke.xently.products.shared.repository.SearchableRepository
import co.ke.xently.products.ui.detail.ProductHttpException
import co.ke.xently.source.remote.retryCatchIfNecessary
import co.ke.xently.source.remote.sendRequest
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ProductsRepository @Inject constructor(private val dependencies: Dependencies) :
    SearchableRepository(dependencies), IProductsRepository {
    private fun Flow<TaskResult<Product>>.doTaskWhileSavingEachLocally(retry: Retry) =
        onEach { result ->
            result.getOrNull()?.also {
                it.saveLocallyWithAttributes(dependencies.database)
            }
        }.retryCatchIfNecessary(retry).flowOn(dependencies.dispatcher.io)

    override fun add(product: Product) = Retry().run {
        flow {
            emit(sendRequest(401, errorClass = ProductHttpException::class.java) {
                dependencies.service.product.add(product)
            })
        }.doTaskWhileSavingEachLocally(this)
    }

    override fun update(product: Product) = Retry().run {
        flow {
            emit(sendRequest(401, errorClass = ProductHttpException::class.java) {
                dependencies.service.product.update(product.id, product)
            })
        }.doTaskWhileSavingEachLocally(this)
    }

    override fun get(id: Long) = Retry().run {
        dependencies.database.productDao.get(id).map { productWithRelated ->
            if (productWithRelated == null) {
                sendRequest(401) {
                    dependencies.service.product.get(id)
                }.also { result ->
                    result.getOrNull()?.also {
                        it.saveLocallyWithAttributes(dependencies.database)
                    }
                }
            } else {
                TaskResult.Success(productWithRelated.product.copy(
                    shop = productWithRelated.shop ?: Shop.default(),
                    brands = productWithRelated.brands,
                    attributes = productWithRelated.attributes,
                ))
            }
        }.retryCatchIfNecessary(this).flowOn(dependencies.dispatcher.io)
    }

    override fun get(config: PagingConfig) = Pager(
        config = config,
        remoteMediator = ProductsRemoteMediator(dependencies),
        pagingSourceFactory = dependencies.database.productDao::get,
    ).flow.map { data ->
        data.map {
            it.product.copy(shop = it.shop ?: Shop.default())
        }
    }
}