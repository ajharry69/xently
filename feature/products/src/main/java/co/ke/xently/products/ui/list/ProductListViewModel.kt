package co.ke.xently.products.ui.list

import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Shop
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.viewmodels.AbstractPagedListViewModel
import co.ke.xently.products.repository.IProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class ProductListViewModel @Inject constructor(
    private val repository: IProductsRepository,
) : AbstractPagedListViewModel() {
    private val _shopId = MutableStateFlow<Long?>(null)
    private val shopId = _shopId.asStateFlow()

    fun setShopId(id: Long?) {
        _shopId.value = if (id != null && id == Shop.default().id) {
            null
        } else {
            id
        }
    }

    val shopName = shopId.flatMapLatest {
        if (it != null) {
            repository.getShopName(it)
        } else {
            flowOf(null)
        }
    }.stateIn(
        initialValue = null,
        scope = viewModelScope,
        started = DEFAULT_SHARING_STARTED,
    )

    val pagingData = combineTransform(shopId, pagingConfig) { id, config ->
        emitAll(repository.get(id, config))
    }.cachedState()
}