package co.ke.xently.products.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.ke.xently.data.Product
import co.ke.xently.data.TaskResult
import kotlinx.coroutines.flow.Flow

interface IProductsRepository {
    fun addProduct(product: Product): Flow<TaskResult<Product>>
    fun updateProduct(product: Product): Flow<TaskResult<Product>>
    fun getProduct(id: Long): Flow<TaskResult<Product>>
    fun getProductListPager(config: PagingConfig): Pager<Int, Product>
}