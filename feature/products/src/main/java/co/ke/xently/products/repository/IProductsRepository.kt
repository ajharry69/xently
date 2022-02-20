package co.ke.xently.products.repository

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.ke.xently.data.Product
import co.ke.xently.data.TaskResult
import co.ke.xently.products.shared.repository.ISearchableRepository
import kotlinx.coroutines.flow.Flow

interface IProductsRepository : ISearchableRepository {
    fun add(product: Product): Flow<TaskResult<Product>>
    fun update(product: Product): Flow<TaskResult<Product>>
    fun get(id: Long): Flow<TaskResult<Product>>
    fun get(shopId: Long?, config: PagingConfig): Flow<PagingData<Product>>
    fun getShopName(shopId: Long): Flow<String?>
}