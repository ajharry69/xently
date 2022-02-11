package co.ke.xently.products.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.*
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.utils.flagLoadingOnStartCatchingErrors
import co.ke.xently.feature.utils.setCleansedQuery
import co.ke.xently.products.repository.AttributeQuery
import co.ke.xently.products.repository.IProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class ProductDetailViewModel @Inject constructor(
    private val repository: IProductsRepository,
) : ViewModel() {
    private val _productResult = MutableStateFlow<TaskResult<Product?>>(Success(null))
    val productResult: StateFlow<TaskResult<Product?>>
        get() = _productResult

    val shopsResult: StateFlow<List<Shop>>
    val brandsResult: StateFlow<List<Brand>>
    val attributesResult: StateFlow<List<Attribute>>
    val measurementUnitsResult: StateFlow<List<MeasurementUnit>>

    private val shopQuery = MutableStateFlow("")
    private val brandQuery = MutableStateFlow("")
    private val measurementUnitQuery = MutableStateFlow("")
    private val attributeQuery = MutableStateFlow(AttributeQuery())

    private fun <T> Flow<TaskResult<List<T>>>.searchStateFlow() = flagLoadingOnStartCatchingErrors()
        .mapLatest {
            it.getOrNull() ?: emptyList()
        }.stateIn(viewModelScope, WhileSubscribed(replayExpirationMillis = 0), emptyList())

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

    fun addOrUpdate(product: Product) {
        viewModelScope.launch {
            combineTransform(flowOf(product)) {
                if (it[0].isDefault) {
                    emitAll(repository.add(product))
                } else {
                    emitAll(repository.update(product))
                }
            }.flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _productResult.value = it
                }
        }
    }

    fun get(id: Long) {
        viewModelScope.launch {
            combineTransform(flowOf(id)) {
                emitAll(repository.get(it[0]))
            }.flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _productResult.value = it
                }
        }
    }
}