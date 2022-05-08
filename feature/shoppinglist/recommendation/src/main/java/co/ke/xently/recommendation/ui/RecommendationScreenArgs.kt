package co.ke.xently.recommendation.ui

import co.ke.xently.shoppinglist.repository.ShoppingListGroup

data class RecommendationScreenArgs(
    val itemId: Long? = null,
    val group: ShoppingListGroup? = null,
)