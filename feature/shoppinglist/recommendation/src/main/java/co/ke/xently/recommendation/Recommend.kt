package co.ke.xently.recommendation

import co.ke.xently.shoppinglist.GroupBy


data class Recommend(
    internal val by: Any = Unit,
    internal val from: From = From.GroupedList,
    internal val saveBy: Boolean = false,
    internal val groupBy: GroupBy = GroupBy.DateAdded,
) {
    enum class From {
        Item,
        ItemList,
        GroupedList,
    }
}