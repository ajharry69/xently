package co.ke.xently.shops.ui.list.addresses

import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddressListViewModel @Inject constructor(private val repository: IShopsRepository) :
    ViewModel() {

    fun get(shopId: Long, config: PagingConfig, query: String = "") = repository.getAddresses(shopId, config, query)

}