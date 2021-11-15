package co.ke.xently.products.ui.detail

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
internal class ProductDetailViewModel @Inject constructor(
    private val repository: IProductsRepository,
) : ViewModel() {
    private val _productResult = MutableStateFlow<Result<Product?>>(Result.success(null))
    val productResult: StateFlow<Result<Product?>>
        get() = _productResult

    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.addProduct(product).catch {
                Result.failure<Result<Product?>>(it)
            }.collectLatest {
                _productResult.value = it
            }
        }
    }

    fun getProduct(id: Long) {
        viewModelScope.launch {
            repository.getProduct(id).catch {
                Result.failure<Result<Product?>>(it)
            }.collectLatest {
                _productResult.value = it
            }
        }
    }
}