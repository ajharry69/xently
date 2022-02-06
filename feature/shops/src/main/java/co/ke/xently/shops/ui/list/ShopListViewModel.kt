package co.ke.xently.shops.ui.list

import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import co.ke.xently.shops.repository.IShopsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
internal class ShopListViewModel @Inject constructor(
    private val repository: IShopsRepository,
) : ViewModel() {
    fun get(config: PagingConfig, query: String = "") =
        combineTransform(flowOf(config), flowOf(query)) { c, q ->
            emitAll(repository.get(c, q).flow)
        }
}