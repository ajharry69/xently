package co.ke.xently.shops.ui.list.addresses

import androidx.lifecycle.viewModelScope
import co.ke.xently.feature.AbstractListViewModel
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AddressListViewModel @Inject constructor(
    private val repository: IShopsRepository,
) : AbstractListViewModel() {
    private val _shopId = MutableStateFlow<Long>(-1)
    private val shopId = _shopId.asStateFlow()

    fun setShopId(id: Long) {
        _shopId.value = id
    }

    val shopName = shopId.flatMapLatest(repository::getShopName).stateIn(
        initialValue = null,
        scope = viewModelScope,
        started = defaultSharingStarted,
    )

    val pagingData = combineTransform(shopId, pagingConfig) { id, config ->
        emitAll(repository.getAddresses(id, config, ""))
    }.cachedState()
}