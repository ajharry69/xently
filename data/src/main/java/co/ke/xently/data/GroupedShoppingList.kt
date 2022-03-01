package co.ke.xently.data

import co.ke.xently.common.DEFAULT_LOCAL_DATE_FORMAT
import java.util.*
import kotlin.random.Random

data class GroupedShoppingList(
    override val group: String,
    val shoppingList: List<ShoppingListItem>,
    override val numberOfItems: Int = shoppingList.size,
    val isDefault: Boolean = false,
) : GroupedShoppingListCount(group, numberOfItems) {
    companion object {
        fun default() = GroupedShoppingList(
            isDefault = true,
            group = DEFAULT_LOCAL_DATE_FORMAT.format(Date()),
            shoppingList = List(Random.nextInt(1, 20)) {
                ShoppingListItem.default()
            },
        )
    }
}