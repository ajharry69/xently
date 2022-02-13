package co.ke.xently.products.repository

import co.ke.xently.data.*
import co.ke.xently.products.AttributeQuery
import kotlinx.coroutines.flow.Flow

interface ISearchableRepository {
    fun getShops(query: String): Flow<TaskResult<List<Shop>>>
    fun getBrands(query: String): Flow<TaskResult<List<Brand>>>
    fun getAttributes(query: AttributeQuery): Flow<TaskResult<List<Attribute>>>
    fun getMeasurementUnits(query: String): Flow<TaskResult<List<MeasurementUnit>>>
}