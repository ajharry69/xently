package co.ke.xently.products.ui.list

import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import androidx.paging.map
import co.ke.xently.data.Shop
import co.ke.xently.products.repository.IProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
internal class ProductListViewModel @Inject constructor(
    private val repository: IProductsRepository,
) : ViewModel() {
    fun get(config: PagingConfig) = repository.get(config).flow.map { data ->
        data.map {
            it.product.copy(shop = it.shop ?: Shop.default())
        }
    }
}