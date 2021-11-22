package co.ke.xently.shoppinglist.ui.list

import androidx.lifecycle.ViewModel
import androidx.paging.PagingConfig
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListViewModel @Inject constructor(
    private val repository: IShoppingListRepository,
) : ViewModel() {
    fun getPagingData(config: PagingConfig) = repository.get(config).flow
}