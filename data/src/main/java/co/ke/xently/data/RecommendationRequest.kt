package co.ke.xently.data

data class RecommendationRequest(
    val items: List<ShoppingListItem>,
    // Save shopping list items...
    val persist: Boolean = true,
)