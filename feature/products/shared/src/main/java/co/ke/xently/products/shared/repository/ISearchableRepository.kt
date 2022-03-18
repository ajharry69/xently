package co.ke.xently.products.shared.repository

import co.ke.xently.data.MeasurementUnit
import co.ke.xently.data.Product
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.products.shared.AttributeQuery
import kotlinx.coroutines.flow.Flow

interface ISearchableRepository {
    fun getShops(query: String): Flow<TaskResult<List<Shop>>>
    fun getProducts(query: String): Flow<TaskResult<List<Product>>>
    fun getBrands(query: String): Flow<TaskResult<List<Product.Brand>>>
    fun getAttributes(query: AttributeQuery): Flow<TaskResult<List<Product.Attribute>>>
    fun getMeasurementUnits(query: String): Flow<TaskResult<List<MeasurementUnit>>>
}