package co.ke.xently.products.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.ke.xently.data.*
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.utils.flagLoadingOnStartCatchingErrors
import co.ke.xently.products.repository.IProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ProductDetailViewModel @Inject constructor(
    private val repository: IProductsRepository,
) : ViewModel() {
    private val _productResult = MutableStateFlow<TaskResult<Product?>>(Success(null))
    val productResult: StateFlow<TaskResult<Product?>>
        get() = _productResult

    private val _measurementUnitsResult = MutableStateFlow<List<MeasurementUnit>>(emptyList())
    val measurementUnitsResult: StateFlow<List<MeasurementUnit>>
        get() = _measurementUnitsResult

    private val _shopsResult = MutableStateFlow<PagingData<Shop>>(PagingData.empty())
    val shopsResult: StateFlow<PagingData<Shop>>
        get() = _shopsResult

    fun add(product: Product) {
        viewModelScope.launch {
            repository.add(product)
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _productResult.value = it
                }
        }
    }

    fun get(id: Long) {
        viewModelScope.launch {
            repository.get(id)
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _productResult.value = it
                }
        }
    }

    fun getShops(config: PagingConfig, query: String) = viewModelScope.launch {
        combineTransform(flowOf(config), flowOf(query.trim())) { c, q ->
            if (q.isNotEmpty()) {
                emitAll(repository.getShops(c, q).flow)
            }
        }.collectLatest {
            _shopsResult.value = it
        }
    }

    fun getMeasurementUnits(query: String) = viewModelScope.launch {
        combineTransform(flowOf(query.trim())) {
            if (it[0].isNotEmpty()) {
                emitAll(repository.getMeasurementUnits(it[0]))
            }
        }.flagLoadingOnStartCatchingErrors()
            .collectLatest {
                _measurementUnitsResult.value = it.getOrNull() ?: emptyList()
            }
    }
}