package co.ke.xently.shoppinglist.ui.list

import co.ke.xently.feature.AbstractViewModel
import kotlinx.coroutines.flow.MutableStateFlow

internal abstract class AbstractShoppingListViewModel : AbstractViewModel() {
    protected val groupBy = MutableStateFlow<String?>(null)

    /*fun setGroupBy(groupBy: String) {
        this.groupBy.value = groupBy
    }*/
}