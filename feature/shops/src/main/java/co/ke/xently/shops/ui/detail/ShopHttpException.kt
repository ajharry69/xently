package co.ke.xently.shops.ui.detail

import co.ke.xently.source.remote.HttpException

internal class ShopHttpException(
    val name: List<String> = emptyList(),
    val taxPin: List<String> = emptyList(),
    val addresses: List<AddressHttpException> = emptyList(),
) : HttpException() {
    data class Error(
        val name: String,
        val taxPin: String,
        val addresses: String,
    )

    override fun hasFieldErrors() = listOf(
        name,
        taxPin,
        addresses,
    ).any { it.isNotEmpty() }
}


internal val ShopHttpException?.error: ShopHttpException.Error
    get() {
        return ShopHttpException.Error(
            this?.name?.joinToString("\n") ?: "",
            this?.taxPin?.joinToString("\n") ?: "",
            this?.addresses?.joinToString("\n") ?: "",
        )
    }