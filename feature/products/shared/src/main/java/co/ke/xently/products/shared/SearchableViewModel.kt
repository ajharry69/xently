package co.ke.xently.products.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.*
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.products.shared.repository.ISearchableRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
open class SearchableViewModel(private val repository: ISearchableRepository) : ViewModel() {
    val shopsResult: StateFlow<List<Shop>>
    val brandsResult: StateFlow<List<Product.Brand>>
    val attributesResult: StateFlow<List<Product.Attribute>>
    val measurementUnitsResult: StateFlow<List<MeasurementUnit>>

    private val shopQuery = MutableStateFlow("")
    private val brandQuery = MutableStateFlow("")
    private val measurementUnitQuery = MutableStateFlow("")
    private val attributeQuery = MutableStateFlow(AttributeQuery())

    private fun <T> Flow<TaskResult<List<T>>>.searchStateFlow() = flagLoadingOnStart()
        .mapLatest {
            it.getOrNull() ?: emptyList()
        }.stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(replayExpirationMillis = 0), emptyList())

    init {
        shopsResult = shopQuery.flatMapLatest {
            if (it.isBlank()) {
                emptyFlow()
            } else {
                repository.getShops(it)
            }
        }.searchStateFlow()

        brandsResult = brandQuery.flatMapLatest {
            if (it.isBlank()) {
                emptyFlow()
            } else {
                repository.getBrands(it)
            }
        }.searchStateFlow()

        attributesResult = attributeQuery.flatMapLatest {
            if (it.isDefault) {
                emptyFlow()
            } else {
                repository.getAttributes(it)
            }
        }.searchStateFlow()

        measurementUnitsResult = measurementUnitQuery.flatMapLatest {
            if (it.isBlank()) {
                emptyFlow()
            } else {
                repository.getMeasurementUnits(it)
            }
        }.searchStateFlow()
    }

    fun setShopQuery(query: String) = shopQuery.setCleansedQuery(query)

    fun setBrandQuery(query: String) = brandQuery.setCleansedQuery(query)

    fun setMeasurementUnitQuery(query: String) = measurementUnitQuery.setCleansedQuery(query)

    fun setAttributeQuery(query: AttributeQuery) {
        attributeQuery.value = query
    }

    private fun MutableStateFlow<String>.setCleansedQuery(query: String) {
        query.trim().also {
            if (it.isNotBlank()) {
                this.value = query
            }
        }
    }
}