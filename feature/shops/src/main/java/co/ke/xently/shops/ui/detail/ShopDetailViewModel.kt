package co.ke.xently.shops.ui.detail

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.LocationPermissionViewModel
import co.ke.xently.feature.utils.flagLoadingOnStartCatchingErrors
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShopDetailViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val repository: IShopsRepository,
) : LocationPermissionViewModel(application, savedStateHandle) {
    private val _shopResult = MutableStateFlow<TaskResult<Shop?>>(Success(null))
    val shopResult: StateFlow<TaskResult<Shop?>>
        get() = _shopResult

    fun add(shop: Shop) {
        viewModelScope.launch {
            repository.add(shop)
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _shopResult.value = it
                }
        }
    }

    fun get(id: Long) {
        viewModelScope.launch {
            repository.get(id)
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _shopResult.value = it
                }
        }
    }
}