package co.ke.xently.products.repository

import co.ke.xently.data.Product
import kotlinx.coroutines.flow.Flow

interface IProductsRepository {
    fun addProduct(product: Product): Flow<Result<Product>>
    fun updateProduct(product: Product): Flow<Result<Product>>
    fun getProduct(id: Long): Flow<Result<Product>>
    fun getProductList(remote: Boolean): Flow<Result<List<Product>>>
}