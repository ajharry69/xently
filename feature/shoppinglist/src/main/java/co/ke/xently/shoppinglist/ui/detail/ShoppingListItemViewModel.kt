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
    private val shoppingListItem = MutableStateFlow<ShoppingListItem?>(null)
    private val shoppingListItemId = MutableStateFlow<Long?>(null)
    private val _shoppingItemResult = MutableStateFlow<TaskResult<ShoppingListItem?>>(Success(null))
    val shoppingItemResult: StateFlow<TaskResult<ShoppingListItem?>>
        get() = _shoppingItemResult

    init {
        viewModelScope.launch {
            launch {
                shoppingListItem.collectLatest { item ->
                    if (item != null) {
                        repository.addShoppingListItem(item)
                            .flagLoadingOnStartCatchingErrors()
                            .collectLatest {
                                _shoppingItemResult.value = it
                            }
                    }
                }
            }
            launch {
                shoppingListItemId.collectLatest { itemId ->
                    if (itemId != null) {
                        repository.getShoppingListItem(itemId)
                            .flagLoadingOnStartCatchingErrors()
                            .collectLatest {
                                _shoppingItemResult.value = it
                            }
                    }
                }
            }
        }
    }

    fun addShoppingListItem(item: ShoppingListItem) {
        this.shoppingListItem.value = item
    }

    fun getShoppingListItem(itemId: Long?) {
        shoppingListItemId.value = itemId
    }
}