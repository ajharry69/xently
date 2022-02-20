package co.ke.xently.shops.ui.detail

import co.ke.xently.source.remote.HttpException

data class AddressHttpException(
    val shop: List<String> = emptyList(),
    val town: List<String> = emptyList(),
    val coordinates: List<String> = emptyList(),
) : HttpException() {
    override fun hasFieldErrors() = listOf(shop, town, coordinates).any { it.isNotEmpty() }
    override fun toString(): String {
        val error = StringBuilder()
        shop.joinToString("\n").also {
            if (it.isNotBlank()) {
                error.append("Shop: $it")
            }
        }
        town.joinToString("\n").also {
            if (it.isNotBlank()) {
                error.append("Town: $it")
            }
        }
        coordinates.joinToString("\n").also {
            if (it.isNotBlank()) {
                error.append("Coordinates: $it")
            }
        }
        return error.toString()
    }
}