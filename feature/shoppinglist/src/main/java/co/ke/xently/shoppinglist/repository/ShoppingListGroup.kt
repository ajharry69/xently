package co.ke.xently.shoppinglist.repository

import co.ke.xently.shoppinglist.GroupBy

data class ShoppingListGroup(
    val group: Any,
    val groupBy: GroupBy = GroupBy.DateAdded,
)