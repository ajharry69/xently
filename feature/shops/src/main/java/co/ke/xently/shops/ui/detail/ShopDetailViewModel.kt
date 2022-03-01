package co.ke.xently.shops.ui.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.Shop
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.viewmodels.LocationPermissionViewModel
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class ShopDetailViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: IShopsRepository,
) : LocationPermissionViewModel(context, savedStateHandle) {
    fun addOrUpdate(shop: Shop) = if (shop.isDefault) {
        repository.add(shop)
    } else {
        repository.update(shop)
    }.flagLoadingOnStart()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    fun get(id: Long) = repository.get(id)
        .flagLoadingOnStart()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Success(Shop.default()))
}