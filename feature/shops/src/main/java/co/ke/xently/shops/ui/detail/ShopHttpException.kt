package co.ke.xently.shops.ui.detail

import co.ke.xently.source.remote.HttpException

internal class ShopHttpException(
    val name: List<String> = emptyList(),
    val taxPin: List<String> = emptyList(),
    val addresses: List<AddressHttpException> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors() = listOf(
        name,
        taxPin,
        addresses,
    ).any { it.isNotEmpty() }
}