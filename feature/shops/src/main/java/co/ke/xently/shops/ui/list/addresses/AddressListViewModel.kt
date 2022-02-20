package co.ke.xently.shops.ui.list.addresses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AddressListViewModel @Inject constructor(private val repository: IShopsRepository) :
    ViewModel() {

    fun getShopName(shopId: Long) = repository.getShopName(shopId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(replayExpirationMillis = 5000),
        null,
    )

    fun get(shopId: Long, config: PagingConfig, query: String = "") =
        repository.getAddresses(shopId, config, query)

}