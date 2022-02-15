package co.ke.xently.shoppinglist.ui.detail

import androidx.lifecycle.viewModelScope
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.products.shared.SearchableViewModel
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListItemViewModel @Inject constructor(
    private val repository: IShoppingListRepository,
) : SearchableViewModel(repository) {
    private val _shoppingItemResult = MutableStateFlow<TaskResult<ShoppingListItem?>>(Success(null))
    val result: StateFlow<TaskResult<ShoppingListItem?>>
        get() = _shoppingItemResult

    fun addOrUpdate(item: ShoppingListItem) {
        viewModelScope.launch {
            repository.add(item)
                .flagLoadingOnStart()
                .collectLatest {
                    _shoppingItemResult.value = it
                }
        }
    }

    fun get(id: Long) {
        viewModelScope.launch {
            repository.get(id)
                .flagLoadingOnStart()
                .collectLatest {
                    _shoppingItemResult.value = it
                }
        }
    }
}