package co.ke.xently.products.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.ke.xently.products.repository.IProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
internal class ProductListViewModel @Inject constructor(
    private val repository: IProductsRepository,
) : ViewModel() {
    fun getShopName(shopId: Long) = repository.getShopName(shopId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(replayExpirationMillis = 5000),
        null,
    )

    fun get(config: PagingConfig, shopId: Long?) = combineTransform(flowOf(config)) {
        emitAll(repository.get(shopId, it[0]))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PagingData.empty())
}