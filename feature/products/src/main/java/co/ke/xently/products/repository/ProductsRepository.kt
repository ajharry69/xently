package co.ke.xently.products.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import co.ke.xently.common.Retry
import co.ke.xently.data.Product
import co.ke.xently.data.TaskResult
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.repository.Dependencies
import co.ke.xently.products.ProductsRemoteMediator
import co.ke.xently.products.saveLocallyWithAttributes
import co.ke.xently.products.shared.repository.SearchableRepository
import co.ke.xently.products.ui.detail.ProductHttpException
import co.ke.xently.source.remote.retryCatch
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
                it.saveLocallyWithAttributes(dependencies)
            }
        }.retryCatch(retry).flowOn(dependencies.dispatcher.io)

    override fun add(product: Product) = Retry().run {
        flow {
            emit(sendRequest(errorClass = ProductHttpException::class.java) {
                dependencies.service.product.add(product)
            })
        }.doTaskWhileSavingEachLocally(this)
    }

    override fun update(product: Product) = Retry().run {
        flow {
            emit(sendRequest(errorClass = ProductHttpException::class.java) {
                dependencies.service.product.update(product.id, product)
            })
        }.doTaskWhileSavingEachLocally(this)
    }

    override fun get(id: Long) = Retry().run {
        dependencies.database.productDao.get(id).map { productWithRelated ->
            if (productWithRelated == null) {
                sendRequest {
                    dependencies.service.product.get(id)
                }.also { result ->
                    result.getOrNull()?.also {
                        it.saveLocallyWithAttributes(dependencies)
                    }
                }
            } else {
                TaskResult.Success(productWithRelated.product)
            }
        }.retryCatch(this).flowOn(dependencies.dispatcher.io)
    }

    override fun get(shopId: Long?, config: PagingConfig) = Pager(
        config = config,
        remoteMediator = ProductsRemoteMediator(dependencies, shopId),
    ) {
        dependencies.database.productDao.run {
            if (shopId == null) {
                get()
            } else {
                getForShop(shopId)
            }
        }
    }.flow.map { data ->
        data.map { it.product }
    }

    override fun getShopName(shopId: Long) = dependencies.database.shopDao.getShopName(shopId)
}