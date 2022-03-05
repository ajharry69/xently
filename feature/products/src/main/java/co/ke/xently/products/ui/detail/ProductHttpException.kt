package co.ke.xently.products.ui.detail

import co.ke.xently.products.shared.AttributeHttpException
import co.ke.xently.products.shared.BrandHttpException
import co.ke.xently.source.remote.HttpException

class ProductHttpException(
    val shop: List<String> = emptyList(),
    val name: List<String> = emptyList(),
    val unit: List<String> = emptyList(),
    val unitQuantity: List<String> = emptyList(),
    val purchasedQuantity: List<String> = emptyList(),
    val unitPrice: List<String> = emptyList(),
    val datePurchased: List<String> = emptyList(),
    val brands: List<BrandHttpException> = emptyList(),
    val attributes: List<AttributeHttpException> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors() = listOf(
        shop,
        name,
        unit,
        unitQuantity,
        purchasedQuantity,
        unitPrice,
        datePurchased,
        brands,
        attributes,
    ).any { it.isNotEmpty() }
}