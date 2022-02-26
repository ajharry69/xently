package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.lifecycle.viewModelScope
import co.ke.xently.data.GroupedShoppingList
import co.ke.xently.data.TaskResult
import co.ke.xently.feature.AbstractAuthViewModel
import co.ke.xently.feature.utils.flagLoadingOnStart
import co.ke.xently.shoppinglist.GroupBy
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import co.ke.xently.source.local.Database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListGroupedViewModel @Inject constructor(
    database: Database,
    private val repository: IShoppingListRepository,
) : AbstractAuthViewModel(database) {
    val shoppingListResult: StateFlow<TaskResult<List<GroupedShoppingList>>>
    val shoppingListCount: StateFlow<Map<Any, Int>>

    private val groupBy = MutableStateFlow(GroupBy.DateAdded)

    init {
        shoppingListResult = historicallyFirstUser.combineTransform(groupBy) { _, b ->
            emitAll(repository.get(b).flagLoadingOnStart())
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            TaskResult.Success(emptyList()),
        )

        shoppingListCount = groupBy.flatMapLatest(repository::getCount)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())
    }
}