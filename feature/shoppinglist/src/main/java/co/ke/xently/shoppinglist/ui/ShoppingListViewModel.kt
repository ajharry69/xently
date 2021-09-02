package co.ke.xently.shoppinglist.ui

import androidx.lifecycle.ViewModel
import co.ke.xently.shoppinglist.repository.AbstractShoppingListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    val repository: AbstractShoppingListRepository,
) : ViewModel() {
}