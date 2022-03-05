package co.ke.xently.products.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.TaskResult
import co.ke.xently.data.getOrNull
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.products.shared.repository.ISearchableRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
open class SearchableViewModel(private val repository: ISearchableRepository) : ViewModel() {
    private fun <T> Flow<TaskResult<List<T>>>.searchStateFlow() = mapLatest {
        it.getOrNull() ?: emptyList()
    }.shareIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
    )

    private val shopQuery = MutableSharedFlow<String>()
    val shopsResult = shopQuery.flatMapLatest {
        if (it.isBlank()) {
            emptyFlow()
        } else {
            repository.getShops(it).flagLoadingOnStart()
        }
    }.searchStateFlow()

    private val measurementUnitQuery = MutableSharedFlow<String>()
    val measurementUnitsResult = measurementUnitQuery.flatMapLatest {
        if (it.isBlank()) {
            emptyFlow()
        } else {
            repository.getMeasurementUnits(it).flagLoadingOnStart()
        }
    }.searchStateFlow()

    private val brandQuery = MutableSharedFlow<String>()
    val brandsResult = brandQuery.flatMapLatest {
        if (it.isBlank()) {
            emptyFlow()
        } else {
            repository.getBrands(it).flagLoadingOnStart()
        }
    }.searchStateFlow()

    private val attributeQuery = MutableSharedFlow<AttributeQuery>()
    val attributesResult = attributeQuery.flatMapLatest {
        if (it.isDefault) {
            emptyFlow()
        } else {
            repository.getAttributes(it).flagLoadingOnStart()
        }
    }.searchStateFlow()

    private fun MutableSharedFlow<String>.setCleansedQuery(query: String) {
        query.trim().also {
            if (it.isNotBlank()) {
                viewModelScope.launch {
                    this@setCleansedQuery.emit(it)
                }
            }
        }
    }

    fun setShopQuery(query: String) = shopQuery.setCleansedQuery(query)

    fun setBrandQuery(query: String) = brandQuery.setCleansedQuery(query)

    fun setMeasurementUnitQuery(query: String) = measurementUnitQuery.setCleansedQuery(query)

    fun setAttributeQuery(query: AttributeQuery) {
        viewModelScope.launch {
            attributeQuery.emit(query)
        }
    }
}