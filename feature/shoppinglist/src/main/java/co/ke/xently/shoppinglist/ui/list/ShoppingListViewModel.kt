package co.ke.xently.shoppinglist.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListViewModel @Inject constructor(
    private val repository: IShoppingListRepository,
) : ViewModel() {
    fun get(config: PagingConfig) = combineTransform(flowOf(config)) {
        emitAll(repository.get(it[0]))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PagingData.empty())
}