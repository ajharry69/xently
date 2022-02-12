package co.ke.xently.products.ui.detail

import co.ke.xently.source.remote.HttpException

internal class ProductHttpException(
    val shop: List<String> = emptyList(),
    val name: List<String> = emptyList(),
    val unit: List<String> = emptyList(),
    val unitQuantity: List<String> = emptyList(),
    val purchasedQuantity: List<String> = emptyList(),
    val unitPrice: List<String> = emptyList(),
    val datePurchased: List<String> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors() = listOf(
        shop,
        name,
        unit,
        unitQuantity,
        purchasedQuantity,
        unitPrice,
        datePurchased,
    ).any { it.isNotEmpty() }
}