package co.ke.xently.data

data class GroupedShoppingList(
    override val group: String,
    val shoppingList: List<ShoppingListItem>,
    override val numberOfItems: Int = shoppingList.size,
) : GroupedShoppingListCount(group, numberOfItems)