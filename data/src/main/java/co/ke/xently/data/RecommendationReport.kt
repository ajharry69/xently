package co.ke.xently.data

import java.text.DecimalFormat

data class RecommendationReport(
    val lookupDuration: Float = 0f,
    val missedItems: List<ShoppingListItem> = emptyList(),
    val recommendations: List<Recommendation> = emptyList(),
    val count: Count = Count().copy(
        missedItems = missedItems.size,
        recommendations = recommendations.size,
    ),
) {
    data class Count(
        val hitItems: Int = 0,
        val missedItems: Int = 0,
        val recommendations: Int = 0,
        val shopsVisited: Int = 0,
    )

    data class Recommendation(
        val id: Long = -1L,
        val name: String = "",
        val taxPin: String = "",
        val hits: Hits = Hits(),
        val addresses: List<Address> = emptyList(),
    ) {
        val estimatedDistance: String = "2 km" // TODO: Move this in `Recommendation` constructor

        val printableTotalPrice: String
            get() = DecimalFormat("KES ###,###.##").format(hits.totalPrice)

        data class Hits(
            val count: Int = 0,
            val totalPrice: Float = 0f,
            val items: List<Item> = emptyList(),
        ) {
            data class Item(
                val item: ShoppingListItem = ShoppingListItem(),
                val purchaseQuantity: Float = 0f,
                val unitPrice: Float = 0f,
                val totalPrice: Float = 0f,
            )
        }
    }
}