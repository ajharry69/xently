package co.ke.xently.products.ui.detail

import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Product
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.products.repository.IProductsRepository
import co.ke.xently.products.shared.SearchableViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class ProductDetailViewModel @Inject constructor(
    private val repository: IProductsRepository,
) : SearchableViewModel(repository) {
    private val product = MutableSharedFlow<Product>()
    val addResult = product.flatMapLatest {
        if (it.isDefault) {
            repository.add(it)
        } else {
            repository.update(it)
        }.flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun addOrUpdate(product: Product) {
        viewModelScope.launch {
            this@ProductDetailViewModel.product.emit(product)
        }
    }

    private val productId = MutableSharedFlow<Long>()
    val result = productId.flatMapLatest {
        if (it == Product.default().id) {
            flowOf(TaskResult.Success(Product.default()))
        } else {
            repository.get(it).flagLoadingOnStart()
        }
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED, 1)

    fun get(productId: Long) {
        viewModelScope.launch {
            this@ProductDetailViewModel.productId.emit(productId)
        }
    }
}