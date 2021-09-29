package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.lifecycle.viewModelScope
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import co.ke.xently.shoppinglist.ui.list.AbstractShoppingListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListGroupedViewModel @Inject constructor(
    private val repository: IShoppingListRepository,
) : AbstractShoppingListViewModel() {
    private val _groupedShoppingListResult =
        MutableStateFlow(Result.success<List<GroupedShoppingList>?>(null))
    val groupedShoppingListResult: StateFlow<Result<List<GroupedShoppingList>?>>
        get() = _groupedShoppingListResult

    private val _groupedShoppingListCount =
        MutableStateFlow(mapOf<Any, Int>())
    val groupedShoppingListCount: StateFlow<Map<Any, Int>> get() = _groupedShoppingListCount

    init {
        viewModelScope.launch {
            groupBy.collectLatest { group ->
                repository.getGroupedShoppingList(group ?: "dateadded").catch {
                    emit(
                        Result.failure(
                            it
                        )
                    )
                }
                    .collectLatest {
                        _groupedShoppingListResult.value = it
                    }
            }
        }
        viewModelScope.launch {
            groupBy.collectLatest { group ->
                repository.getGroupedShoppingListCount(group ?: "dateadded")
                    .collectLatest {
                        _groupedShoppingListCount.value = it
                    }
            }
        }
    }
}