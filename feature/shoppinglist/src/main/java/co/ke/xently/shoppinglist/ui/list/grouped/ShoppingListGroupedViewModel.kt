package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListGroupedViewModel @Inject constructor(
    private val repository: IShoppingListRepository,
) : ViewModel() {
    val groupedShoppingListResult: StateFlow<TaskResult<List<GroupedShoppingList>>>

    private val _groupedShoppingListCount = MutableStateFlow(mapOf<Any, Int>())
    val groupedShoppingListCount: StateFlow<Map<Any, Int>> get() = _groupedShoppingListCount

    private val groupBy = MutableStateFlow(GroupBy.DateAdded)

    init {
        groupedShoppingListResult = groupBy.flatMapLatest(repository::get)
            .flagLoadingOnStart()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), TaskResult.Loading)

        viewModelScope.launch {
            groupBy.collectLatest { group ->
                repository.getCount(group).collectLatest {
                    _groupedShoppingListCount.value = it
                }
            }
        }
    }
}