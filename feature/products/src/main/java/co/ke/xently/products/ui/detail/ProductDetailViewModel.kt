package co.ke.xently.products.ui.detail

import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Product
import co.ke.xently.data.TaskResult
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.products.repository.IProductsRepository
import co.ke.xently.products.shared.SearchableViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ProductDetailViewModel @Inject constructor(
    private val repository: IProductsRepository,
) : SearchableViewModel(repository) {
    private val _productResult = MutableStateFlow<TaskResult<Product?>>(Success(null))
    val result: StateFlow<TaskResult<Product?>>
        get() = _productResult

    fun addOrUpdate(product: Product) {
        viewModelScope.launch {
            combineTransform(flowOf(product)) {
                if (it[0].isDefault) {
                    emitAll(repository.add(product))
                } else {
                    emitAll(repository.update(product))
                }
            }.flagLoadingOnStart()
                .collectLatest {
                    _productResult.value = it
                }
        }
    }

    fun get(id: Long) {
        viewModelScope.launch {
            combineTransform(flowOf(id)) {
                emitAll(repository.get(it[0]))
            }.flagLoadingOnStart()
                .collectLatest {
                    _productResult.value = it
                }
        }
    }
}