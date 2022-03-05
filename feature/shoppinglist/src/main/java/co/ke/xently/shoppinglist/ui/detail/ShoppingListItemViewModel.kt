package co.ke.xently.shoppinglist.ui.detail

import androidx.lifecycle.viewModelScope
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.utils.DEFAULT_SHARING_STARTED
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.products.shared.SearchableViewModel
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListItemViewModel @Inject constructor(
    private val repository: IShoppingListRepository,
) : SearchableViewModel(repository) {
    private val shoppingListItem = MutableSharedFlow<ShoppingListItem>()
    val addResult = shoppingListItem.flatMapLatest {
        repository.add(it).flagLoadingOnStart()
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED)

    fun addOrUpdate(item: ShoppingListItem) {
        viewModelScope.launch {
            this@ShoppingListItemViewModel.shoppingListItem.emit(item)
        }
    }

    private val shoppingListItemId = MutableSharedFlow<Long>()
    val result = shoppingListItemId.flatMapLatest {
        if (it == ShoppingListItem.default().id) {
            flowOf(Success(ShoppingListItem.default()))
        } else {
            repository.get(it).flagLoadingOnStart()
        }
    }.shareIn(viewModelScope, DEFAULT_SHARING_STARTED, 1)

    fun get(shoppingListItemId: Long) {
        viewModelScope.launch {
            this@ShoppingListItemViewModel.shoppingListItemId.emit(shoppingListItemId)
        }
    }
}