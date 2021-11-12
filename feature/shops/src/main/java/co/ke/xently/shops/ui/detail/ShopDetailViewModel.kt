package co.ke.xently.shops.ui.detail

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
class ShopDetailViewModel @Inject constructor(private val repository: IShopsRepository) :
    ViewModel() {
    private val _shopResult = MutableStateFlow<Result<Shop?>>(Result.success(null))
    val shopResult: StateFlow<Result<Shop?>>
        get() = _shopResult

    fun addShop(shop: Shop) {
        viewModelScope.launch {
            repository.addShop(shop).catch {
                Result.failure<Result<Shop?>>(it)
            }.collectLatest {
                _shopResult.value = it
            }
        }
    }
}