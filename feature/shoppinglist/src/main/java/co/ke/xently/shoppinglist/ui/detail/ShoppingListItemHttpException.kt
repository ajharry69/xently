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
    data class Error(
        val name: String,
        val unit: String,
        val unitQuantity: String,
        val purchaseQuantity: String,
        val brands: String,
        val attributes: String,
    )

    override fun hasFieldErrors() = listOf(
        name,
        unit,
        unitQuantity,
        purchaseQuantity,
        brands,
        attributes,
    ).any { it.isNotEmpty() }
}


internal val ShoppingListItemHttpException?.error: ShoppingListItemHttpException.Error
    get() {
        return ShoppingListItemHttpException.Error(
            this?.name?.joinToString("\n") ?: "",
            this?.unit?.joinToString("\n") ?: "",
            this?.unitQuantity?.joinToString("\n") ?: "",
            this?.purchaseQuantity?.joinToString("\n") ?: "",
            this?.brands?.flatMap { it.name }?.joinToString("\n") ?: "",
            this?.attributes?.joinToString("\n") ?: "",
        )
    }