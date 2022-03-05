package co.ke.xently.shoppinglist.ui.detail

import co.ke.xently.products.shared.AttributeHttpException
import co.ke.xently.products.shared.BrandHttpException
import co.ke.xently.source.remote.HttpException

internal class ShoppingListItemHttpException(
    val name: List<String> = emptyList(),
    val unit: List<String> = emptyList(),
    val unitQuantity: List<String> = emptyList(),
    val purchaseQuantity: List<String> = emptyList(),
    val brands: List<BrandHttpException> = emptyList(),
    val attributes: List<AttributeHttpException> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors() = listOf(
        name,
        unit,
        unitQuantity,
        purchaseQuantity,
        brands,
        attributes,
    ).any { it.isNotEmpty() }
}