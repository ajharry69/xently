package co.ke.xently.shoppinglist.ui.list

import co.ke.xently.feature.AbstractListViewModel
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListViewModel @Inject constructor(
    private val repository: IShoppingListRepository,
) : AbstractListViewModel() {
    val pagingData = pagingConfig.flatMapLatest(repository::get).cachedState()
}