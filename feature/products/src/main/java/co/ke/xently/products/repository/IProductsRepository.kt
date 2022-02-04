package co.ke.xently.products.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.ke.xently.data.MeasurementUnit
import co.ke.xently.data.Product
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import kotlinx.coroutines.flow.Flow

interface IProductsRepository {
    fun add(product: Product): Flow<TaskResult<Product>>
    fun update(product: Product): Flow<TaskResult<Product>>
    fun get(id: Long): Flow<TaskResult<Product>>
    fun get(config: PagingConfig): Pager<Int, Product>
    fun getMeasurementUnits(query: String): Flow<TaskResult<List<MeasurementUnit>>>
    fun getShops(query: String): Flow<TaskResult<List<Shop>>>
}