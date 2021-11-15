package co.ke.xently.shops.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Shop
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShopListViewModel @Inject constructor(
    private val repository: IShopsRepository,
) : ViewModel() {
    // interpret `null` as loading...
    private val _shopListResult = MutableStateFlow(Result.success<List<Shop>?>(null))
    val shopListResult: StateFlow<Result<List<Shop>?>>
        get() = _shopListResult

    init {
        viewModelScope.launch {
            repository.getShopList(true)
                .catch { emit(Result.failure(it)) }
                .collectLatest {
                    _shopListResult.value = it
                }
        }
    }
}