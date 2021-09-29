package co.ke.xently.shoppinglist.ui.list.recommendation

import co.ke.xently.shoppinglist.repository.IShoppingListRepository
import co.ke.xently.shoppinglist.ui.list.AbstractShoppingListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.conflate
import javax.inject.Inject

@HiltViewModel
internal class ShoppingListRecommendationViewModel @Inject constructor(
    private val repository: IShoppingListRepository,
) : AbstractShoppingListViewModel() {
    fun getRecommendations(recommendBy: Any) =
        repository.getRecommendations(recommendBy, groupBy.value ?: "dateadded").conflate()
}