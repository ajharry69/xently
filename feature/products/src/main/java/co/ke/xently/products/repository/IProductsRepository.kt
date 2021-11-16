package co.ke.xently.products.repository

import co.ke.xently.data.Product
import co.ke.xently.data.TaskResult
import kotlinx.coroutines.flow.Flow

interface IProductsRepository {
    fun addProduct(product: Product): Flow<TaskResult<Product>>
    fun updateProduct(product: Product): Flow<TaskResult<Product>>
    fun getProduct(id: Long): Flow<TaskResult<Product>>
    fun getProductList(remote: Boolean): Flow<TaskResult<List<Product>>>
}