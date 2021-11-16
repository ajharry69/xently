package co.ke.xently.products.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Product
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.utils.flagLoadingOnStartCatchingErrors
import co.ke.xently.products.repository.IProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ProductListViewModel @Inject constructor(
    private val repository: IProductsRepository,
) : ViewModel() {
    private val _productListResult = MutableStateFlow<TaskResult<List<Product>>>(TaskResult.Loading)
    val productListResult: StateFlow<TaskResult<List<Product>>>
        get() = _productListResult

    init {
        viewModelScope.launch {
            repository.getProductList(true)
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _productListResult.value = it
                }
        }
    }
}