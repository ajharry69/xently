package co.ke.xently.shoppinglist.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.ShoppingListItem
import co.ke.xently.data.TaskResult
import co.ke.xently.data.TaskResult.Success
import co.ke.xently.feature.utils.flagLoadingOnStartCatchingErrors
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
) : ViewModel() {
    private val _shoppingItemResult = MutableStateFlow<TaskResult<ShoppingListItem?>>(Success(null))
    val shoppingItemResult: StateFlow<TaskResult<ShoppingListItem?>>
        get() = _shoppingItemResult

    fun add(item: ShoppingListItem) {
        viewModelScope.launch {
            repository.add(item)
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _shoppingItemResult.value = it
                }
        }
    }

    fun get(id: Long) {
        viewModelScope.launch {
            repository.get(id)
                .flagLoadingOnStartCatchingErrors()
                .collectLatest {
                    _shoppingItemResult.value = it
                }
        }
    }
}