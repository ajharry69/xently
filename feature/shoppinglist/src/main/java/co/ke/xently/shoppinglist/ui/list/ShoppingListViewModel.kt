package co.ke.xently.shoppinglist.ui.list

import androidx.lifecycle.viewModelScope
import co.ke.xently.feature.viewmodels.AbstractPagedListViewModel
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import co.ke.xently.shoppinglist.repository.ShoppingListGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListViewModel @Inject constructor(
    private val repository: IShoppingListRepository,
) : AbstractPagedListViewModel() {
    private val group = MutableSharedFlow<ShoppingListGroup?>(replay = 1)
    val pagingData = combineTransform(pagingConfig, group) { a, b ->
        emitAll(repository.get(a, b))
    }.cachedState()

    suspend fun setGroup(group: ShoppingListGroup?) {
        viewModelScope.launch {
            this@ShoppingListViewModel.group.emit(group)
        }
    }
}