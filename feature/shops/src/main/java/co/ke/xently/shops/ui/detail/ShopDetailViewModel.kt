package co.ke.xently.shops.ui.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.LocationPermissionViewModel
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShopDetailViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: IShopsRepository,
) : LocationPermissionViewModel(context, savedStateHandle) {
    private val _shopResult = MutableStateFlow<TaskResult<Shop?>>(Success(null))
    val shopResult: StateFlow<TaskResult<Shop?>>
        get() = _shopResult

    fun add(shop: Shop) {
        viewModelScope.launch {
            repository.add(shop)
                .flagLoadingOnStart()
                .collectLatest {
                    _shopResult.value = it
                }
        }
    }

    fun get(id: Long) {
        viewModelScope.launch {
            repository.get(id)
                .flagLoadingOnStart()
                .collectLatest {
                    _shopResult.value = it
                }
        }
    }
}