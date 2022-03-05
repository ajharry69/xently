package co.ke.xently.shops.ui.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.feature.viewmodels.LocationPermissionViewModel
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class ShopDetailViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: IShopsRepository,
) : LocationPermissionViewModel(context, savedStateHandle) {
    private val shop = MutableSharedFlow<Shop>()
    val addResult = shop.flatMapLatest {
        if (it.isDefault) {
            repository.add(it)
        } else {
            repository.update(it)
        }.flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun addOrUpdate(shop: Shop) {
        viewModelScope.launch {
            this@ShopDetailViewModel.shop.emit(shop)
        }
    }

    private val shopId = MutableSharedFlow<Long>()
    val result = shopId.flatMapLatest {
        if (it == Shop.default().id) {
            flowOf(TaskResult.Success(Shop.default()))
        } else {
            repository.get(it).flagLoadingOnStart()
        }
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED, 1)

    fun get(shopId: Long) {
        viewModelScope.launch {
            this@ShopDetailViewModel.shopId.emit(shopId)
        }
    }
}