package co.ke.xently.products.shared.repository

import co.ke.xently.data.*
import co.ke.xently.products.shared.AttributeQuery
import kotlinx.coroutines.flow.Flow

interface ISearchableRepository {
    fun getShops(query: String): Flow<TaskResult<List<Shop>>>
    fun getBrands(query: String): Flow<TaskResult<List<Product.Brand>>>
    fun getAttributes(query: AttributeQuery): Flow<TaskResult<List<Product.Attribute>>>
    fun getMeasurementUnits(query: String): Flow<TaskResult<List<MeasurementUnit>>>
}