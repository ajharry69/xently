package co.ke.xently.shoppinglist.ui.list.grouped

import androidx.lifecycle.ViewModel
import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShoppingListGroupedViewModel @Inject constructor(private val repository: IShoppingListRepository) :
    ViewModel() {
}