package co.ke.xently.shops.ui.detail

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Shop
import co.ke.xently.feature.LocationPermissionViewModel
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShopDetailViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val repository: IShopsRepository,
) : LocationPermissionViewModel(application, savedStateHandle) {
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

    fun getShop(id: Long) {
        viewModelScope.launch {
            repository.getShop(id).catch {
                Result.failure<Result<Shop?>>(it)
            }.collectLatest {
                _shopResult.value = it
            }
        }
    }
}