package co.ke.xently.products.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Product
import co.ke.xently.data.TaskResult
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.utils.flagLoadingOnStartCatchingErrors
import co.ke.xently.products.repository.IProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ProductDetailViewModel @Inject constructor(
    private val repository: IProductsRepository,
) : ViewModel() {
    private val _productResult = MutableStateFlow<TaskResult<Product?>>(Success(null))
    val productResult: StateFlow<TaskResult<Product?>>
        get() = _productResult

    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.add(product)
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _productResult.value = it
                }
        }
    }

    fun getProduct(id: Long) {
        viewModelScope.launch {
            repository.get(id)
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _productResult.value = it
                }
        }
    }
}