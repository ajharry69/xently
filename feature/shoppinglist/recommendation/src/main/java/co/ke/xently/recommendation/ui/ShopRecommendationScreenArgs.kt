package co.ke.xently.recommendation.ui

import co.ke.xently.shoppinglist.repository.ShoppingListGroup

data class ShopRecommendationScreenArgs(
    val itemId: Long? = null,
    val group: ShoppingListGroup? = null,
) {
    init {
        if (itemId == null && group == null) {
            throw IllegalStateException("`itemId` and `group` are mutually exclusive and cannot be null at the same time.")
        }
    }
}