package co.ke.xently.shops.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.utils.flagLoadingOnStartCatchingErrors
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShopListViewModel @Inject constructor(
    private val repository: IShopsRepository,
) : ViewModel() {
    private val _shopListResult =
        MutableStateFlow<TaskResult<List<Shop>>>(TaskResult.Loading)
    val shopListResult: StateFlow<TaskResult<List<Shop>>>
        get() = _shopListResult

    init {
        viewModelScope.launch {
            repository.getShopList(true)
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _shopListResult.value = it
                }
        }
    }
}