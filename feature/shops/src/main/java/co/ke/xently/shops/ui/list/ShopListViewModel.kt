package co.ke.xently.shops.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
internal class ShopListViewModel @Inject constructor(
    private val repository: IShopsRepository,
) : ViewModel() {
    fun get(config: PagingConfig, query: String = "") =
        combineTransform(flowOf(config), flowOf(query)) { c, q ->
            emitAll(repository.get(c, q))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PagingData.empty())
}