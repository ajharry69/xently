package co.ke.xently.shops.ui.detail

import co.ke.xently.source.remote.HttpException

internal class ShopHttpException(
    val name: List<String> = emptyList(),
    val town: List<String> = emptyList(),
    val taxPin: List<String> = emptyList(),
    val coordinate: List<String> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors() = listOf(
        name,
        town,
        taxPin,
        coordinate,
    ).any { it.isNotEmpty() }
}