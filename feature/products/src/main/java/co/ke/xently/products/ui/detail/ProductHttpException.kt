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
    data class Error(
        val shop: String,
        val name: String,
        val unit: String,
        val unitQuantity: String,
        val purchasedQuantity: String,
        val unitPrice: String,
        val datePurchased: String,
        val brands: String,
        val attributes: String,
    )

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


internal val ProductHttpException?.error: ProductHttpException.Error
    get() {
        return ProductHttpException.Error(
            this?.shop?.joinToString("\n") ?: "",
            this?.name?.joinToString("\n") ?: "",
            this?.unit?.joinToString("\n") ?: "",
            this?.unitQuantity?.joinToString("\n") ?: "",
            this?.purchasedQuantity?.joinToString("\n") ?: "",
            this?.unitPrice?.joinToString("\n") ?: "",
            this?.datePurchased?.joinToString("\n") ?: "",
            this?.brands?.flatMap { it.name }?.joinToString("\n") ?: "",
            this?.attributes?.joinToString("\n") ?: "",
        )
    }