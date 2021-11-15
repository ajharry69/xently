package co.ke.xently.products.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Product
import co.ke.xently.products.repository.IProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ProductListViewModel @Inject constructor(
    private val repository: IProductsRepository,
) : ViewModel() {
    private val _productListResult = MutableStateFlow(Result.success<List<Product>?>(null))
    val productListResult: StateFlow<Result<List<Product>?>>
        get() = _productListResult

    init {
        viewModelScope.launch {
            repository.getProductList(true)
                .catch { emit(Result.failure(it)) }
                .collectLatest {
                    _productListResult.value = it
                }
        }
    }
}