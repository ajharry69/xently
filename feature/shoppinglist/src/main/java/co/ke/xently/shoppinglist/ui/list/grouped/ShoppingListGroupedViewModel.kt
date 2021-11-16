package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.utils.flagLoadingOnStartCatchingErrors
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListGroupedViewModel @Inject constructor(
    private val repository: IShoppingListRepository,
) : ViewModel() {
    private val _groupedShoppingListResult =
        MutableStateFlow<TaskResult<List<GroupedShoppingList>>>((TaskResult.Loading))
    val groupedShoppingListResult: StateFlow<TaskResult<List<GroupedShoppingList>>>
        get() = _groupedShoppingListResult

    private val _groupedShoppingListCount = MutableStateFlow(mapOf<Any, Int>())
    val groupedShoppingListCount: StateFlow<Map<Any, Int>> get() = _groupedShoppingListCount

    private val groupBy = MutableStateFlow(GroupBy.DateAdded)

    init {
        viewModelScope.launch {
            launch {
                groupBy.collectLatest { group ->
                    repository.getGroupedShoppingList(group)
                        .flagLoadingOnStartCatchingErrors()
                        .collectLatest {
                            _groupedShoppingListResult.value = it
                        }
                }
            }
            launch {
                groupBy.collectLatest { group ->
                    repository.getGroupedShoppingListCount(group).collectLatest {
                        _groupedShoppingListCount.value = it
                    }
                }
            }
        }
    }
}